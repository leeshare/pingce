package com.shaanxi.zhiping.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shaanxi.zhiping.common.CacheConstants;
import com.shaanxi.zhiping.common.PageResult;
import com.shaanxi.zhiping.dto.PaperQueryDTO;
import com.shaanxi.zhiping.entity.Paper;
import com.shaanxi.zhiping.entity.Question;
import com.shaanxi.zhiping.mapper.PaperMapper;
import com.shaanxi.zhiping.mapper.QuestionMapper;
import com.shaanxi.zhiping.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 试卷 Service（集成 Redis 缓存）
 *
 * 缓存策略：
 * - 试卷详情：缓存 30 分钟，更新/删除时清除单条缓存
 */
@Slf4j
@Service
public class PaperService {

    @Resource
    private PaperMapper paperMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 分页查询试卷
     */
    public PageResult<Paper> listPapers(PaperQueryDTO query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int size = query.getSize() == null ? 10 : query.getSize();
        Page<Paper> p = new Page<>(page, size);
        IPage<Paper> result = paperMapper.selectPaperPage(p, query);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    /**
     * 试卷详情（带缓存）
     */
    public Paper detail(Long id) {
        String cacheKey = CacheConstants.PAPER_DETAIL_PREFIX + id;
        Paper cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("试卷详情命中缓存 id={}", id);
            return cached;
        }
        Paper paper = paperMapper.selectById(id);
        if (paper != null) {
            redisUtil.set(cacheKey, paper, CacheConstants.TTL_PAPER_DETAIL);
        }
        return paper;
    }

    /**
     * 试卷预览：返回试卷元数据 + 按 question_ids 顺序的题目列表。
     * 复合题(type=7)大题后紧跟其子题（parent_id=大题id，按 sort、id 升序）。
     * 已物理删除的题目自动跳过。
     */
    public Map<String, Object> preview(Long id) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("paper", paper);

        List<Question> ordered = new ArrayList<>();
        String idsStr = paper.getQuestionIds();
        if (idsStr != null && !idsStr.isEmpty()) {
            // 1) 解析题目 ID（保持原顺序、去重）
            LinkedHashSet<Long> idSet = new LinkedHashSet<>();
            for (String s : idsStr.split(",")) {
                if (s == null || s.trim().isEmpty()) continue;
                try {
                    idSet.add(Long.parseLong(s.trim()));
                } catch (NumberFormatException ignore) {
                }
            }
            if (!idSet.isEmpty()) {
                // 2) 批量拉取（selectBatchIds；Question 已移除 @TableLogic，物理删除的行自然查不到）
                List<Long> ids = new ArrayList<>(idSet);
                List<Question> mains = questionMapper.selectBatchIds(ids);
                Map<Long, Question> mainMap = mains.stream()
                        .collect(Collectors.toMap(Question::getId, q -> q, (a, b) -> a));

                // 3) 按 question_ids 原顺序输出大题/独立题，并收集复合题大题 ID
                List<Long> compositeIds = new ArrayList<>();
                for (Long qid : ids) {
                    Question q = mainMap.get(qid);
                    if (q == null) continue; // 已物理删除，跳过
                    ordered.add(q);
                    if (q.getType() != null && q.getType() == 7
                            && q.getParentId() != null && q.getParentId() == 0L) {
                        compositeIds.add(q.getId());
                    }
                }

                // 4) 一次性拉取所有复合题子题，按 (parentId, sort, id) 排序
                if (!compositeIds.isEmpty()) {
                    List<Question> children = questionMapper.selectList(
                            new LambdaQueryWrapper<Question>()
                                    .in(Question::getParentId, compositeIds)
                                    .orderByAsc(Question::getParentId)
                                    .orderByAsc(Question::getSort)
                                    .orderByAsc(Question::getId));
                    Map<Long, List<Question>> childMap = children.stream()
                            .collect(Collectors.groupingBy(Question::getParentId));

                    // 5) 组装：每个大题后紧跟其子题
                    List<Question> flattened = new ArrayList<>();
                    for (Question q : ordered) {
                        flattened.add(q);
                        List<Question> kids = childMap.get(q.getId());
                        if (kids != null) flattened.addAll(kids);
                    }
                    ordered = flattened;
                }
            }
        }
        result.put("questions", ordered);
        return result;
    }

    /**
     * 新增试卷
     */
    public Long create(Paper paper) {
        paperMapper.insert(paper);
        return paper.getId();
    }

    /**
     * 更新试卷
     */
    public boolean update(Paper paper) {
        boolean ok = paperMapper.updateById(paper) > 0;
        if (ok) {
            redisUtil.delete(CacheConstants.PAPER_DETAIL_PREFIX + paper.getId());
        }
        return ok;
    }

    /**
     * 删除试卷
     */
    public boolean delete(Long id) {
        boolean ok = paperMapper.deleteById(id) > 0;
        if (ok) {
            redisUtil.delete(CacheConstants.PAPER_DETAIL_PREFIX + id);
        }
        return ok;
    }

    /**
     * 切换发布状态（0草稿 1已发布），同时清缓存
     */
    public boolean updateStatus(Long id, Integer status) {
        Paper patch = new Paper();
        patch.setId(id);
        patch.setStatus(status);
        boolean ok = paperMapper.updateById(patch) > 0;
        if (ok) {
            redisUtil.delete(CacheConstants.PAPER_DETAIL_PREFIX + id);
        }
        return ok;
    }

    /**
     * 反查引用了给定任一题目 ID 的试卷列表。
     * 用于"删除试题"前的引用校验：试题被任何试卷引用时禁止删除。
     */
    public List<Paper> findPapersReferencing(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return paperMapper.selectByQuestionIdIn(questionIds);
    }

    /**
     * 批量导入后，按 (bizSection, categoryId, year, source) 维度维护一套试卷：
     * - 不存在则新增一条草稿试卷（默认时长 90 / 总分 100 / 及格 60，管理员可后续在「真卷维护」里调整）
     * - 已存在则把本次新增的题目 ID 追加到 question_ids（去重，保持升序）
     * - 卷名自动生成：{source}-{year}-{categoryId}（source 缺省时用 "真题"）
     *
     * 注意：questionIds 仅收录"大题"ID（独立题 + 复合题大题，即 parentId=0），子题不单独列入试卷组成。
     *
     * @return 维护后的试卷对象；若本次 questionIds 为空则返回 null
     */
    public Paper upsertByBatch(Integer bizSection, Long categoryId, Integer year, String source, List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return null;
        }
        // 去重升序
        LinkedHashSet<Long> dedup = new LinkedHashSet<>();
        questionIds.stream().filter(java.util.Objects::nonNull).sorted().forEach(dedup::add);
        if (dedup.isEmpty()) {
            return null;
        }
        String newIdsStr = joinIds(dedup);

        // 按 4 维 key 查找已存在试卷
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<Paper>()
                .eq(Paper::getBizSection, bizSection)
                .eq(Paper::getCategoryId, categoryId)
                .eq(Paper::getYear, year);
        if (source == null || source.isEmpty()) {
            wrapper.and(w -> w.isNull(Paper::getSource).or().eq(Paper::getSource, ""));
        } else {
            wrapper.eq(Paper::getSource, source);
        }
        wrapper.orderByAsc(Paper::getId);
        List<Paper> existed = paperMapper.selectList(wrapper);

        if (!existed.isEmpty()) {
            Paper paper = existed.get(0);
            // 合并旧 ID + 新 ID，去重升序
            LinkedHashSet<Long> merged = new LinkedHashSet<>();
            if (paper.getQuestionIds() != null && !paper.getQuestionIds().isEmpty()) {
                for (String s : paper.getQuestionIds().split(",")) {
                    if (s != null && !s.trim().isEmpty()) {
                        try {
                            merged.add(Long.parseLong(s.trim()));
                        } catch (NumberFormatException ignore) {
                        }
                    }
                }
            }
            merged.addAll(dedup);
            Paper patch = new Paper();
            patch.setId(paper.getId());
            patch.setQuestionIds(joinIds(merged));
            paperMapper.updateById(patch);
            redisUtil.delete(CacheConstants.PAPER_DETAIL_PREFIX + paper.getId());
            paper.setQuestionIds(patch.getQuestionIds());
            return paper;
        }

        // 新建草稿试卷
        Paper paper = new Paper();
        paper.setBizSection(bizSection == null ? 1 : bizSection);
        paper.setCategoryId(categoryId);
        paper.setYear(year);
        paper.setSource(source);
        paper.setTitle(buildDefaultTitle(source, year, categoryId));
        paper.setDuration(90);
        paper.setTotalScore(100);
        paper.setPassScore(60);
        paper.setQuestionIds(newIdsStr);
        paper.setStatus(0);
        paperMapper.insert(paper);
        return paper;
    }

    private String joinIds(Iterable<Long> ids) {
        StringBuilder sb = new StringBuilder();
        for (Long id : ids) {
            if (sb.length() > 0) sb.append(",");
            sb.append(id);
        }
        return sb.toString();
    }

    private String buildDefaultTitle(String source, Integer year, Long categoryId) {
        StringBuilder sb = new StringBuilder();
        if (source != null && !source.isEmpty()) {
            sb.append(source);
        } else {
            sb.append("真题");
        }
        if (year != null) {
            sb.append('-').append(year);
        }
        if (categoryId != null) {
            sb.append('-').append("分类").append(categoryId);
        }
        return sb.toString();
    }
}
