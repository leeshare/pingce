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
import com.shaanxi.zhiping.entity.QuestionImportBatch;
import com.shaanxi.zhiping.exception.BusinessException;
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
    private QuestionImportBatchMapper batchMapper;

    @Resource
    private RedisUtil redisUtil;

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
        // 查重：同一 (content_hash, type) 且未删除的题已存在则拒绝录入
        String hash = computeContentHash(q.getContent(), q.getType());
        q.setContentHash(hash);
        Question existed = findDuplicate(hash, q.getType());
        if (existed != null) {
            throw new BusinessException(409, "题干与现有题目重复，已存在题 ID=" + existed.getId()
                    + "（题型=" + typeText(q.getType()) + "）");
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
        // 题干或题型变更 → 重算 hash，并校验与其他题不冲突
        boolean contentChanged = q.getContent() != null && !q.getContent().equals(existed.getContent());
        boolean typeChanged = q.getType() != null && !q.getType().equals(existed.getType());
        if (contentChanged || typeChanged) {
            String newContent = q.getContent() != null ? q.getContent() : existed.getContent();
            Integer newType = q.getType() != null ? q.getType() : existed.getType();
            String hash = computeContentHash(newContent, newType);
            Question dup = findDuplicate(hash, newType);
            if (dup != null && !dup.getId().equals(dto.getId())) {
                throw new BusinessException(409, "修改后题干与其他题目重复，冲突题 ID=" + dup.getId());
            }
            q.setContentHash(hash);
        }
        questionMapper.updateById(q);
        invalidateCache();
        log.info("更新题目成功 id={}", q.getId());
        return true;
    }

    /**
     * 删除题目
     */
    public boolean delete(Long id) {
        boolean ok = questionMapper.deleteById(id) > 0;
        if (ok) {
            redisUtil.delete(CacheConstants.QUESTION_DETAIL_PREFIX + id);
            redisUtil.delete(CacheConstants.QUESTION_CHILDREN_PREFIX + id);
            invalidateCache();
        }
        return ok;
    }

    /**
     * 批量删除
     */
    public int deleteBatch(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        int rows = questionMapper.deleteBatchIds(ids);
        for (Long id : ids) {
            redisUtil.delete(CacheConstants.QUESTION_DETAIL_PREFIX + id);
            redisUtil.delete(CacheConstants.QUESTION_CHILDREN_PREFIX + id);
        }
        invalidateCache();
        return rows;
    }

    // ==================== 2. 批量导入 ====================

    /**
     * Excel 批量导入
     * Excel 列约定（首行表头）：
     *   题型* | 题干* | 选项A | 选项B | 选项C | 选项D | 选项E | 选项F | 正确答案* | 分值 | 难度 | 解析 | 子题型 | 分类ID | 年份 | 来源 | 排序
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
            reader.addHeaderAlias("题干", "content");
            reader.addHeaderAlias("选项A", "optA");
            reader.addHeaderAlias("选项B", "optB");
            reader.addHeaderAlias("选项C", "optC");
            reader.addHeaderAlias("选项D", "optD");
            reader.addHeaderAlias("选项E", "optE");
            reader.addHeaderAlias("选项F", "optF");
            reader.addHeaderAlias("正确答案", "answer");
            reader.addHeaderAlias("分值", "score");
            reader.addHeaderAlias("难度", "difficulty");
            reader.addHeaderAlias("解析", "analysis");
            reader.addHeaderAlias("子题型", "subType");
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
        // 本批次内已见 (content_hash, type) 集合，避免同一 Excel 内重复行也全部入库
        java.util.Set<String> batchSeenKeys = new java.util.HashSet<>();
        int duplicateCount = 0;
        for (int i = 0; i < total; i++) {
            int excelRow = i + 2; // 第 1 行表头
            Map<String, Object> row = rows.get(i);
            try {
                Question q = buildQuestionFromRow(row, params);
                // 查重：先算 hash，再查 DB 与本批次
                String hash = computeContentHash(q.getContent(), q.getType());
                q.setContentHash(hash);
                String dedupKey = hash + "|" + q.getType();
                if (batchSeenKeys.contains(dedupKey)) {
                    duplicateCount++;
                    throw new BusinessException("题干与本批次前序行重复（题型=" + typeText(q.getType()) + "），已跳过");
                }
                Question existed = findDuplicate(hash, q.getType());
                if (existed != null) {
                    duplicateCount++;
                    throw new BusinessException("题干已存在，重复题 ID=" + existed.getId()
                            + "（题型=" + typeText(q.getType()) + "），已跳过");
                }
                q.setImportBatchId(batchId);
                q.setStatus(params.getStatus() != null ? params.getStatus() : 1);
                questionMapper.insert(q);
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
        redisUtil.deleteByPrefix(CacheConstants.QUESTION_LIST_PREFIX);
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

    private Question buildQuestionFromRow(Map<String, Object> row, QuestionImportDTO params) {
        Question q = new Question();
        Object typeVal = row.get("type");
        if (typeVal == null || StrUtil.isBlank(String.valueOf(typeVal))) {
            throw new BusinessException("题型不能为空");
        }
        Integer typeCode = TYPE_NAME_MAP.get(String.valueOf(typeVal).trim());
        if (typeCode == null) {
            // 支持数字
            try {
                typeCode = Integer.parseInt(String.valueOf(typeVal).trim());
            } catch (NumberFormatException ignored) {
                throw new BusinessException("题型非法：" + typeVal);
            }
            if (typeCode < 1 || typeCode > 7) {
                throw new BusinessException("题型编码超出范围(1-7)：" + typeVal);
            }
        }
        q.setType(typeCode);

        Object contentVal = row.get("content");
        if (contentVal == null || StrUtil.isBlank(String.valueOf(contentVal))) {
            throw new BusinessException("题干不能为空");
        }
        q.setContent(String.valueOf(contentVal).trim());

        // 选项：选择题必填
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
        }

        Object answerVal = row.get("answer");
        if (answerVal == null || StrUtil.isBlank(String.valueOf(answerVal))) {
            throw new BusinessException("正确答案不能为空");
        }
        q.setAnswer(String.valueOf(answerVal).trim());

        Object scoreVal = row.get("score");
        if (scoreVal != null && StrUtil.isNotBlank(String.valueOf(scoreVal))) {
            try {
                q.setScore(new BigDecimal(String.valueOf(scoreVal).trim()));
            } catch (NumberFormatException ignored) {
                throw new BusinessException("分值格式非法：" + scoreVal);
            }
        }

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

        Object analysisVal = row.get("analysis");
        if (analysisVal != null && StrUtil.isNotBlank(String.valueOf(analysisVal))) {
            q.setAnalysis(String.valueOf(analysisVal).trim());
        }

        Object subTypeVal = row.get("subType");
        if (subTypeVal != null && StrUtil.isNotBlank(String.valueOf(subTypeVal))) {
            q.setSubType(String.valueOf(subTypeVal).trim());
        }

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

        Object sourceVal = row.get("source");
        String source = params.getSource();
        if (sourceVal != null && StrUtil.isNotBlank(String.valueOf(sourceVal))) {
            source = String.valueOf(sourceVal).trim();
        }
        q.setSource(source);

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
        q.setParentId(0L);
        return q;
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
     * 查重：在未删除题目中，查找 (content_hash, type) 命中的记录
     *
     * @return 已存在的 Question，无则 null
     */
    private Question findDuplicate(String contentHash, Integer type) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getContentHash, contentHash)
               .eq(Question::getType, type)
               .last("LIMIT 1");
        return questionMapper.selectOne(wrapper);
    }
}
