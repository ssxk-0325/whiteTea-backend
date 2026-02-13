package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 奖品实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_reward")
public class Reward extends BaseEntity {

    /**
     * 奖品名称
     */
    @TableField("name")
    private String name;

    /**
     * 奖品描述
     */
    @TableField("description")
    private String description;

    /**
     * 奖品图片
     */
    @TableField("image")
    private String image;

    /**
     * 所需积分
     */
    @TableField("points_required")
    private Integer pointsRequired;

    /**
     * 库存数量
     */
    @TableField("stock")
    private Integer stock = 0;

    /**
     * 已兑换数量
     */
    @TableField("total_exchanged")
    private Integer totalExchanged = 0;

    /**
     * 奖品类型：1-实物奖品，2-优惠券，3-虚拟奖品
     */
    @TableField("type")
    private Integer type = 1;

    /**
     * 状态：0-下架，1-上架
     */
    @TableField("status")
    private Integer status = 1;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder = 0;
}

