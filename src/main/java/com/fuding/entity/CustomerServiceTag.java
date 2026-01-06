package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 客服Tag实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_customer_service_tag")
public class CustomerServiceTag extends BaseEntity {

    /**
     * Tag名称
     */
    @TableField("tag_name")
    private String tagName;

    /**
     * Tag描述
     */
    @TableField("tag_description")
    private String tagDescription;

    /**
     * Tag对应的答案
     */
    @TableField("answer")
    private String answer;

    /**
     * 点击次数
     */
    @TableField("hit_count")
    private Integer hitCount = 0;

    /**
     * 关联的问题数量
     */
    @TableField("question_count")
    private Integer questionCount = 0;

    /**
     * Tag得分（用于排序）
     */
    @TableField("score")
    private BigDecimal score = BigDecimal.ZERO;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder = 0;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status = 1;
}

