package com.shaanxi.zhiping.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目批量导入批次实体
 */
@Data
@TableName("t_question_import_batch")
public class QuestionImportBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 批次ID（UUID） */
    private String batchId;

    private String fileName;

    /** 文件大小(字节) */
    private Long fileSize;

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    /** 失败明细JSON，如[{row:2,msg:"题型非法"}] */
    private String failDetail;

    /** 状态 0处理中 1成功 2部分失败 3失败 */
    private Integer status;

    /** 业务分区 1单招 2普通 3中考 4高考 5考研 */
    private Integer bizSection;

    private Long categoryId;

    private Integer year;

    private String source;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
