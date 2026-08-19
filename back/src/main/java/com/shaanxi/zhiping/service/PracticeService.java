package com.shaanxi.zhiping.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaanxi.zhiping.common.CacheConstants;
import com.shaanxi.zhiping.dto.PracticeQueryDTO;
import com.shaanxi.zhiping.dto.PracticeQuestionVO;
import com.shaanxi.zhiping.entity.Question;
import com.shaanxi.zhiping.mapper.QuestionMapper;
import com.shaanxi.zhiping.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 真题练习 Service（小程序端）
 *
 * 与 {@link QuestionService}（通用题目查询）的区别：
 *  - 强制 bizSection=1（单招）、status=2（已通过）、parentId=0（独立题+复合题大题）
 *  - 不返回管理字段，输出 {@link PracticeQuestionVO}
 *  - 复合题(type=7) 的子题一次性嵌套返回，前端无需二次请求
 *  - options 已由 JSON 字符串解析为 Option 列表；填空题 answer 已转为 List
 *
 * 缓存策略：按 (year, categoryId) 维度整卷缓存 10 分钟，与管理端缓存隔离。
 */
@Slf4j
@Service
public class PracticeService {

    /** 单招业务分区 */
    private static final int BIZ_SECTION_DANZHAO = 1;

    /** 已通过状态 */
    private static final int STATUS_APPROVED = 2;

    /** 复合题大题类型 */
    private static final int TYPE_COMPOSITE = 7;

    /** 填空题类型 */
    private static final int TYPE_FILL = 4;

    /** 真题练习整卷缓存前缀，key: practice:list:{year}:{categoryId} */
    private static final String CACHE_PREFIX = "practice:list:";

    /** 整卷缓存 10 分钟 */
    private static final long CACHE_TTL = 10 * 60;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 查询一套真题
     *
     * @param query year + categoryId 必填
     * @return 题目列表（复合题子题已嵌套），按 sort、id 升序
     */
    public List<PracticeQuestionVO> listPaper(PracticeQueryDTO query) {
        if (query.getYear() == null || query.getCategoryId() == null) {
            return Collections.emptyList();
        }

        String cacheKey = CACHE_PREFIX + query.getYear() + ":" + query.getCategoryId();
        List<PracticeQuestionVO> cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("真题练习命中缓存 year={} categoryId={}", query.getYear(), query.getCategoryId());
            return cached;
        }

        // 1. 拉取所有独立题 + 复合题大题（不含子题）
        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getBizSection, BIZ_SECTION_DANZHAO)
                        .eq(Question::getStatus, STATUS_APPROVED)
                        .eq(Question::getParentId, 0L)
                        .eq(Question::getYear, query.getYear())
                        .eq(Question::getCategoryId, query.getCategoryId())
                        .orderByAsc(Question::getSort)
                        .orderByAsc(Question::getId)
        );

        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 找出所有复合题，一次性批量拉子题
        List<Long> compositeIds = questions.stream()
                .filter(q -> q.getType() != null && q.getType() == TYPE_COMPOSITE)
                .map(Question::getId)
                .collect(Collectors.toList());

        Map<Long, List<Question>> childrenMap = new HashMap<>();
        if (!compositeIds.isEmpty()) {
            List<Question> allChildren = questionMapper.selectList(
                    new LambdaQueryWrapper<Question>()
                            .in(Question::getParentId, compositeIds)
                            .orderByAsc(Question::getParentId)
                            .orderByAsc(Question::getSort)
                            .orderByAsc(Question::getId)
            );
            for (Question child : allChildren) {
                childrenMap.computeIfAbsent(child.getParentId(), k -> new ArrayList<>()).add(child);
            }
        }

        // 3. 转换为 VO，复合题嵌套子题
        List<PracticeQuestionVO> result = new ArrayList<>(questions.size());
        for (Question q : questions) {
            PracticeQuestionVO vo = toVO(q);
            if (q.getType() != null && q.getType() == TYPE_COMPOSITE) {
                List<Question> children = childrenMap.getOrDefault(q.getId(), Collections.emptyList());
                List<PracticeQuestionVO> subVOs = new ArrayList<>(children.size());
                for (Question child : children) {
                    subVOs.add(toVO(child));
                }
                vo.setSubQuestions(subVOs);
            }
            result.add(vo);
        }

        // 4. 写缓存
        redisUtil.set(cacheKey, result, CACHE_TTL);
        return result;
    }

    /**
     * 单题详情（含复合题子题）
     */
    public PracticeQuestionVO detail(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null || q.getStatus() == null || q.getStatus() != STATUS_APPROVED) {
            return null;
        }
        PracticeQuestionVO vo = toVO(q);
        if (q.getType() != null && q.getType() == TYPE_COMPOSITE) {
            List<Question> children = questionMapper.selectList(
                    new LambdaQueryWrapper<Question>()
                            .eq(Question::getParentId, id)
                            .orderByAsc(Question::getSort)
                            .orderByAsc(Question::getId)
            );
            List<PracticeQuestionVO> subVOs = new ArrayList<>(children.size());
            for (Question child : children) {
                subVOs.add(toVO(child));
            }
            vo.setSubQuestions(subVOs);
        }
        return vo;
    }

    // ==================== 实体 → VO ====================

    private PracticeQuestionVO toVO(Question q) {
        PracticeQuestionVO vo = new PracticeQuestionVO();
        vo.setId(q.getId());
        vo.setParentId(q.getParentId());
        vo.setType(q.getType());
        vo.setTypeLabel(typeLabel(q.getType()));
        vo.setSubType(q.getSubType());
        vo.setSort(q.getSort());
        vo.setDifficulty(q.getDifficulty());
        vo.setDifficultyLabel(difficultyLabel(q.getDifficulty()));
        vo.setContent(q.getContent());
        vo.setOptions(parseOptions(q.getOptions()));
        vo.setAnswer(parseAnswer(q.getAnswer(), q.getType()));
        vo.setScore(q.getScore());
        vo.setAnalysis(q.getAnalysis());
        vo.setYear(q.getYear());
        vo.setSource(q.getSource());
        return vo;
    }

    /**
     * 解析 options JSON 字符串为 Option 列表
     * 输入格式："[\"A.xxx\",\"B.xxx\"]"
     * 输出：[{letter:"A", text:"xxx"}, ...]
     */
    private List<PracticeQuestionVO.Option> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isEmpty()) {
            return null;
        }
        try {
            List<String> rawList = objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {});
            List<PracticeQuestionVO.Option> result = new ArrayList<>(rawList.size());
            for (String raw : rawList) {
                if (raw == null || raw.isEmpty()) continue;
                PracticeQuestionVO.Option opt = new PracticeQuestionVO.Option();
                // 形如 "A.选项内容" 或 "A、选项内容" 或 "A:选项内容"
                String letter = null;
                String text = raw;
                if (raw.length() >= 2) {
                    char first = raw.charAt(0);
                    if (Character.isLetterOrDigit(first)) {
                        char sep = raw.charAt(1);
                        if (sep == '.' || sep == '、' || sep == ':' || sep == '：' || sep == ')') {
                            letter = String.valueOf(first);
                            text = raw.substring(2).trim();
                        }
                    }
                }
                opt.setLetter(letter);
                opt.setText(text);
                result.add(opt);
            }
            return result;
        } catch (Exception e) {
            log.warn("options 解析失败，原样返回单元素列表 value={}", optionsJson, e);
            PracticeQuestionVO.Option opt = new PracticeQuestionVO.Option();
            opt.setLetter(null);
            opt.setText(optionsJson);
            return Collections.singletonList(opt);
        }
    }

    /**
     * 按题型规范化 answer
     *  - 填空(4)：JSON 字符串 → List<String>
     *  - 其他：原样返回字符串
     */
    private Object parseAnswer(String answer, Integer type) {
        if (answer == null || answer.isEmpty()) {
            return null;
        }
        if (type != null && type == TYPE_FILL) {
            try {
                return objectMapper.readValue(answer, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.warn("填空题 answer 解析失败，原样返回 value={}", answer);
                return answer;
            }
        }
        return answer;
    }

    private String typeLabel(Integer type) {
        if (type == null) return null;
        switch (type) {
            case 1: return "单选题";
            case 2: return "多选题";
            case 3: return "判断题";
            case 4: return "填空题";
            case 5: return "简答题";
            case 6: return "计算题";
            case 7: return "复合题";
            default: return "题目";
        }
    }

    private String difficultyLabel(Integer difficulty) {
        if (difficulty == null) return null;
        switch (difficulty) {
            case 1: return "简单";
            case 2: return "中等";
            case 3: return "困难";
            default: return null;
        }
    }
}
