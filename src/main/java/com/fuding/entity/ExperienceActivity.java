package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 体验活动实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_experience_activity")
public class ExperienceActivity extends BaseEntity {

    /**
     * 活动名称
     */
    @TableField("name")
    private String name;

    /**
     * 活动描述
     */
    @TableField("description")
    private String description;

    /**
     * 活动图片
     */
    @TableField("image")
    private String image;

    /**
     * 活动类型：1-茶艺课，2-茶园参观，3-线下品鉴会，4-制茶体验
     */
    @TableField("type")
    private Integer type = 1;

    /**
     * 价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 活动开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 报名人数上限
     */
    @TableField("max_participants")
    private Integer maxParticipants = 0;

    /**
     * 已报名人数
     */
    @TableField("current_participants")
    private Integer currentParticipants = 0;

    /**
     * 抢券开始时间
     */
    @TableField("coupon_start_time")
    private LocalDateTime couponStartTime;

    /**
     * 抢券结束时间
     */
    @TableField("coupon_end_time")
    private LocalDateTime couponEndTime;

    /**
     * 总券数
     */
    @TableField("total_coupons")
    private Integer totalCoupons = 0;

    /**
     * 已发放券数
     */
    @TableField("issued_coupons")
    private Integer issuedCoupons = 0;

    /**
     * 状态：0-未开始，1-进行中，2-已结束，3-已取消
     */
    @TableField("status")
    private Integer status = 0;
}
