package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_order")
public class Order extends BaseEntity {

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 订单总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    @TableField("pay_amount")
    private BigDecimal payAmount;

    /**
     * 收货人姓名
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 收货人电话
     */
    @TableField("receiver_phone")
    private String receiverPhone;

    /**
     * 收货地址
     */
    @TableField("receiver_address")
    private String receiverAddress;

    /**
     * 配送方式：1-线上配送，2-线下自提
     */
    @TableField("delivery_type")
    private Integer deliveryType = 1;

    /**
     * 自提门店ID（仅自提时有值）
     */
    @TableField("store_id")
    private Long storeId;

    /**
     * 订单状态：0-待付款，1-待发货，2-待收货，3-已完成，4-已取消，5-退款中，6-已退款
     */
    @TableField("status")
    private Integer status = 0;

    /**
     * 支付方式：0-未支付，1-微信，2-支付宝，3-银行卡
     */
    @TableField("pay_type")
    private Integer payType = 0;

    /**
     * 支付时间
     */
    @TableField("pay_time")
    private LocalDateTime payTime;

    /**
     * 发货时间
     */
    @TableField("ship_time")
    private LocalDateTime shipTime;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
