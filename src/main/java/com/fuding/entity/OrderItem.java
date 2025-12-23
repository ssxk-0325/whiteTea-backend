package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单项实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_order_item")
public class OrderItem extends BaseEntity {

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 产品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 产品名称（快照）
     */
    @TableField("product_name")
    private String productName;

    /**
     * 产品图片（快照）
     */
    @TableField("product_image")
    private String productImage;

    /**
     * 单价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 数量
     */
    @TableField("quantity")
    private Integer quantity;

    /**
     * 小计
     */
    @TableField("subtotal")
    private BigDecimal subtotal;
}
