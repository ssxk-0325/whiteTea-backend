package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 积分兑换记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_reward_exchange")
public class RewardExchange extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 奖品ID
     */
    @TableField("reward_id")
    private Long rewardId;

    /**
     * 奖品名称（冗余字段，便于查询）
     */
    @TableField("reward_name")
    private String rewardName;

    /**
     * 使用的积分
     */
    @TableField("points_used")
    private Integer pointsUsed;

    /**
     * 状态：0-待处理，1-已发放，2-已取消
     */
    @TableField("status")
    private Integer status = 0;

    /**
     * 兑换码（用于虚拟奖品）
     */
    @TableField("exchange_code")
    private String exchangeCode;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 使用该兑换券抵扣的订单ID（积分商城优惠券下单核销后写入）
     */
    @TableField("order_id")
    private Long orderId;
}

