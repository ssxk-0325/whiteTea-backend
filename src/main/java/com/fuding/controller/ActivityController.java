package com.fuding.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.common.Result;
import com.fuding.entity.ExperienceActivity;
import com.fuding.entity.UserActivityCoupon;
import com.fuding.service.ActivityService;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 活动控制器
 */
@RestController
@RequestMapping("/activity")
@CrossOrigin
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取活动列表
     */
    @GetMapping("/list")
    public Result<IPage<ExperienceActivity>> getActivityList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword) {
        try {
            // MyBatis-Plus 的 Page 从 1 开始，前端传的是从 0 开始，需要 +1
            Page<ExperienceActivity> activityPage = new Page<>(page + 1, size);
            IPage<ExperienceActivity> result = activityService.getActivityList(activityPage, type, keyword);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public Result<ExperienceActivity> getActivityDetail(@PathVariable Long id) {
        try {
            ExperienceActivity activity = activityService.getActivityById(id);
            return Result.success(activity);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 抢券
     */
    @PostMapping("/{id}/grab")
    public Result<UserActivityCoupon> grabCoupon(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            UserActivityCoupon coupon = activityService.grabCoupon(userId, id);
            return Result.success("抢券成功", coupon);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的券包
     */
    @GetMapping("/coupons")
    public Result<List<UserActivityCoupon>> getMyCoupons(
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            List<UserActivityCoupon> coupons = activityService.getUserCoupons(userId, status);
            return Result.success(coupons);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查是否已抢过券
     */
    @GetMapping("/{id}/check-grabbed")
    public Result<Map<String, Object>> checkGrabbed(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            boolean hasGrabbed = activityService.hasGrabbedCoupon(userId, id);
            Map<String, Object> result = new HashMap<>();
            result.put("hasGrabbed", hasGrabbed);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 管理员接口 ==========

    /**
     * 管理员获取活动列表（包括所有状态）
     */
    @GetMapping("/admin/list")
    public Result<IPage<ExperienceActivity>> adminGetActivityList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token); // 验证token有效性

            // MyBatis-Plus 的 Page 从 1 开始，前端传的是从 0 开始，需要 +1
            Page<ExperienceActivity> activityPage = new Page<>(page + 1, size);
            IPage<ExperienceActivity> result = activityService.getAdminActivityList(activityPage, type, keyword);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员创建活动
     */
    @PostMapping("/admin/create")
    public Result<ExperienceActivity> adminCreateActivity(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token); // 验证token有效性

            ExperienceActivity activity = activityService.createActivity(params);
            return Result.success("创建成功", activity);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员更新活动
     */
    @PutMapping("/admin/update")
    public Result<ExperienceActivity> adminUpdateActivity(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token); // 验证token有效性

            ExperienceActivity activity = activityService.updateActivity(params);
            return Result.success("更新成功", activity);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员删除活动
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> adminDeleteActivity(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token); // 验证token有效性

            activityService.deleteActivity(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

