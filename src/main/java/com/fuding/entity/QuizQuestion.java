package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 趣味问答问题实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_quiz_question")
public class QuizQuestion extends BaseEntity {

    /**
     * 问题内容
     */
    @TableField("question")
    private String question;

    /**
     * 选项（JSON格式，如：["选项A","选项B","选项C","选项D"]）
     */
    @TableField("options")
    private String options;

    /**
     * 正确答案索引（0-3）
     */
    @TableField("correct_answer")
    private Integer correctAnswer;

    /**
     * 答案解析
     */
    @TableField("explanation")
    private String explanation;

    /**
     * 分类：1-互动，2-文化，3-活动
     */
    @TableField("category")
    private Integer category = 1;

    /**
     * 难度：1-简单，2-中等，3-困难
     */
    @TableField("difficulty")
    private Integer difficulty = 1;

    /**
     * 问题图片
     */
    @TableField("image")
    private String image;

    /**
     * 答题次数
     */
    @TableField("view_count")
    private Integer viewCount = 0;

    /**
     * 答对次数
     */
    @TableField("correct_count")
    private Integer correctCount = 0;

    /**
     * 状态：0-草稿，1-发布
     */
    @TableField("status")
    private Integer status = 1;

    /**
     * 用户答题状态（不映射到数据库）：0-未答，1-已答正确，2-已答错误
     */
    @TableField(exist = false)
    private Integer answerStatus;
}

