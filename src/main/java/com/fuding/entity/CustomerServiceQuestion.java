package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客服问题统计实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_customer_service_question")
public class CustomerServiceQuestion extends BaseEntity {

    /**
     * 问题内容
     */
    @TableField("question")
    private String question;

    /**
     * 问题哈希值（用于去重）
     */
    @TableField("question_hash")
    private String questionHash;

    /**
     * 提问次数
     */
    @TableField("ask_count")
    private Integer askCount = 1;

    /**
     * 提问用户数
     */
    @TableField("user_count")
    private Integer userCount = 1;

    /**
     * 最后提问时间
     */
    @TableField("last_ask_time")
    private java.time.LocalDateTime lastAskTime;
}

