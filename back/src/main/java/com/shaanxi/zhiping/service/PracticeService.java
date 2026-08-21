package com.shaanxi.zhiping.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaanxi.zhiping.common.CacheConstants;
import com.shaanxi.zhiping.dto.PracticeGradeDTO;
import com.shaanxi.zhiping.dto.PracticeGradeResultVO;
import com.shaanxi.zhiping.dto.PracticeQueryDTO;
import com.shaanxi.zhiping.dto.PracticeQuestionVO;
import com.shaanxi.zhiping.entity.ExamRecord;
import com.shaanxi.zhiping.entity.Paper;
import com.shaanxi.zhiping.entity.Question;
import com.shaanxi.zhiping.mapper.ExamRecordMapper;
import com.shaanxi.zhiping.mapper.PaperMapper;
import com.shaanxi.zhiping.mapper.QuestionMapper;
import com.shaanxi.zhiping.mapper.WrongQuestionMapper;
import com.shaanxi.zhiping.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
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

    /** 真题练习整卷缓存前缀，复用 CacheConstants.PRACTICE_LIST_PREFIX */
    private static final String CACHE_PREFIX = CacheConstants.PRACTICE_LIST_PREFIX;

    /** 整卷缓存 10 分钟 */
    private static final long CACHE_TTL = 10 * 60;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private PaperMapper paperMapper;

    @Resource
    private WrongQuestionMapper wrongQuestionMapper;

    @Resource
    private ExamRecordMapper examRecordMapper;

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
                            .eq(Question::getBizSection, BIZ_SECTION_DANZHAO)
                            .eq(Question::getStatus, STATUS_APPROVED)
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
                            .eq(Question::getBizSection, BIZ_SECTION_DANZHAO)
                            .eq(Question::getStatus, STATUS_APPROVED)
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

    /**
     * 按 t_paper.question_ids 拉取一套试卷的题目（用于全真模考）
     *
     * 与 {@link #listPaper(PracticeQueryDTO)} 的区别：题目来源是试卷的 question_ids 字段，
     * 而非 year + categoryId 查询。复用 toVO 转换，复合题子题一次性嵌套返回。
     *
     * 过滤规则：
     *  - 仅返回 status=2 已通过的题目（未通过/已物理删除的题目自动跳过）
     *  - 仅返回 parentId=0 的独立题 + 复合题大题作为试卷组成；复合题大题后嵌套其子题
     *
     * @param paperId 试卷ID
     * @return 题目列表（复合题子题已嵌套），按 question_ids 顺序；试卷不存在或无题目返回空列表
     */
    public List<PracticeQuestionVO> listPaperById(Long paperId) {
        if (paperId == null) {
            return Collections.emptyList();
        }

        String cacheKey = CacheConstants.PRACTICE_LIST_PREFIX + "paper:v2:" + paperId;
        List<PracticeQuestionVO> cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("模考试卷命中缓存 paperId={}", paperId);
            return cached;
        }

        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            return Collections.emptyList();
        }

        // 1. 解析 question_ids（保持原顺序、去重）
        LinkedHashSet<Long> idSet = new LinkedHashSet<>();
        String idsStr = paper.getQuestionIds();
        if (idsStr == null || idsStr.isEmpty()) {
            return Collections.emptyList();
        }
        for (String s : idsStr.split(",")) {
            if (s == null || s.trim().isEmpty()) continue;
            try {
                idSet.add(Long.parseLong(s.trim()));
            } catch (NumberFormatException ignore) {
            }
        }
        if (idSet.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 批量拉取试卷引用的题目（已发布试卷视为内容已审定，不再按 status 过滤）
        List<Question> mains = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .in(Question::getId, idSet)
                        .eq(Question::getParentId, 0L)
        );
        Map<Long, Question> mainMap = mains.stream()
                .collect(Collectors.toMap(Question::getId, q -> q, (a, b) -> a));

        // 3. 按 question_ids 原顺序输出，并收集复合题大题 ID
        List<Question> ordered = new ArrayList<>();
        List<Long> compositeIds = new ArrayList<>();
        for (Long qid : idSet) {
            Question q = mainMap.get(qid);
            if (q == null) continue; // 未通过/已删除，跳过
            ordered.add(q);
            if (q.getType() != null && q.getType() == TYPE_COMPOSITE) {
                compositeIds.add(q.getId());
            }
        }

        // 4. 一次性拉取所有复合题子题（不按 status 过滤，同上）
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

        // 5. 转换为 VO，复合题嵌套子题
        List<PracticeQuestionVO> result = new ArrayList<>(ordered.size());
        for (Question q : ordered) {
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

        // 6. 写缓存
        redisUtil.set(cacheKey, result, CACHE_TTL);
        return result;
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

    // ==================== 批量判分 + 记录错题 ====================

    /** 单选 */
    private static final int TYPE_SINGLE = 1;
    /** 多选 */
    private static final int TYPE_MULTI = 2;
    /** 判断 */
    private static final int TYPE_JUDGE = 3;
    /** 简答 */
    private static final int TYPE_SHORT = 5;
    /** 计算 */
    private static final int TYPE_CALC = 6;

    /**
     * 批量判分并记录错题
     *
     * 判分规则：
     *  - 单选/判断：选中选项的 letter == answer
     *  - 多选：选中选项 letters 排序后 == answer 排序后
     *  - 填空：用户文本逐空匹配（忽略大小写、首尾空格）
     *  - 简答/计算：忽略大小写、空白后完全相等
     *  - 复合题：逐子题判分
     *
     * 错题记录规则：有正确答案 + 用户已作答 + 答错 → upsert t_wrong_question
     * （复合题记录答错的子题ID）
     *
     * @param dto   用户作答数据
     * @param userId 当前登录用户ID
     * @return 判分结果（每题 + 汇总）
     */
    public PracticeGradeResultVO grade(PracticeGradeDTO dto, Long userId) {
        PracticeGradeResultVO resultVO = new PracticeGradeResultVO();
        List<PracticeGradeResultVO.ItemResultVO> results = new ArrayList<>();

        int total = 0, answered = 0, correct = 0, wrong = 0, noRef = 0;
        BigDecimal gotScore = BigDecimal.ZERO;

        if (dto == null || dto.getAnswers() == null || dto.getAnswers().isEmpty()) {
            PracticeGradeResultVO.SummaryVO summary = new PracticeGradeResultVO.SummaryVO();
            summary.setTotal(0);
            summary.setAnswered(0);
            summary.setCorrect(0);
            summary.setWrong(0);
            summary.setNoRef(0);
            summary.setGotScore(BigDecimal.ZERO);
            resultVO.setResults(results);
            resultVO.setSummary(summary);
            return resultVO;
        }

        for (PracticeGradeDTO.AnswerDTO ans : dto.getAnswers()) {
            if (ans == null || ans.getQuestionId() == null) continue;
            total++;

            Question q = questionMapper.selectById(ans.getQuestionId());
            if (q == null) {
                // 题目不存在，跳过
                PracticeGradeResultVO.ItemResultVO r = new PracticeGradeResultVO.ItemResultVO();
                r.setQuestionId(ans.getQuestionId());
                r.setIsCorrect(false);
                r.setNoReference(true);
                r.setUserAnswerText("");
                r.setCorrectAnswerText("");
                results.add(r);
                noRef++;
                continue;
            }

            PracticeGradeResultVO.ItemResultVO item = gradeOne(q, ans, userId);
            results.add(item);

            if (isAnswered(ans)) answered++;
            if (item.getNoReference() != null && item.getNoReference()) {
                noRef++;
            } else if (item.getIsCorrect() != null && item.getIsCorrect()) {
                correct++;
                if (q.getScore() != null) {
                    gotScore = gotScore.add(q.getScore());
                }
            } else {
                wrong++;
            }
        }

        PracticeGradeResultVO.SummaryVO summary = new PracticeGradeResultVO.SummaryVO();
        summary.setTotal(total);
        summary.setAnswered(answered);
        summary.setCorrect(correct);
        summary.setWrong(wrong);
        summary.setNoRef(noRef);
        summary.setGotScore(gotScore);
        resultVO.setResults(results);
        resultVO.setSummary(summary);
        try {
            log.info("[grade] 返回结果: {}", objectMapper.writeValueAsString(resultVO));
        } catch (Exception e) {
            log.warn("[grade] 序列化结果失败", e);
        }

        // 写入模考记录（仅模拟考试场景，dto.paperId 非空时）
        saveExamRecord(dto, userId, summary);

        return resultVO;
    }

    /**
     * 判分单题（含复合题子题）
     */
    private PracticeGradeResultVO.ItemResultVO gradeOne(Question q, PracticeGradeDTO.AnswerDTO ans, Long userId) {
        PracticeGradeResultVO.ItemResultVO item = new PracticeGradeResultVO.ItemResultVO();
        item.setQuestionId(q.getId());

        Integer type = q.getType();

        // 复合题：逐子题判分
        if (type != null && type == TYPE_COMPOSITE) {
            return gradeComposite(q, ans, userId);
        }

        boolean hasRef = hasCorrectAnswer(q);
        boolean answered = isAnswered(ans);

        // 无参考答案
        if (!hasRef) {
            item.setIsCorrect(false);
            item.setNoReference(true);
            item.setUserAnswerText(buildUserAnswerText(q, ans));
            item.setCorrectAnswerText("");
            return item;
        }

        boolean isCorrect = doGrade(q, ans);
        item.setIsCorrect(isCorrect);
        item.setNoReference(false);
        item.setUserAnswerText(buildUserAnswerText(q, ans));
        item.setCorrectAnswerText(buildCorrectAnswerText(q));

        // 记录错题：有正确答案 + 已作答 + 答错
        if (answered && !isCorrect && q.getId() != null) {
            try {
                wrongQuestionMapper.upsert(userId, q.getId());
            } catch (Exception e) {
                log.warn("记录错题失败 userId={}, questionId={}", userId, q.getId(), e);
            }
        }

        return item;
    }

    /**
     * 复合题判分：逐个子题判分，任一子题答错则记录该子题错题
     */
    private PracticeGradeResultVO.ItemResultVO gradeComposite(Question q, PracticeGradeDTO.AnswerDTO ans, Long userId) {
        PracticeGradeResultVO.ItemResultVO item = new PracticeGradeResultVO.ItemResultVO();
        item.setQuestionId(q.getId());

        // 查子题
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getParentId, q.getId())
               .orderByAsc(Question::getSort);
        List<Question> subQuestions = questionMapper.selectList(wrapper);

        List<PracticeGradeResultVO.ItemResultVO> subResults = new ArrayList<>();
        List<PracticeGradeDTO.AnswerDTO> subAnswers = ans != null ? ans.getSubAnswers() : null;

        for (int i = 0; i < subQuestions.size(); i++) {
            Question sub = subQuestions.get(i);
            PracticeGradeDTO.AnswerDTO subAns = (subAnswers != null && i < subAnswers.size()) ? subAnswers.get(i) : new PracticeGradeDTO.AnswerDTO();
            PracticeGradeResultVO.ItemResultVO subItem = gradeOne(sub, subAns, userId);
            subResults.add(subItem);
        }

        item.setSubResults(subResults);
        // 复合题大题本身不判对错（由子题决定），也不记录大题错题
        item.setIsCorrect(false);
        item.setNoReference(false);
        item.setUserAnswerText("");
        item.setCorrectAnswerText("");
        return item;
    }

    /**
     * 核心判分逻辑（非复合题）
     */
    private boolean doGrade(Question q, PracticeGradeDTO.AnswerDTO ans) {
        Integer type = q.getType();
        String correctAnswer = q.getAnswer();

        if (type == null || correctAnswer == null || correctAnswer.isEmpty()) {
            return false;
        }

        switch (type) {
            case TYPE_SINGLE:
            case TYPE_JUDGE:
                return gradeSingleChoice(q, ans, correctAnswer);
            case TYPE_MULTI:
                return gradeMultiChoice(q, ans, correctAnswer);
            case TYPE_FILL:
                return gradeFill(q, ans, correctAnswer);
            case TYPE_SHORT:
            case TYPE_CALC:
                return gradeShortAnswer(ans, correctAnswer);
            default:
                return false;
        }
    }

    /** 单选/判断：选中选项的 letter == answer */
    private boolean gradeSingleChoice(Question q, PracticeGradeDTO.AnswerDTO ans, String correctAnswer) {
        if (ans == null || ans.getSelected() == null || ans.getSelected().isEmpty()) {
            return false;
        }
        List<PracticeQuestionVO.Option> options = parseOptions(q.getOptions());
        if (options == null || options.isEmpty()) {
            return false;
        }
        int idx = ans.getSelected().get(0);
        if (idx < 0 || idx >= options.size()) {
            return false;
        }
        // 用户选项字母：options 没有 letter 时用索引生成 A/B/C/D
        String userLetter = options.get(idx).getLetter();
        if (userLetter == null || userLetter.isEmpty()) {
            userLetter = String.valueOf((char) ('A' + idx));
        }
        // 正确答案归一化为字母（兼容字母/数字索引/选项文本三种存储格式）
        String correctLetter = normalizeChoiceAnswerDisplay(correctAnswer, options, TYPE_SINGLE);
        return userLetter.equalsIgnoreCase(correctLetter);
    }

    /** 多选：选中选项 letters 排序后 == answer 排序后 */
    private boolean gradeMultiChoice(Question q, PracticeGradeDTO.AnswerDTO ans, String correctAnswer) {
        if (ans == null || ans.getSelected() == null || ans.getSelected().isEmpty()) {
            return false;
        }
        List<PracticeQuestionVO.Option> options = parseOptions(q.getOptions());
        if (options == null || options.isEmpty()) {
            return false;
        }
        // 用户选中的 letters（options 没有 letter 时用索引生成 A/B/C/D）
        List<String> userLetters = new ArrayList<>();
        for (Integer idx : ans.getSelected()) {
            if (idx == null || idx < 0 || idx >= options.size()) continue;
            String letter = options.get(idx).getLetter();
            if (letter == null || letter.isEmpty()) {
                letter = String.valueOf((char) ('A' + idx));
            }
            userLetters.add(letter.toUpperCase());
        }
        // 正确答案归一化为字母列表（兼容字母/数字索引/选项文本三种存储格式）
        String correctStr = normalizeChoiceAnswerDisplay(correctAnswer, options, TYPE_MULTI);
        List<String> correctLetters = new ArrayList<>();
        for (char c : correctStr.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                correctLetters.add(String.valueOf(c).toUpperCase());
            }
        }
        Collections.sort(userLetters);
        Collections.sort(correctLetters);
        return userLetters.equals(correctLetters);
    }

    /** 填空：用户文本逐空匹配（忽略大小写、首尾空格） */
    private boolean gradeFill(Question q, PracticeGradeDTO.AnswerDTO ans, String correctAnswer) {
        if (ans == null || ans.getText() == null || ans.getText().trim().isEmpty()) {
            return false;
        }
        List<String> correctBlanks;
        try {
            correctBlanks = objectMapper.readValue(correctAnswer, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // answer 不是 JSON 数组，当作单空处理
            correctBlanks = Collections.singletonList(correctAnswer);
        }
        // 用户答案按换行或常见分隔符拆分
        String[] userBlanks = ans.getText().split("[\\n\\r;；,，|]");
        if (userBlanks.length != correctBlanks.size()) {
            return false;
        }
        for (int i = 0; i < correctBlanks.size(); i++) {
            String u = userBlanks[i].trim().toLowerCase();
            String c = correctBlanks.get(i).trim().toLowerCase();
            if (!u.equals(c)) {
                return false;
            }
        }
        return true;
    }

    /** 简答/计算：忽略大小写、空白后完全相等 */
    private boolean gradeShortAnswer(PracticeGradeDTO.AnswerDTO ans, String correctAnswer) {
        if (ans == null || ans.getText() == null || ans.getText().trim().isEmpty()) {
            return false;
        }
        String normUser = ans.getText().trim().toLowerCase().replaceAll("\\s+", " ");
        String normCorrect = correctAnswer.trim().toLowerCase().replaceAll("\\s+", " ");
        return normUser.equals(normCorrect);
    }

    /** 判断题目是否录入了正确答案 */
    private boolean hasCorrectAnswer(Question q) {
        return q.getAnswer() != null && !q.getAnswer().trim().isEmpty();
    }

    /** 判断用户是否已作答 */
    private boolean isAnswered(PracticeGradeDTO.AnswerDTO ans) {
        if (ans == null) return false;
        if (ans.getSelected() != null && !ans.getSelected().isEmpty()) return true;
        if (ans.getText() != null && !ans.getText().trim().isEmpty()) return true;
        if (ans.getSubAnswers() != null) {
            for (PracticeGradeDTO.AnswerDTO sub : ans.getSubAnswers()) {
                if (isAnswered(sub)) return true;
            }
        }
        return false;
    }

    /** 构建用户作答展示文本 */
    private String buildUserAnswerText(Question q, PracticeGradeDTO.AnswerDTO ans) {
        if (ans == null) return "";
        Integer type = q.getType();
        if (type != null && (type == TYPE_SINGLE || type == TYPE_JUDGE || type == TYPE_MULTI)) {
            List<PracticeQuestionVO.Option> options = parseOptions(q.getOptions());
            if (options == null || options.isEmpty() || ans.getSelected() == null) return "";
            List<String> letters = new ArrayList<>();
            for (Integer idx : ans.getSelected()) {
                if (idx == null || idx < 0 || idx >= options.size()) continue;
                String letter = options.get(idx).getLetter();
                // options 没有 letter 时用索引生成 A/B/C/D，保证与正确答案展示格式一致
                if (letter == null || letter.isEmpty()) {
                    letter = String.valueOf((char) ('A' + idx));
                }
                letters.add(letter);
            }
            // 多选按字母顺序排序后拼接，与正确答案展示一致
            if (type == TYPE_MULTI) {
                Collections.sort(letters);
                return String.join("", letters);
            }
            return String.join("、", letters);
        }
        return ans.getText() != null ? ans.getText().trim() : "";
    }

    /** 构建正确答案展示文本 */
    private String buildCorrectAnswerText(Question q) {
        String answer = q.getAnswer();
        if (answer == null || answer.trim().isEmpty()) {
            return "";
        }
        Integer type = q.getType();
        // 单选/多选：归一化为字母，与用户答案展示格式一致
        if (type != null && (type == TYPE_SINGLE || type == TYPE_MULTI)) {
            List<PracticeQuestionVO.Option> options = parseOptions(q.getOptions());
            return normalizeChoiceAnswerDisplay(answer, options, type);
        }
        return answer.trim();
    }

    /**
     * 单选/多选答案归一化为字母展示
     * 兼容数据库 answer 的三种存储格式：
     *  - 字母："A" / "ABD" → 单选原样返回；多选拆字符排序后拼接
     *  - 数字索引："0" / "0,1,3" / "0、1、3" → 转为对应字母 A/B/C/D
     *  - 选项文本："李白" / "李白,杜甫" → 反查为字母
     *
     * @param answer  数据库 answer 原始值
     * @param options 已解析的选项列表
     * @param type    TYPE_SINGLE 或 TYPE_MULTI
     * @return 归一化后的字母串（单选如 "A"，多选如 "ABD"）
     */
    private String normalizeChoiceAnswerDisplay(String answer, List<PracticeQuestionVO.Option> options, int type) {
        if (answer == null) return "";
        String s = answer.trim();
        if (s.isEmpty()) return "";

        // 构建 text → letter、idx → letter 映射；options 无 letter 时用索引生成 A/B/C/D
        Map<String, String> textToLetter = new HashMap<>();
        Map<String, String> idxToLetter = new HashMap<>();
        if (options != null) {
            for (int i = 0; i < options.size(); i++) {
                PracticeQuestionVO.Option opt = options.get(i);
                String letter = opt.getLetter();
                if (letter == null || letter.isEmpty()) {
                    letter = String.valueOf((char) ('A' + i));
                }
                idxToLetter.put(String.valueOf(i), letter);
                if (opt.getText() != null && !opt.getText().trim().isEmpty()) {
                    textToLetter.put(opt.getText().trim(), letter);
                }
            }
        }

        // 单选
        if (type == TYPE_SINGLE) {
            // 纯字母
            if (s.length() == 1 && Character.isLetter(s.charAt(0))) {
                return s.toUpperCase();
            }
            // 数字索引
            if (idxToLetter.containsKey(s)) {
                return idxToLetter.get(s);
            }
            // 文本反查
            if (textToLetter.containsKey(s)) {
                return textToLetter.get(s);
            }
            return s;
        }

        // 多选：按分隔符拆分
        List<String> letters = new ArrayList<>();
        String[] parts = s.split("[,，、\\s]+");
        for (String p : parts) {
            if (p == null || p.isEmpty()) continue;
            // 纯字母串 "ABD" 拆字符
            if (p.matches("^[A-Za-z]+$")) {
                for (char c : p.toCharArray()) {
                    letters.add(String.valueOf(c).toUpperCase());
                }
            } else if (idxToLetter.containsKey(p)) {
                // 数字索引
                letters.add(idxToLetter.get(p));
            } else if (textToLetter.containsKey(p)) {
                // 文本反查
                letters.add(textToLetter.get(p));
            } else {
                // 无法识别，原样返回避免丢失信息
                return s;
            }
        }
        Collections.sort(letters);
        return String.join("", letters);
    }

    /**
     * 保存模考记录到 t_exam_record
     *
     * 仅在 dto.paperId 非空（模拟考试场景）时写入；
     * 真题练习（无 paperId）不写记录。
     *
     * 字段填充：
     *  - score：用户得分（summary.gotScore 取整）
     *  - totalScore：试卷满分（t_paper.total_score）
     *  - duration：用时秒数（由前端通过 dto 传入，若未传则置 0）
     *  - answers：用户作答数据 JSON（dto 的 answers 字段序列化）
     *  - submitTime：当前时间
     *  - status：1 已交卷
     */
    private void saveExamRecord(PracticeGradeDTO dto, Long userId, PracticeGradeResultVO.SummaryVO summary) {
        if (dto == null || dto.getPaperId() == null || userId == null) {
            return;
        }
        try {
            ExamRecord record = new ExamRecord();
            record.setUserId(userId);
            record.setPaperId(dto.getPaperId());
            // 得分（BigDecimal → int 取整）
            int score = 0;
            if (summary != null && summary.getGotScore() != null) {
                score = summary.getGotScore().intValue();
            }
            record.setScore(score);
            // 试卷总分
            Paper paper = paperMapper.selectById(dto.getPaperId());
            if (paper != null && paper.getTotalScore() != null) {
                record.setTotalScore(paper.getTotalScore());
            }
            // 用时（秒）：前端通过 dto.durationSec 传入
            record.setDuration(dto.getDurationSec() != null ? dto.getDurationSec() : 0);
            // 答题详情 JSON
            try {
                record.setAnswers(objectMapper.writeValueAsString(dto.getAnswers()));
            } catch (Exception e) {
                log.warn("[saveExamRecord] 序列化 answers 失败", e);
            }
            record.setSubmitTime(java.time.LocalDateTime.now());
            record.setStatus(1);
            examRecordMapper.insert(record);
            log.info("[saveExamRecord] 写入模考记录 userId={}, paperId={}, score={}", userId, dto.getPaperId(), score);
        } catch (Exception e) {
            // 记录失败不阻塞判分主流程
            log.error("[saveExamRecord] 写入模考记录失败 userId={}, paperId={}", userId, dto.getPaperId(), e);
        }
    }
}
