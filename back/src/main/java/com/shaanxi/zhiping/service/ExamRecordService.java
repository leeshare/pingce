package com.shaanxi.zhiping.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaanxi.zhiping.dto.ExamRecordDetailVO;
import com.shaanxi.zhiping.dto.ExamRecordListVO;
import com.shaanxi.zhiping.dto.PracticeGradeDTO;
import com.shaanxi.zhiping.dto.PracticeQuestionVO;
import com.shaanxi.zhiping.entity.ExamRecord;
import com.shaanxi.zhiping.entity.Paper;
import com.shaanxi.zhiping.mapper.ExamRecordMapper;
import com.shaanxi.zhiping.mapper.PaperMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 模考记录 Service（小程序端）
 *
 * 提供历史成绩列表查询、单次考试作答详情查询。
 * 数据来自 t_exam_record。
 */
@Slf4j
@Service
public class ExamRecordService {

    @Resource
    private ExamRecordMapper examRecordMapper;

    @Resource
    private PaperMapper paperMapper;

    @Resource
    private PracticeService practiceService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 查询当前用户的历史模考记录列表
     *
     * @param userId 用户ID
     * @return 按交卷时间倒序
     */
    public List<ExamRecordListVO> list(Long userId) {
        if (userId == null) return Collections.emptyList();

        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getUserId, userId)
               .eq(ExamRecord::getStatus, 1)
               .orderByDesc(ExamRecord::getSubmitTime);
        List<ExamRecord> records = examRecordMapper.selectList(wrapper);

        List<ExamRecordListVO> result = new ArrayList<>();
        for (ExamRecord r : records) {
            ExamRecordListVO vo = new ExamRecordListVO();
            vo.setId(r.getId());
            vo.setPaperId(r.getPaperId());
            vo.setScore(r.getScore());
            vo.setTotalScore(r.getTotalScore());
            vo.setDuration(r.getDuration());
            vo.setSubmitTime(r.getSubmitTime());
            // 试卷标题
            if (r.getPaperId() != null) {
                Paper paper = paperMapper.selectById(r.getPaperId());
                if (paper != null) {
                    vo.setPaperTitle(paper.getTitle());
                }
            }
            result.add(vo);
        }
        return result;
    }

    /**
     * 查询单次考试作答详情
     *
     * 返回：试卷信息 + 题目列表（含正确答案/解析）+ 用户作答 + 判分结果
     *
     * @param recordId 考试记录ID
     * @param userId   当前用户ID（鉴权用，只能查自己的记录）
     * @return 详情 VO；记录不存在或不属于该用户时返回 null
     */
    public ExamRecordDetailVO detail(Long recordId, Long userId) {
        if (recordId == null || userId == null) return null;

        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null || !userId.equals(record.getUserId())) {
            return null;
        }

        ExamRecordDetailVO vo = new ExamRecordDetailVO();
        vo.setRecordId(record.getId());
        vo.setPaperId(record.getPaperId());
        vo.setScore(record.getScore());
        vo.setTotalScore(record.getTotalScore());
        vo.setDuration(record.getDuration());
        vo.setSubmitTime(record.getSubmitTime());

        // 试卷标题
        if (record.getPaperId() != null) {
            Paper paper = paperMapper.selectById(record.getPaperId());
            if (paper != null) {
                vo.setPaperTitle(paper.getTitle());
            }
        }

        // 题目列表（复用 PracticeService.listPaperById，含正确答案/解析）
        List<PracticeQuestionVO> questions = practiceService.listPaperById(record.getPaperId());
        vo.setQuestions(questions);

        // 用户作答（从 answers JSON 反序列化）
        List<PracticeGradeDTO.AnswerDTO> userAnswers = new ArrayList<>();
        if (record.getAnswers() != null && !record.getAnswers().isEmpty()) {
            try {
                userAnswers = objectMapper.readValue(
                        record.getAnswers(),
                        new TypeReference<List<PracticeGradeDTO.AnswerDTO>>() {}
                );
            } catch (Exception e) {
                log.warn("[detail] 反序列化 answers 失败 recordId={}", recordId, e);
            }
        }
        vo.setUserAnswers(userAnswers);

        return vo;
    }
}
