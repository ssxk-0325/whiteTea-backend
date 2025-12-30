package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户活动券实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_user_activity_coupon")
public class UserActivityCoupon extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 活动ID
     */
    @TableField("activity_id")
    private Long activityId;

    /**
     * 券码（唯一）
     */
    @TableField("coupon_code")
    private String couponCode;

    /**
     * 券名称
     */
    @TableField("coupon_name")
    private String couponName;

    /**
     * 券类型：1-茶艺课，2-茶园参观，3-线下品鉴会，4-制茶体验
     */
    @TableField("coupon_type")
    private Integer couponType = 1;

    /**
     * 状态：0-未使用，1-已使用，2-已过期
     */
    @TableField("status")
    private Integer status = 0;

    /**
     * 使用时间
     */
    @TableField("use_time")
    private LocalDateTime useTime;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;
}

