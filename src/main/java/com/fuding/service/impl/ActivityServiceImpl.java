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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    public IPage<ExperienceActivity> getActivityList(Page<ExperienceActivity> page, Integer type, String keyword, String category) {
        LambdaQueryWrapper<ExperienceActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(ExperienceActivity::getStatus, 3); // 不显示已取消的
        if (type != null) {
            wrapper.eq(ExperienceActivity::getType, type);
        } else if (category != null && !category.trim().isEmpty()) {
            if ("experience".equalsIgnoreCase(category.trim())) {
                wrapper.in(ExperienceActivity::getType, Arrays.asList(1, 2, 3, 4));
            } else if ("industry".equalsIgnoreCase(category.trim())) {
                wrapper.in(ExperienceActivity::getType, Arrays.asList(5, 6));
            }
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
            case 5:
                prefix = "PICK";
                break;
            case 6:
                prefix = "WHSL";
                break;
            default:
                prefix = "ACT";
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + timestamp + random;
    }

    @Override
    public IPage<ExperienceActivity> getAdminActivityList(Page<ExperienceActivity> page, Integer type, String keyword) {
        LambdaQueryWrapper<ExperienceActivity> wrapper = new LambdaQueryWrapper<>();
        // 管理员可以看到所有状态的活动，包括已取消的
        if (type != null) {
            wrapper.eq(ExperienceActivity::getType, type);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(ExperienceActivity::getName, keyword)
                    .or().like(ExperienceActivity::getDescription, keyword));
        }
        wrapper.orderByDesc(ExperienceActivity::getCreateTime);
        return activityMapper.selectPage(page, wrapper);
    }

    @Override
    public ExperienceActivity createActivity(Map<String, Object> params) {
        ExperienceActivity activity = new ExperienceActivity();
        activity.setName(params.get("name").toString());
        activity.setDescription(params.get("description") != null ? params.get("description").toString() : null);
        activity.setImage(params.get("image") != null ? params.get("image").toString() : null);
        activity.setType(params.get("type") != null ? Integer.valueOf(params.get("type").toString()) : 1);
        activity.setPrice(params.get("price") != null ? new BigDecimal(params.get("price").toString()) : null);
        
        if (params.get("startTime") != null) {
            activity.setStartTime(LocalDateTime.parse(params.get("startTime").toString().replace(" ", "T")));
        }
        if (params.get("endTime") != null) {
            activity.setEndTime(LocalDateTime.parse(params.get("endTime").toString().replace(" ", "T")));
        }
        if (params.get("couponStartTime") != null) {
            activity.setCouponStartTime(LocalDateTime.parse(params.get("couponStartTime").toString().replace(" ", "T")));
        }
        if (params.get("couponEndTime") != null) {
            activity.setCouponEndTime(LocalDateTime.parse(params.get("couponEndTime").toString().replace(" ", "T")));
        }
        
        activity.setTotalCoupons(params.get("totalCoupons") != null ? Integer.valueOf(params.get("totalCoupons").toString()) : 0);
        activity.setMaxParticipants(params.get("maxParticipants") != null ? Integer.valueOf(params.get("maxParticipants").toString()) : 0);
        activity.setStatus(params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : 0);
        
        activityMapper.insert(activity);
        return activity;
    }

    @Override
    public ExperienceActivity updateActivity(Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        ExperienceActivity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        if (params.get("name") != null) {
            activity.setName(params.get("name").toString());
        }
        if (params.get("description") != null) {
            activity.setDescription(params.get("description").toString());
        }
        if (params.get("image") != null) {
            activity.setImage(params.get("image").toString());
        }
        if (params.get("type") != null) {
            activity.setType(Integer.valueOf(params.get("type").toString()));
        }
        if (params.get("price") != null) {
            activity.setPrice(new BigDecimal(params.get("price").toString()));
        }
        if (params.get("startTime") != null) {
            activity.setStartTime(LocalDateTime.parse(params.get("startTime").toString().replace(" ", "T")));
        }
        if (params.get("endTime") != null) {
            activity.setEndTime(LocalDateTime.parse(params.get("endTime").toString().replace(" ", "T")));
        }
        if (params.get("couponStartTime") != null) {
            activity.setCouponStartTime(LocalDateTime.parse(params.get("couponStartTime").toString().replace(" ", "T")));
        }
        if (params.get("couponEndTime") != null) {
            activity.setCouponEndTime(LocalDateTime.parse(params.get("couponEndTime").toString().replace(" ", "T")));
        }
        if (params.get("totalCoupons") != null) {
            activity.setTotalCoupons(Integer.valueOf(params.get("totalCoupons").toString()));
        }
        if (params.get("maxParticipants") != null) {
            activity.setMaxParticipants(Integer.valueOf(params.get("maxParticipants").toString()));
        }
        if (params.get("status") != null) {
            activity.setStatus(Integer.valueOf(params.get("status").toString()));
        }

        activityMapper.updateById(activity);
        return activity;
    }

    @Override
    public void deleteActivity(Long id) {
        activityMapper.deleteById(id);
    }
}

