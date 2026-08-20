package com.shaanxi.zhiping.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shaanxi.zhiping.common.CacheConstants;
import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.dto.QuestionCreateDTO;
import com.shaanxi.zhiping.dto.QuestionImportBatchQueryDTO;
import com.shaanxi.zhiping.dto.QuestionImportDTO;
import com.shaanxi.zhiping.dto.QuestionImportResultVO;
import com.shaanxi.zhiping.dto.QuestionProofreadStatVO;
import com.shaanxi.zhiping.dto.QuestionQueryDTO;
import com.shaanxi.zhiping.dto.QuestionReviewDTO;
import com.shaanxi.zhiping.entity.Question;
import com.shaanxi.zhiping.entity.QuestionDeleted;
import com.shaanxi.zhiping.entity.QuestionImportBatch;
import com.shaanxi.zhiping.entity.Paper;
import com.shaanxi.zhiping.exception.BusinessException;
import com.shaanxi.zhiping.mapper.QuestionDeletedMapper;
import com.shaanxi.zhiping.mapper.QuestionImportBatchMapper;
import com.shaanxi.zhiping.mapper.QuestionMapper;
import com.shaanxi.zhiping.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 题库中心 Service
 *
 * 负责 5 大功能：
 * 1. 试题录入 create
 * 2. 批量导入 importExcel
 * 3. 试题校对 proofread（list + update）
 * 4. 试题编辑 update
 * 5. 试题审核 review
 */
@Slf4j
@Service
public class AdminQuestionService {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private QuestionDeletedMapper questionDeletedMapper;

    @Resource
    private QuestionImportBatchMapper batchMapper;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private PaperService paperService;

    /** 题型名称 → 编码 */
    private static final Map<String, Integer> TYPE_NAME_MAP = new HashMap<>();
    /** 难度名称 → 编码 */
    private static final Map<String, Integer> DIFFICULTY_NAME_MAP = new HashMap<>();

    static {
        TYPE_NAME_MAP.put("单选", 1);
        TYPE_NAME_MAP.put("单选题", 1);
        TYPE_NAME_MAP.put("多选", 2);
        TYPE_NAME_MAP.put("多选题", 2);
        TYPE_NAME_MAP.put("判断", 3);
        TYPE_NAME_MAP.put("判断题", 3);
        TYPE_NAME_MAP.put("填空", 4);
        TYPE_NAME_MAP.put("填空题", 4);
        TYPE_NAME_MAP.put("简答", 5);
        TYPE_NAME_MAP.put("简答题", 5);
        TYPE_NAME_MAP.put("计算", 6);
        TYPE_NAME_MAP.put("计算题", 6);
        TYPE_NAME_MAP.put("复合", 7);
        TYPE_NAME_MAP.put("复合题", 7);
        TYPE_NAME_MAP.put("大题", 7);

        DIFFICULTY_NAME_MAP.put("简单", 1);
        DIFFICULTY_NAME_MAP.put("容易", 1);
        DIFFICULTY_NAME_MAP.put("中等", 2);
        DIFFICULTY_NAME_MAP.put("一般", 2);
        DIFFICULTY_NAME_MAP.put("困难", 3);
        DIFFICULTY_NAME_MAP.put("难", 3);
    }

    // ==================== 1. 试题录入 / 编辑 ====================

    /**
     * 新增题目（录入）
     * - submitType=save_draft → status=0
     * - submitType=submit_for_review 或不传 → status=1
     */
    public Long create(QuestionCreateDTO dto) {
        validateQuestionDto(dto, true);
        Question q = convertToEntity(dto);
        if (q.getStatus() == null) {
            q.setStatus("save_draft".equals(dto.getSubmitType()) ? 0 : 1);
        }
        // 查重：同一 (content_hash, type, biz_section, category_id, year) 且未删除的题已存在则拒绝录入
        String hash = computeContentHash(q.getContent(), q.getType());
        q.setContentHash(hash);
        Question existed = findDuplicate(hash, q.getType(), q.getBizSection(), q.getCategoryId(), q.getYear());
        if (existed != null) {
            throw new BusinessException(409, "题干与现有题目重复，已存在题 ID=" + existed.getId()
                    + "（题型=" + typeText(q.getType())
                    + "，业务分区=" + q.getBizSection()
                    + "，分类ID=" + q.getCategoryId()
                    + "，年份=" + q.getYear() + "）");
        }
        questionMapper.insert(q);
        invalidateCache();
        log.info("录入题目成功 id={}, type={}, categoryId={}", q.getId(), q.getType(), q.getCategoryId());
        return q.getId();
    }

    /**
     * 更新题目（编辑 / 校对保存）
     */
    public boolean update(QuestionCreateDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(400, "更新题目时 id 不能为空");
        }
        Question existed = questionMapper.selectById(dto.getId());
        if (existed == null) {
            throw new BusinessException(404, "题目不存在");
        }
        validateQuestionDto(dto, false);
        Question q = convertToEntity(dto);
        // 编辑后默认重新进入"待审核"（除非显式指定 status）
        if (q.getStatus() == null) {
            q.setStatus(1);
        }
        // 任一字段变更 → 重算 hash，并校验与其他题不冲突（5 项全等才算重复）
        String newContent = q.getContent() != null ? q.getContent() : existed.getContent();
        Integer newType = q.getType() != null ? q.getType() : existed.getType();
        Integer newBizSection = q.getBizSection() != null ? q.getBizSection() : existed.getBizSection();
        Long newCategoryId = q.getCategoryId() != null ? q.getCategoryId() : existed.getCategoryId();
        Integer newYear = q.getYear() != null ? q.getYear() : existed.getYear();
        boolean contentChanged = q.getContent() != null && !q.getContent().equals(existed.getContent());
        boolean typeChanged = q.getType() != null && !q.getType().equals(existed.getType());
        boolean bizChanged = q.getBizSection() != null && !q.getBizSection().equals(existed.getBizSection());
        boolean catChanged = q.getCategoryId() != null && !q.getCategoryId().equals(existed.getCategoryId());
        boolean yearChanged = q.getYear() != null && !q.getYear().equals(existed.getYear());
        if (contentChanged || typeChanged || bizChanged || catChanged || yearChanged) {
            String hash = computeContentHash(newContent, newType);
            Question dup = findDuplicate(hash, newType, newBizSection, newCategoryId, newYear);
            if (dup != null && !dup.getId().equals(dto.getId())) {
                throw new BusinessException(409, "修改后题干与其他题目重复，冲突题 ID=" + dup.getId()
                        + "（题型=" + typeText(newType)
                        + "，业务分区=" + newBizSection
                        + "，分类ID=" + newCategoryId
                        + "，年份=" + newYear + "）");
            }
            q.setContentHash(hash);
        }
        questionMapper.updateById(q);
        invalidateCache();
        log.info("更新题目成功 id={}", q.getId());
        return true;
    }

    /**
     * 删除题目（物理删除模型）：
     * 1) 把要删的题目（含其所有子题）查出来
     * 2) 写入归档表 t_question_deleted（deleted=1, deleted_at=NOW）
     * 3) 从主表 t_question 物理删除
     * 4) 清缓存
     * 删除复合题父行时，会一并归档删除其所有子题（parent_id = 父id）。
     *
     * @param id 题目 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        // 收集本删除动作要归档的 id 列表（自身 + 子题）
        List<Long> ids = collectDeleteIds(id);
        if (ids.isEmpty()) {
            return false;
        }
        // 引用校验：被任何试卷引用则禁止删除
        List<Paper> refs = paperService.findPapersReferencing(ids);
        if (!refs.isEmpty()) {
            throw new BusinessException(buildReferencedMessage(refs));
        }
        archiveAndDelete(ids);
        for (Long x : ids) {
            redisUtil.delete(CacheConstants.QUESTION_DETAIL_PREFIX + x);
            redisUtil.delete(CacheConstants.QUESTION_CHILDREN_PREFIX + x);
        }
        invalidateCache();
        return true;
    }

    /**
     * 批量删除（物理删除模型）：
     * 对每个 id 收集自身 + 子题，去重后统一归档 + 物理删除。
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatch(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        // 收集所有要删的 id（含级联子题），去重
        java.util.LinkedHashSet<Long> all = new java.util.LinkedHashSet<>();
        for (Long id : ids) {
            all.addAll(collectDeleteIds(id));
        }
        if (all.isEmpty()) {
            return 0;
        }
        List<Long> list = new java.util.ArrayList<>(all);
        // 引用校验：被任何试卷引用则禁止删除
        List<Paper> refs = paperService.findPapersReferencing(list);
        if (!refs.isEmpty()) {
            throw new BusinessException(buildReferencedMessage(refs));
        }
        archiveAndDelete(list);
        for (Long x : list) {
            redisUtil.delete(CacheConstants.QUESTION_DETAIL_PREFIX + x);
            redisUtil.delete(CacheConstants.QUESTION_CHILDREN_PREFIX + x);
        }
        invalidateCache();
        return list.size();
    }

    /**
     * 构造"试题被试卷引用，禁止删除"的错误提示。
     * 仅列出试卷 title（去重、按出现顺序），最多展示 5 个，避免消息过长。
     */
    private String buildReferencedMessage(List<Paper> refs) {
        java.util.LinkedHashSet<String> titles = new java.util.LinkedHashSet<>();
        for (Paper p : refs) {
            if (p.getTitle() != null && !p.getTitle().isEmpty()) {
                titles.add(p.getTitle());
            } else {
                titles.add("试卷#" + p.getId());
            }
        }
        StringBuilder sb = new StringBuilder("试题已被以下试卷引用，无法删除：");
        int i = 0;
        for (String t : titles) {
            if (i >= 5) {
                sb.append(" 等").append(titles.size()).append("套试卷");
                break;
            }
            if (i > 0) {
                sb.append("、");
            }
            sb.append("「").append(t).append("」");
            i++;
        }
        sb.append("；请先在「试卷管理」中移除相关题目或删除试卷后再试。");
        return sb.toString();
    }

    /**
     * 收集删除一个复合题父行时需要级联删除的所有 id：
     * - id 自身
     * - 若 id 是复合题父行（parent_id=0 且 type=7），则还包含 parent_id=id 的所有子题
     * 简单题（非复合题父行）只删自身。
     * 使用物理查询（绕过全局逻辑删除过滤），即使 deleted=1 也能查到，保证幂等。
     */
    private List<Long> collectDeleteIds(Long id) {
        Question q = questionMapper.physicalSelectById(id);
        if (q == null) {
            return java.util.Collections.emptyList();
        }
        List<Long> result = new java.util.ArrayList<>();
        result.add(id);
        // 复合题父行 → 级联删除子题
        if (q.getParentId() != null && q.getParentId() == 0L
                && q.getType() != null && q.getType() == 7) {
            List<Question> children = questionMapper.physicalSelectByParentId(id);
            for (Question c : children) {
                result.add(c.getId());
            }
        }
        return result;
    }

    /**
     * 把给定 ids 对应的题目归档到 t_question_deleted，然后从 t_question 物理删除。
     * 注意：不能用 questionMapper.deleteBatchIds —— 因为全局 logic-delete-field=deleted
     * 配置会让 BaseMapper 的 delete 退化为 UPDATE deleted=1。这里通过自定义 SQL
     * 真正执行 DELETE FROM 物理删除。
     */
    private void archiveAndDelete(List<Long> ids) {
        if (ids.isEmpty()) {
            return;
        }
        // 1) 查出所有原始行（用物理查询，避免全局逻辑删除 deleted=0 过滤漏查已软删的行）
        //    collectDeleteIds 已通过 physicalSelect* 拿到 ids，这里逐个 physicalSelectById 即可，
        //    ids 数量通常 ≤ 几十，性能可接受
        List<Question> questions = new java.util.ArrayList<>();
        for (Long id : ids) {
            Question q = questionMapper.physicalSelectById(id);
            if (q != null) {
                questions.add(q);
            }
        }
        if (questions.isEmpty()) {
            return;
        }
        // 2) 转为归档实体并插入归档表（归档表只用作历史留痕，不做重复校验）
        LocalDateTime now = LocalDateTime.now();
        for (Question q : questions) {
            QuestionDeleted archive = toDeletedEntity(q, now);
            questionDeletedMapper.insert(archive);
        }
        // 3) 物理删除主表记录（绕过 BaseMapper 的逻辑删除，用原生 SQL）
        try {
            questionMapper.physicalDeleteByIds(ids);
        } catch (Throwable t) {
            log.error("物理删除 t_question 失败 ids={}", ids, t);
            throw new BusinessException("删除试题失败：" + t.getMessage());
        }
    }

    /**
     * 把 Question 实体转换为 QuestionDeleted 归档实体。
     */
    private static QuestionDeleted toDeletedEntity(Question q, LocalDateTime now) {
        QuestionDeleted d = new QuestionDeleted();
        // 归档表主键 id 自增；原 t_question ID 存到 question_id
        d.setId(null);
        d.setQuestionId(q.getId());
        d.setBizSection(q.getBizSection());
        d.setCategoryId(q.getCategoryId());
        d.setParentId(q.getParentId());
        d.setType(q.getType());
        d.setSubType(q.getSubType());
        d.setSort(q.getSort());
        d.setDifficulty(q.getDifficulty());
        d.setContent(q.getContent());
        d.setContentHash(q.getContentHash());
        d.setOptions(q.getOptions());
        d.setKnowledgeCode(q.getKnowledgeCode());
        d.setAbilityLevel(q.getAbilityLevel());
        d.setAbilityLevelAux(q.getAbilityLevelAux());
        d.setCoreLiteracy(q.getCoreLiteracy());
        d.setThemeContext(q.getThemeContext());
        d.setDifficultyP(q.getDifficultyP());
        d.setAnswer(q.getAnswer());
        d.setScore(q.getScore());
        d.setCourseStructure(q.getCourseStructure());
        d.setAnalysis(q.getAnalysis());
        d.setYear(q.getYear());
        d.setSource(q.getSource());
        d.setStatus(q.getStatus());
        d.setImportBatchId(q.getImportBatchId());
        d.setReviewerId(q.getReviewerId());
        d.setReviewRemark(q.getReviewRemark());
        d.setReviewedAt(q.getReviewedAt());
        d.setCreatedAt(q.getCreatedAt());
        d.setUpdatedAt(q.getUpdatedAt());
        d.setDeleted(1);
        d.setDeletedAt(now);
        return d;
    }

    // ==================== 2. 批量导入 ====================

    /**
     * Excel 批量导入
     * Excel 列约定（首行表头）：
     *   题型* | 子题型 | 题干* | 答案* | 解析 | 难度 | 分数 | 选项A-F | (分类ID | 年份 | 来源 | 排序 可选)
     *
     * <p>复合题采用父子行格式：
     * <ul>
     *   <li>父行：题型=复合题，题干=语段/材料（答案、选项可为空）</li>
     *   <li>子行：题型留空，子题型=单选题/多选题/简答题等（作为子题题型），题干=小题题干</li>
     *   <li>遇到下一个非空"题型"时结束当前复合题上下文</li>
     * </ul>
     */
    @Transactional(rollbackFor = Exception.class)
    public QuestionImportResultVO importExcel(MultipartFile file, QuestionImportDTO params, Long operatorId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传 Excel 文件");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !(originalName.toLowerCase().endsWith(".xlsx") || originalName.toLowerCase().endsWith(".xls"))) {
            throw new BusinessException(400, "仅支持 .xlsx / .xls 格式");
        }

        // 生成批次ID
        String batchId = IdUtil.fastSimpleUUID();

        List<QuestionImportResultVO.FailItem> failItems = new ArrayList<>();
        int successCount = 0;

        // 先落一条"处理中"的批次记录
        QuestionImportBatch batch = new QuestionImportBatch();
        batch.setBatchId(batchId);
        batch.setFileName(originalName);
        batch.setFileSize(file.getSize());
        batch.setStatus(0);
        batch.setBizSection(params.getBizSection());
        batch.setCategoryId(params.getCategoryId());
        batch.setYear(params.getYear());
        batch.setSource(params.getSource());
        batch.setCreatedBy(operatorId);
        batchMapper.insert(batch);

        // 解析 Excel
        List<Map<String, Object>> rows;
        try (InputStream in = file.getInputStream()) {
            ExcelReader reader = ExcelUtil.getReader(in);
            reader.addHeaderAlias("题型", "type");
            reader.addHeaderAlias("子题型", "subType");
            reader.addHeaderAlias("题干", "content");
            // 兼容两种表头命名："答案"(新模板) / "正确答案"(旧模板)
            reader.addHeaderAlias("答案", "answer");
            reader.addHeaderAlias("正确答案", "answer");
            // 兼容两种表头命名："分数"(新模板) / "分值"(旧模板)
            reader.addHeaderAlias("分数", "score");
            reader.addHeaderAlias("分值", "score");
            reader.addHeaderAlias("难度", "difficulty");
            reader.addHeaderAlias("解析", "analysis");
            reader.addHeaderAlias("选项A", "optA");
            reader.addHeaderAlias("选项B", "optB");
            reader.addHeaderAlias("选项C", "optC");
            reader.addHeaderAlias("选项D", "optD");
            reader.addHeaderAlias("选项E", "optE");
            reader.addHeaderAlias("选项F", "optF");
            // 新版 Excel 扩展字段（数学单招等真题）——旧 Excel 缺列时自动取 null，导入不报错
            reader.addHeaderAlias("课程结构", "courseStructure");
            reader.addHeaderAlias("知识点编码", "knowledgeCode");
            reader.addHeaderAlias("能力层级", "abilityLevel");
            reader.addHeaderAlias("辅助能力层级", "abilityLevelAux");
            reader.addHeaderAlias("核心素养", "coreLiteracy");
            reader.addHeaderAlias("主题语境", "themeContext");
            reader.addHeaderAlias("难度系数P", "difficultyP");
            reader.addHeaderAlias("分类ID", "categoryId");
            reader.addHeaderAlias("年份", "year");
            reader.addHeaderAlias("来源", "source");
            reader.addHeaderAlias("排序", "sort");
            rows = reader.readAll();
        } catch (IOException e) {
            log.error("Excel 解析失败", e);
            throw new BusinessException(400, "Excel 解析失败：" + e.getMessage());
        }

        int total = rows.size();
        // 本批次内已见 (content_hash, type, biz_section, category_id, year) 集合，
        // 避免同一 Excel 内重复行也全部入库
        java.util.Set<String> batchSeenKeys = new java.util.HashSet<>();
        int duplicateCount = 0;
        // 当前复合题父行 ID（0 表示不在复合题上下文中）；遇到下一个非空"题型"会重置
        long lastParentId = 0L;
        // 本批次成功导入的"大题"ID（独立题 + 复合题大题，parentId=0），用于自动归集到 t_paper
        List<Long> batchQuestionIds = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            int excelRow = i + 2; // 第 1 行表头
            Map<String, Object> row = rows.get(i);
            try {
                Question q = buildQuestionFromRow(row, params, lastParentId);
                // 查重：先算 hash，再查 DB 与本批次（5 项全等才算重复）
                String hash = computeContentHash(q.getContent(), q.getType());
                q.setContentHash(hash);
                String dedupKey = hash + "|" + q.getType()
                        + "|" + q.getBizSection()
                        + "|" + q.getCategoryId()
                        + "|" + q.getYear();
                if (batchSeenKeys.contains(dedupKey)) {
                    duplicateCount++;
                    throw new BusinessException("题干与本批次前序行重复（题型=" + typeText(q.getType())
                            + "，业务分区=" + q.getBizSection()
                            + "，分类ID=" + q.getCategoryId()
                            + "，年份=" + q.getYear() + "），已跳过");
                }
                Question existed = findDuplicate(hash, q.getType(), q.getBizSection(), q.getCategoryId(), q.getYear());
                if (existed != null) {
                    duplicateCount++;
                    throw new BusinessException("题干已存在，重复题 ID=" + existed.getId()
                            + "（题型=" + typeText(q.getType())
                            + "，业务分区=" + q.getBizSection()
                            + "，分类ID=" + q.getCategoryId()
                            + "，年份=" + q.getYear() + "），已跳过");
                }
                q.setImportBatchId(batchId);
                q.setStatus(params.getStatus() != null ? params.getStatus() : 1);
                questionMapper.insert(q);
                // 维护复合题上下文：父行后更新 lastParentId；独立行重置；子行保持
                if (q.getType() != null && q.getType() == 7) {
                    lastParentId = q.getId();
                } else if (q.getParentId() == null || q.getParentId() == 0L) {
                    lastParentId = 0L;
                }
                // 仅收录大题（parentId=0）入试卷组成；子题不单独列入
                if (q.getParentId() == null || q.getParentId() == 0L) {
                    batchQuestionIds.add(q.getId());
                }
                batchSeenKeys.add(dedupKey);
                successCount++;
            } catch (Exception e) {
                failItems.add(new QuestionImportResultVO.FailItem(excelRow, e.getMessage()));
                log.warn("Excel 第 {} 行导入失败: {}", excelRow, e.getMessage());
            }
        }

        int failCount = failItems.size();
        batch.setTotalCount(total);
        batch.setSuccessCount(successCount);
        batch.setFailCount(failCount);
        batch.setStatus(failCount == 0 ? 1 : (successCount == 0 ? 3 : 2));
        // 失败明细存 JSON（仅保留前 100 条避免过长）
        List<QuestionImportResultVO.FailItem> saved = failItems.size() > 100
                ? failItems.subList(0, 100) : failItems;
        if (!saved.isEmpty()) {
            batch.setFailDetail(toFailJson(saved));
        }
        batchMapper.updateById(batch);

        invalidateCache();

        // 自动归集到 t_paper：仅当批次提供了 year 与 categoryId（能唯一定位一套真卷）时触发
        if (params.getYear() != null && params.getCategoryId() != null && !batchQuestionIds.isEmpty()) {
            try {
                Paper paper = paperService.upsertByBatch(
                        params.getBizSection() == null ? 1 : params.getBizSection(),
                        params.getCategoryId(),
                        params.getYear(),
                        params.getSource(),
                        batchQuestionIds);
                if (paper != null) {
                    log.info("批次 {} 自动归集试卷成功 paperId={} questionCount={}",
                            batchId, paper.getId(), batchQuestionIds.size());
                }
            } catch (Exception e) {
                // 试卷归集失败不阻断导入主流程，仅记日志
                log.error("批次 {} 自动归集试卷失败: {}", batchId, e.getMessage(), e);
            }
        }

        QuestionImportResultVO vo = new QuestionImportResultVO();
        vo.setBatchId(batchId);
        vo.setTotalCount(total);
        vo.setSuccessCount(successCount);
        vo.setFailCount(failCount);
        vo.setDuplicateCount(duplicateCount);
        vo.setFailItems(failItems);
        return vo;
    }

    /**
     * 批次列表分页查询
     */
    public PageResult<QuestionImportBatch> listBatches(QuestionImportBatchQueryDTO query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int size = query.getSize() == null ? 10 : query.getSize();
        Page<QuestionImportBatch> p = new Page<>(page, size);
        LambdaQueryWrapper<QuestionImportBatch> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(QuestionImportBatch::getFileName, kw)
                    .or().eq(QuestionImportBatch::getBatchId, kw));
        }
        if (query.getStatus() != null) {
            wrapper.eq(QuestionImportBatch::getStatus, query.getStatus());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(QuestionImportBatch::getCategoryId, query.getCategoryId());
        }
        wrapper.orderByDesc(QuestionImportBatch::getCreatedAt);
        IPage<QuestionImportBatch> result = batchMapper.selectPage(p, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    /**
     * 批次详情
     */
    public QuestionImportBatch getBatch(String batchId) {
        LambdaQueryWrapper<QuestionImportBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionImportBatch::getBatchId, batchId);
        return batchMapper.selectOne(wrapper);
    }

    // ==================== 3. 试题校对 ====================

    /**
     * 校对：分页查询题目列表（复用 listQuestions）。
     * status 不传时查全部状态；传具体值则按该状态过滤。
     * 默认查"待校对"由前端控制（query.status 默认值 0），后端不强制设默认值。
     */
    public PageResult<Question> listForProofread(QuestionQueryDTO query) {
        return listQuestions(query);
    }

    /**
     * 校对保存：仅修正 content/options/answer/analysis 等字段，不改状态
     */
    public boolean proofreadSave(QuestionCreateDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(400, "校对保存时 id 不能为空");
        }
        Question existed = questionMapper.selectById(dto.getId());
        if (existed == null) {
            throw new BusinessException(404, "题目不存在");
        }
        Question q = new Question();
        q.setId(dto.getId());
        if (StrUtil.isNotBlank(dto.getContent())) q.setContent(dto.getContent());
        if (CollUtil.isNotEmpty(dto.getOptions())) q.setOptions(toOptionsJson(dto.getOptions()));
        if (StrUtil.isNotBlank(dto.getAnswer())) q.setAnswer(dto.getAnswer());
        if (StrUtil.isNotBlank(dto.getAnalysis())) q.setAnalysis(dto.getAnalysis());
        if (dto.getScore() != null) q.setScore(dto.getScore());
        if (dto.getDifficulty() != null) q.setDifficulty(dto.getDifficulty());
        questionMapper.updateById(q);
        redisUtil.delete(CacheConstants.QUESTION_DETAIL_PREFIX + dto.getId());
        return true;
    }

    // ==================== 4. 试题编辑 ====================

    /**
     * 编辑：分页查询（不限状态）
     */
    public PageResult<Question> listForEdit(QuestionQueryDTO query) {
        return listQuestions(query);
    }

    // ==================== 5. 试题审核 ====================

    /**
     * 审核：批量通过 / 驳回
     */
    @Transactional(rollbackFor = Exception.class)
    public int review(QuestionReviewDTO dto, Long reviewerId) {
        if (dto.getStatus() != 2 && dto.getStatus() != 3) {
            throw new BusinessException(400, "审核结果非法，仅支持 2通过 / 3驳回");
        }
        List<Long> ids = dto.getIds();
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        int affected = 0;
        LocalDateTime now = LocalDateTime.now();
        for (Long id : ids) {
            Question q = new Question();
            q.setId(id);
            q.setStatus(dto.getStatus());
            q.setReviewerId(reviewerId);
            q.setReviewRemark(dto.getRemark());
            q.setReviewedAt(now);
            affected += questionMapper.updateById(q);
            redisUtil.delete(CacheConstants.QUESTION_DETAIL_PREFIX + id);
        }
        invalidateCache();
        log.info("审核完成 reviewerId={}, status={}, affected={}", reviewerId, dto.getStatus(), affected);
        return affected;
    }

    // ==================== 统计 ====================

    /**
     * 题目详情（管理后台直查 DB，不走缓存，保证最新数据）
     */
    public Question detail(Long id) {
        return questionMapper.selectById(id);
    }

    /**
     * 查询复合题的子题列表（按 sort 升序，带 Redis 缓存）
     */
    public List<Question> listChildren(Long parentId) {
        if (parentId == null || parentId <= 0) {
            return new ArrayList<>();
        }
        String cacheKey = CacheConstants.QUESTION_CHILDREN_PREFIX + parentId;
        List<Question> cached = redisUtil.getList(cacheKey);
        if (cached != null) {
            return cached;
        }
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getParentId, parentId)
               .orderByAsc(Question::getSort)
               .orderByAsc(Question::getId);
        List<Question> list = questionMapper.selectList(wrapper);
        redisUtil.setList(cacheKey, list, CacheConstants.TTL_QUESTION_CHILDREN);
        return list;
    }

    public QuestionProofreadStatVO stat() {
        QuestionProofreadStatVO vo = new QuestionProofreadStatVO();
        vo.setDraft(countByStatus(0));
        vo.setPendingReview(countByStatus(1));
        vo.setApproved(countByStatus(2));
        vo.setRejected(countByStatus(3));
        // 待校对：status=0（草稿态）且仅限主题（排除子题）。与 proofread.vue 列表默认查询条件一致。
        vo.setPendingProofread(countByStatus(0));
        vo.setTotal(vo.getDraft() + vo.getPendingReview() + vo.getApproved() + vo.getRejected());
        return vo;
    }

    // ==================== 内部方法 ====================

    private Long countByStatus(int status) {
        // 仅统计主题（parent_id=0），子题不计入总数
        return questionMapper.selectCount(new LambdaQueryWrapper<Question>()
                .eq(Question::getStatus, status)
                .eq(Question::getParentId, 0L));
    }

    /**
     * 复用 QuestionService 的查询逻辑：从 DB 查询（不走缓存，避免校对/审核界面看到旧数据）
     */
    private PageResult<Question> listQuestions(QuestionQueryDTO query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int size = query.getSize() == null ? 10 : query.getSize();
        Page<Question> p = new Page<>(page, size);
        IPage<Question> result = questionMapper.selectQuestionPage(p, query);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    private void invalidateCache() {
        // 管理端题目列表 + 小程序练习整卷缓存都需清理
        // 否则题目状态被改成待审核/驳回后，小程序仍可能命中旧缓存读到非已通过试题
        redisUtil.deleteByPrefix(CacheConstants.QUESTION_LIST_PREFIX);
        redisUtil.deleteByPrefix(CacheConstants.PRACTICE_LIST_PREFIX);
    }

    private void validateQuestionDto(QuestionCreateDTO dto, boolean isCreate) {
        if (isCreate) {
            if (dto.getType() == null) throw new BusinessException(400, "题型不能为空");
            if (StrUtil.isBlank(dto.getContent())) throw new BusinessException(400, "题干不能为空");
            if (dto.getBizSection() == null) dto.setBizSection(1);
            if (dto.getParentId() == null) dto.setParentId(0L);
            if (dto.getDifficulty() == null) dto.setDifficulty(1);
            if (dto.getSort() == null) dto.setSort(0);
        }
    }

    private Question convertToEntity(QuestionCreateDTO dto) {
        Question q = new Question();
        q.setId(dto.getId());
        q.setBizSection(dto.getBizSection());
        q.setCategoryId(dto.getCategoryId());
        q.setParentId(dto.getParentId());
        q.setType(dto.getType());
        q.setSubType(dto.getSubType());
        q.setSort(dto.getSort());
        q.setDifficulty(dto.getDifficulty());
        q.setContent(dto.getContent());
        if (CollUtil.isNotEmpty(dto.getOptions())) {
            q.setOptions(toOptionsJson(dto.getOptions()));
        }
        q.setAnswer(dto.getAnswer());
        q.setScore(dto.getScore());
        q.setAnalysis(dto.getAnalysis());
        q.setYear(dto.getYear());
        q.setSource(dto.getSource());
        q.setStatus(dto.getStatus());
        return q;
    }

    private String toOptionsJson(List<String> options) {
        // 直接拼装 JSON 字符串，避免引入额外依赖
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < options.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escape(options.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * 解析题型编码：支持中文名称（单选题/多选题/.../复合题）和数字（1-7）；非法返回 null
     */
    private Integer parseTypeCode(String s) {
        if (s == null) return null;
        String t = s.trim();
        Integer code = TYPE_NAME_MAP.get(t);
        if (code != null) return code;
        try {
            int n = Integer.parseInt(t.replaceAll("\\.0$", ""));
            if (n >= 1 && n <= 7) return n;
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    /**
     * 从 Excel 行构造 Question，支持三种角色：
     * 1) 复合题父行：题型=复合题，题干=语段，答案/选项可空
     * 2) 复合题子行：题型留空，子题型=单选题/简答题等（作为子题题型），parentId=lastParentId
     * 3) 独立行：题型为非"复合题"的非空值，沿用原有逻辑
     */
    private Question buildQuestionFromRow(Map<String, Object> row, QuestionImportDTO params, long lastParentId) {
        Question q = new Question();

        Object typeVal = row.get("type");
        Object subTypeVal = row.get("subType");
        String typeStr = typeVal == null ? "" : String.valueOf(typeVal).trim();
        String subTypeStr = subTypeVal == null ? "" : String.valueOf(subTypeVal).trim();

        boolean isCompositeParent = StrUtil.isNotBlank(typeStr) && parseTypeCode(typeStr) != null && parseTypeCode(typeStr) == 7;
        boolean isCompositeChild = StrUtil.isBlank(typeStr) && StrUtil.isNotBlank(subTypeStr);

        // 1) 确定题型与 parentId
        Integer typeCode;
        if (isCompositeParent) {
            typeCode = 7;
            q.setParentId(0L);
        } else if (isCompositeChild) {
            typeCode = parseTypeCode(subTypeStr);
            if (typeCode == null) {
                throw new BusinessException("子题型不是合法的题型名称：" + subTypeStr + "（应为单选题/多选题/判断题/填空题/简答题/计算题等）");
            }
            if (typeCode == 7) {
                throw new BusinessException("子题型不能为复合题");
            }
            if (lastParentId == 0L) {
                throw new BusinessException("子题型行缺少父复合题（请确保前一行 题型=复合题）");
            }
            q.setParentId(lastParentId);
        } else {
            if (StrUtil.isBlank(typeStr)) {
                throw new BusinessException("题型不能为空（若为复合题子题，请在「子题型」列填写单选题/简答题等）");
            }
            typeCode = parseTypeCode(typeStr);
            if (typeCode == null) {
                throw new BusinessException("题型非法：" + typeStr);
            }
            q.setParentId(0L);
        }
        q.setType(typeCode);

        // 2) 题干：必填
        Object contentVal = row.get("content");
        if (contentVal == null || StrUtil.isBlank(String.valueOf(contentVal))) {
            throw new BusinessException("题干不能为空");
        }
        q.setContent(String.valueOf(contentVal).trim());

        // 3) 选项：选择题(1,2,3)必填；非选择题若填了也保留；复合题父行不校验
        List<String> opts = new ArrayList<>();
        for (String key : Arrays.asList("optA", "optB", "optC", "optD", "optE", "optF")) {
            Object v = row.get(key);
            if (v != null && StrUtil.isNotBlank(String.valueOf(v))) {
                opts.add(String.valueOf(v).trim());
            }
        }
        if (typeCode == 1 || typeCode == 2 || typeCode == 3) {
            if (opts.isEmpty()) {
                throw new BusinessException("选择题必须填写至少一个选项");
            }
            q.setOptions(toOptionsJson(opts));
        } else if (!opts.isEmpty()) {
            q.setOptions(toOptionsJson(opts));
        }

        // 4) 答案：复合题父行可空，其余必填
        Object answerVal = row.get("answer");
        if (isCompositeParent) {
            if (answerVal != null && StrUtil.isNotBlank(String.valueOf(answerVal))) {
                q.setAnswer(String.valueOf(answerVal).trim());
            }
        } else {
            if (answerVal == null || StrUtil.isBlank(String.valueOf(answerVal))) {
                throw new BusinessException("正确答案不能为空");
            }
            q.setAnswer(String.valueOf(answerVal).trim());
        }

        // 5) 分数
        Object scoreVal = row.get("score");
        if (scoreVal != null && StrUtil.isNotBlank(String.valueOf(scoreVal))) {
            try {
                q.setScore(new BigDecimal(String.valueOf(scoreVal).trim()));
            } catch (NumberFormatException ignored) {
                throw new BusinessException("分数格式非法：" + scoreVal);
            }
        }

        // 6) 难度
        Object diffVal = row.get("difficulty");
        if (diffVal != null && StrUtil.isNotBlank(String.valueOf(diffVal))) {
            Integer dCode = DIFFICULTY_NAME_MAP.get(String.valueOf(diffVal).trim());
            if (dCode == null) {
                try {
                    dCode = Integer.parseInt(String.valueOf(diffVal).trim());
                } catch (NumberFormatException ignored) {
                    throw new BusinessException("难度非法：" + diffVal);
                }
            }
            q.setDifficulty(dCode);
        } else {
            q.setDifficulty(2);
        }

        // 7) 解析
        Object analysisVal = row.get("analysis");
        if (analysisVal != null && StrUtil.isNotBlank(String.valueOf(analysisVal))) {
            q.setAnalysis(String.valueOf(analysisVal).trim());
        }

        // 8) 子题型描述：复合题子行的"子题型"列已被用作题型，不再写入 sub_type；其余情况保留原值
        if (!isCompositeChild) {
            if (subTypeVal != null && StrUtil.isNotBlank(String.valueOf(subTypeVal))) {
                q.setSubType(String.valueOf(subTypeVal).trim());
            }
        }

        // 9) 分类ID（Excel 可选；为空时用表单默认）
        Object categoryVal = row.get("categoryId");
        Long categoryId = null;
        if (categoryVal != null && StrUtil.isNotBlank(String.valueOf(categoryVal))) {
            try {
                // 处理 Excel 数字以浮点形式读取的情况
                String cs = String.valueOf(categoryVal).trim().replaceAll("\\.0$", "");
                categoryId = Long.parseLong(cs);
            } catch (NumberFormatException ignored) {
                throw new BusinessException("分类ID格式非法：" + categoryVal);
            }
        }
        if (categoryId == null) {
            categoryId = params.getCategoryId();
        }
        if (categoryId == null) {
            throw new BusinessException("分类ID不能为空（Excel 或参数至少提供一项）");
        }
        q.setCategoryId(categoryId);

        // 10) 年份
        Object yearVal = row.get("year");
        Integer year = params.getYear();
        if (yearVal != null && StrUtil.isNotBlank(String.valueOf(yearVal))) {
            try {
                year = Integer.parseInt(String.valueOf(yearVal).trim().replaceAll("\\.0$", ""));
            } catch (NumberFormatException ignored) {
                throw new BusinessException("年份格式非法：" + yearVal);
            }
        }
        q.setYear(year);

        // 11) 来源
        Object sourceVal = row.get("source");
        String source = params.getSource();
        if (sourceVal != null && StrUtil.isNotBlank(String.valueOf(sourceVal))) {
            source = String.valueOf(sourceVal).trim();
        }
        q.setSource(source);

        // 12) 排序
        Object sortVal = row.get("sort");
        int sort = 0;
        if (sortVal != null && StrUtil.isNotBlank(String.valueOf(sortVal))) {
            try {
                sort = Integer.parseInt(String.valueOf(sortVal).trim().replaceAll("\\.0$", ""));
            } catch (NumberFormatException ignored) {
                // 保持 0
            }
        }
        q.setSort(sort);

        q.setBizSection(params.getBizSection() != null ? params.getBizSection() : 1);

        // 13) 新版 Excel 扩展字段（旧 Excel 缺列时为空字符串，统一转 null）
        q.setCourseStructure(readStr(row, "courseStructure"));
        q.setKnowledgeCode(readStr(row, "knowledgeCode"));
        q.setAbilityLevel(readStr(row, "abilityLevel"));
        q.setAbilityLevelAux(readStr(row, "abilityLevelAux"));
        q.setCoreLiteracy(readStr(row, "coreLiteracy"));
        q.setThemeContext(readStr(row, "themeContext"));

        // 难度系数P：0~1 之间的小数；非数字或越界时忽略（保留 null）
        Object dpVal = row.get("difficultyP");
        if (dpVal != null && StrUtil.isNotBlank(String.valueOf(dpVal))) {
            try {
                BigDecimal dp = new BigDecimal(String.valueOf(dpVal).trim());
                if (dp.compareTo(BigDecimal.ZERO) >= 0 && dp.compareTo(BigDecimal.ONE) <= 0) {
                    q.setDifficultyP(dp);
                }
            } catch (NumberFormatException ignored) {
                // 非法值忽略
            }
        }
        return q;
    }

    /** 从 row 中读字符串字段，空值统一返回 null（避免 MyBatis-Plus 插入 "" 占用存储） */
    private static String readStr(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    private String toFailJson(List<QuestionImportResultVO.FailItem> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            QuestionImportResultVO.FailItem it = items.get(i);
            sb.append("{\"row\":").append(it.getRow())
                    .append(",\"msg\":\"").append(escape(it.getMsg())).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    /** 题型编码 → 文本（错误提示用） */
    private static String typeText(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "单选题";
            case 2: return "多选题";
            case 3: return "判断题";
            case 4: return "填空题";
            case 5: return "简答题";
            case 6: return "计算题";
            case 7: return "复合题";
            default: return "未知(" + type + ")";
        }
    }

    /**
     * 计算题干归一化后的 SHA-1 短哈希(前16位)
     * 归一化规则：去除首尾空白 → 转小写 → 折叠连续空白为单空格
     * 与 migration_v4 回填公式一致：SHA1(LOWER(TRIM(content)) | type) 前 16 位
     *
     * @param content 原始题干
     * @param type    题型编码
     */
    private static String computeContentHash(String content, Integer type) {
        if (content == null) content = "";
        // 折叠连续空白并去掉首尾
        String normalized = content.trim().toLowerCase().replaceAll("\\s+", " ");
        String input = normalized + "|" + (type == null ? 0 : type);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                String h = Integer.toHexString(b & 0xFF);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            // SHA-1 是 JDK 必备算法，不会缺失
            throw new IllegalStateException("SHA-1 not available", e);
        }
    }

    /**
     * 查重：在未删除题目中，命中 (content_hash, type, biz_section, category_id, year) 全部一致的记录。
     * 说明：content_hash 已对"题干+题型"做归一化指纹，再加 业务分区/分类/年份 三个字段直等过滤，
     * 即 5 个条件全等才算重复（与 uk_hash_type_section_cat_year_dep 唯一索引一致）。
     * 注：year 可能为 NULL，使用 isNull 处理，避免 SQL 中 = NULL 永真为假而漏判。
     *
     * @return 已存在的 Question，无则 null
     */
    private Question findDuplicate(String contentHash, Integer type, Integer bizSection, Long categoryId, Integer year) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getContentHash, contentHash)
               .eq(Question::getType, type)
               .eq(Question::getBizSection, bizSection)
               .eq(Question::getCategoryId, categoryId);
        if (year == null) {
            wrapper.isNull(Question::getYear);
        } else {
            wrapper.eq(Question::getYear, year);
        }
        wrapper.last("LIMIT 1");
        return questionMapper.selectOne(wrapper);
    }
}
