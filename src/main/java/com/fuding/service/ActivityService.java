package com.fuding.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.entity.ExperienceActivity;
import com.fuding.entity.UserActivityCoupon;

import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService {

    /**
     * 获取活动列表（分页）
     */
    IPage<ExperienceActivity> getActivityList(Page<ExperienceActivity> page, Integer type, String keyword);

    /**
     * 获取活动详情
     */
    ExperienceActivity getActivityById(Long id);

    /**
     * 抢券
     */
    UserActivityCoupon grabCoupon(Long userId, Long activityId);

    /**
     * 获取用户的券列表
     */
    List<UserActivityCoupon> getUserCoupons(Long userId, Integer status);

    /**
     * 核销券
     */
    void verifyCoupon(String couponCode);

    /**
     * 检查用户是否已抢过该活动的券
     */
    boolean hasGrabbedCoupon(Long userId, Long activityId);
}

