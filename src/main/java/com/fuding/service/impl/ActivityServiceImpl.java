package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.ExperienceActivity;
import com.fuding.entity.UserActivityCoupon;
import com.fuding.mapper.ExperienceActivityMapper;
import com.fuding.mapper.UserActivityCouponMapper;
import com.fuding.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 活动服务实现类
 */
@Service
@Transactional
public class ActivityServiceImpl extends ServiceImpl<ExperienceActivityMapper, ExperienceActivity> implements ActivityService {

    @Autowired
    private ExperienceActivityMapper activityMapper;

    @Autowired
    private UserActivityCouponMapper couponMapper;

    @Override
    public IPage<ExperienceActivity> getActivityList(Page<ExperienceActivity> page, Integer type, String keyword) {
        LambdaQueryWrapper<ExperienceActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(ExperienceActivity::getStatus, 3); // 不显示已取消的
        if (type != null) {
            wrapper.eq(ExperienceActivity::getType, type);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(ExperienceActivity::getName, keyword)
                    .or().like(ExperienceActivity::getDescription, keyword));
        }
        wrapper.orderByDesc(ExperienceActivity::getStartTime);
        return activityMapper.selectPage(page, wrapper);
    }

    @Override
    public ExperienceActivity getActivityById(Long id) {
        ExperienceActivity activity = activityMapper.selectById(id);
        if (activity == null || activity.getStatus() == 3) {
            throw new RuntimeException("活动不存在或已取消");
        }
        return activity;
    }

    @Override
    public UserActivityCoupon grabCoupon(Long userId, Long activityId) {
        // 检查活动是否存在
        ExperienceActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getStatus() == 3) {
            throw new RuntimeException("活动不存在或已取消");
        }

        // 检查是否在抢券时间内
        LocalDateTime now = LocalDateTime.now();
        if (activity.getCouponStartTime() != null && now.isBefore(activity.getCouponStartTime())) {
            throw new RuntimeException("抢券尚未开始");
        }
        if (activity.getCouponEndTime() != null && now.isAfter(activity.getCouponEndTime())) {
            throw new RuntimeException("抢券已结束");
        }

        // 检查是否还有券
        if (activity.getIssuedCoupons() >= activity.getTotalCoupons()) {
            throw new RuntimeException("券已抢完");
        }

        // 检查用户是否已抢过
        if (hasGrabbedCoupon(userId, activityId)) {
            throw new RuntimeException("您已经抢过该活动的券了");
        }

        // 生成券码
        String couponCode = generateCouponCode(activity.getType());

        // 创建用户券
        UserActivityCoupon coupon = new UserActivityCoupon();
        coupon.setUserId(userId);
        coupon.setActivityId(activityId);
        coupon.setCouponCode(couponCode);
        coupon.setCouponName(activity.getName() + "体验券");
        coupon.setCouponType(activity.getType());
        coupon.setStatus(0); // 未使用
        coupon.setExpireTime(activity.getEndTime()); // 过期时间为活动结束时间
        couponMapper.insert(coupon);

        // 更新活动的已发放券数
        LambdaUpdateWrapper<ExperienceActivity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ExperienceActivity::getId, activityId)
                .setSql("issued_coupons = issued_coupons + 1");
        activityMapper.update(null, wrapper);

        return coupon;
    }

    @Override
    public List<UserActivityCoupon> getUserCoupons(Long userId, Integer status) {
        LambdaQueryWrapper<UserActivityCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserActivityCoupon::getUserId, userId);
        if (status != null) {
            wrapper.eq(UserActivityCoupon::getStatus, status);
        }
        wrapper.orderByDesc(UserActivityCoupon::getCreateTime);
        return couponMapper.selectList(wrapper);
    }

    @Override
    public void verifyCoupon(String couponCode) {
        LambdaQueryWrapper<UserActivityCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserActivityCoupon::getCouponCode, couponCode);
        UserActivityCoupon coupon = couponMapper.selectOne(wrapper);
        
        if (coupon == null) {
            throw new RuntimeException("券码不存在");
        }
        if (coupon.getStatus() == 1) {
            throw new RuntimeException("该券已被使用");
        }
        if (coupon.getStatus() == 2) {
            throw new RuntimeException("该券已过期");
        }
        if (coupon.getExpireTime() != null && LocalDateTime.now().isAfter(coupon.getExpireTime())) {
            // 更新为已过期
            LambdaUpdateWrapper<UserActivityCoupon> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserActivityCoupon::getId, coupon.getId())
                    .set(UserActivityCoupon::getStatus, 2);
            couponMapper.update(null, updateWrapper);
            throw new RuntimeException("该券已过期");
        }

        // 核销券
        LambdaUpdateWrapper<UserActivityCoupon> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserActivityCoupon::getId, coupon.getId())
                .set(UserActivityCoupon::getStatus, 1)
                .set(UserActivityCoupon::getUseTime, LocalDateTime.now());
        couponMapper.update(null, updateWrapper);
    }

    @Override
    public boolean hasGrabbedCoupon(Long userId, Long activityId) {
        LambdaQueryWrapper<UserActivityCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserActivityCoupon::getUserId, userId)
                .eq(UserActivityCoupon::getActivityId, activityId);
        return couponMapper.selectCount(wrapper) > 0;
    }

    /**
     * 生成券码
     */
    private String generateCouponCode(Integer type) {
        String prefix = "";
        switch (type) {
            case 1:
                prefix = "TEA";
                break;
            case 2:
                prefix = "VISIT";
                break;
            case 3:
                prefix = "TASTE";
                break;
            case 4:
                prefix = "MAKE";
                break;
            default:
                prefix = "ACT";
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + timestamp + random;
    }
}

