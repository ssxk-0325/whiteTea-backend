package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单评价（整单一条）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_order_review")
public class OrderReview extends BaseEntity {

    @TableField("order_id")
    private Long orderId;

    @TableField("user_id")
    private Long userId;

    /**
     * 评分 1-5
     */
    @TableField("rating")
    private Integer rating;

    @TableField("content")
    private String content;

    @TableField(exist = false)
    private String userNickname;
}
