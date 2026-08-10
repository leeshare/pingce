package com.shaanxi.zhiping;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shaanxi.zhiping.entity.Paper;
import com.shaanxi.zhiping.entity.Question;
import com.shaanxi.zhiping.mapper.PaperMapper;
import com.shaanxi.zhiping.mapper.QuestionMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * biz_section 字段查询与插入验证测试
 * 覆盖：Question / Paper 两个表的 biz_section 字段
 */
@SpringBootTest
class BizSectionFieldTest {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private PaperMapper paperMapper;

    /** 测试中创建的题目ID列表，测试结束后清理 */
    private Long createdQuestionId;
    private Long createdPaperId;

    @BeforeEach
    void setUp() {
        // 测试前置：确认数据库可连通
        assertNotNull(questionMapper, "QuestionMapper 未注入");
        assertNotNull(paperMapper, "PaperMapper 未注入");
    }

    @AfterEach
    void tearDown() {
        // 清理本次测试创建的数据，避免污染数据库
        if (createdQuestionId != null) {
            questionMapper.deleteById(createdQuestionId);
            createdQuestionId = null;
        }
        if (createdPaperId != null) {
            paperMapper.deleteById(createdPaperId);
            createdPaperId = null;
        }
    }

    // ============================================================
    // 一、Question 表 biz_section 测试
    // ============================================================

    @Test
    @DisplayName("Question 插入：默认 biz_section 应为 1（单招）")
    void testQuestionInsertDefaultBizSection() {
        Question q = buildSampleQuestion(null); // 不显式设置 biz_section
        questionMapper.insert(q);
        createdQuestionId = q.getId();

        // 重新查询验证
        Question loaded = questionMapper.selectById(createdQuestionId);
        assertNotNull(loaded, "插入后查询不应为空");
        assertEquals(1, loaded.getBizSection(), "默认 biz_section 应为 1（单招）");
    }

    @Test
    @DisplayName("Question 插入：显式设置 biz_section=4（高考）应正确保存")
    void testQuestionInsertExplicitBizSection() {
        Question q = buildSampleQuestion(4); // 高考
        questionMapper.insert(q);
        createdQuestionId = q.getId();

        Question loaded = questionMapper.selectById(createdQuestionId);
        assertNotNull(loaded);
        assertEquals(4, loaded.getBizSection(), "biz_section 应为 4（高考）");
    }

    @Test
    @DisplayName("Question 查询：按 biz_section 过滤应只返回对应分区题目")
    void testQuestionQueryByBizSection() {
        // 插入两条不同分区的题目
        Question q1 = buildSampleQuestion(1); // 单招
        questionMapper.insert(q1);
        Question q2 = buildSampleQuestion(4); // 高考
        questionMapper.insert(q2);

        try {
            // 查询 biz_section=4 的题目，应能查到 q2
            LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Question::getBizSection, 4)
                   .eq(Question::getContent, q2.getContent()); // 加唯一标识避免查到历史数据
            List<Question> list = questionMapper.selectList(wrapper);

            assertFalse(list.isEmpty(), "应能查到 biz_section=4 的高考题目");
            assertEquals(4, list.get(0).getBizSection());
            // 同时验证不会查到单招题目
            assertEquals(1, list.size(), "不应查到 biz_section=1 的题目");
        } finally {
            // 清理两条数据
            questionMapper.deleteById(q1.getId());
            questionMapper.deleteById(q2.getId());
        }
    }

    @Test
    @DisplayName("Question 更新：biz_section 字段可被更新")
    void testQuestionUpdateBizSection() {
        Question q = buildSampleQuestion(1);
        questionMapper.insert(q);
        createdQuestionId = q.getId();

        // 更新为高考分区
        q.setBizSection(4);
        int rows = questionMapper.updateById(q);
        assertEquals(1, rows, "更新应影响 1 行");

        Question loaded = questionMapper.selectById(createdQuestionId);
        assertEquals(4, loaded.getBizSection(), "更新后 biz_section 应为 4");
    }

    // ============================================================
    // 二、Paper 表 biz_section 测试
    // ============================================================

    @Test
    @DisplayName("Paper 插入：默认 biz_section 应为 1（单招）")
    void testPaperInsertDefaultBizSection() {
        Paper p = buildSamplePaper(null);
        paperMapper.insert(p);
        createdPaperId = p.getId();

        Paper loaded = paperMapper.selectById(createdPaperId);
        assertNotNull(loaded);
        assertEquals(1, loaded.getBizSection(), "默认 biz_section 应为 1（单招）");
    }

    @Test
    @DisplayName("Paper 插入：显式设置 biz_section=3（中考）应正确保存")
    void testPaperInsertExplicitBizSection() {
        Paper p = buildSamplePaper(3);
        paperMapper.insert(p);
        createdPaperId = p.getId();

        Paper loaded = paperMapper.selectById(createdPaperId);
        assertNotNull(loaded);
        assertEquals(3, loaded.getBizSection(), "biz_section 应为 3（中考）");
    }

    @Test
    @DisplayName("Paper 查询：按 biz_section 过滤应只返回对应分区试卷")
    void testPaperQueryByBizSection() {
        Paper p1 = buildSamplePaper(1);
        paperMapper.insert(p1);
        Paper p2 = buildSamplePaper(3);
        paperMapper.insert(p2);

        try {
            LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Paper::getBizSection, 3)
                   .eq(Paper::getTitle, p2.getTitle());
            List<Paper> list = paperMapper.selectList(wrapper);

            assertFalse(list.isEmpty(), "应能查到 biz_section=3 的中考试卷");
            assertEquals(3, list.get(0).getBizSection());
            assertEquals(1, list.size(), "不应查到 biz_section=1 的试卷");
        } finally {
            paperMapper.deleteById(p1.getId());
            paperMapper.deleteById(p2.getId());
        }
    }

    // ============================================================
    // 三、辅助构造方法
    // ============================================================

    private Question buildSampleQuestion(Integer bizSection) {
        Question q = new Question();
        if (bizSection != null) {
            q.setBizSection(bizSection);
        }
        q.setCategoryId(1L);
        q.setParentId(0L);
        q.setType(1);
        q.setDifficulty(1);
        // 用时间戳保证题目内容唯一，避免与历史数据冲突
        q.setContent("[BIZ_SECTION_TEST] 测试题目-" + System.currentTimeMillis() + "-" + Math.random());
        q.setOptions("[\"A.选项1\",\"B.选项2\",\"C.选项3\",\"D.选项4\"]");
        q.setAnswer("A");
        return q;
    }

    private Paper buildSamplePaper(Integer bizSection) {
        Paper p = new Paper();
        if (bizSection != null) {
            p.setBizSection(bizSection);
        }
        p.setTitle("[BIZ_SECTION_TEST] 测试试卷-" + System.currentTimeMillis() + "-" + Math.random());
        p.setCategoryId(1L);
        p.setDuration(90);
        p.setTotalScore(100);
        p.setPassScore(60);
        p.setStatus(0);
        return p;
    }
}
