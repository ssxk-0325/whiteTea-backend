package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户答题记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_quiz_answer")
public class QuizAnswer extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 问题ID
     */
    @TableField("question_id")
    private Long questionId;

    /**
     * 用户答案索引（0-3）
     */
    @TableField("user_answer")
    private Integer userAnswer;

    /**
     * 是否答对：0-错误，1-正确
     */
    @TableField("is_correct")
    private Integer isCorrect = 0;
}

