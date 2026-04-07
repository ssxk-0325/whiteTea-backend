package com.fuding.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.common.Result;
import com.fuding.entity.Reward;
import com.fuding.entity.RewardExchange;
import com.fuding.service.RewardService;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 奖品控制器
 */
@RestController
@RequestMapping("/reward")
@CrossOrigin
public class RewardController {

    @Autowired
    private RewardService rewardService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取奖品列表
     */
    @GetMapping("/list")
    public Result<IPage<Reward>> getRewardList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword) {
        try {
            Page<Reward> rewardPage = new Page<>(page + 1, size);
            IPage<Reward> result = rewardService.getRewardList(rewardPage, type, keyword);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取奖品详情
     */
    @GetMapping("/{id}")
    public Result<Reward> getRewardDetail(@PathVariable Long id) {
        try {
            Reward reward = rewardService.getRewardById(id);
            return Result.success(reward);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 兑换奖品
     */
    @PostMapping("/{id}/exchange")
    public Result<RewardExchange> exchangeReward(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            RewardExchange exchange = rewardService.exchangeReward(userId, id);
            return Result.success("兑换成功", exchange);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的兑换记录
     */
    @GetMapping("/my-exchanges")
    public Result<List<RewardExchange>> getMyExchanges(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            List<RewardExchange> exchanges = rewardService.getUserExchanges(userId);
            return Result.success(exchanges);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的积分信息
     */
    @GetMapping("/my-points")
    public Result<Map<String, Object>> getMyPoints(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            Map<String, Object> info = rewardService.getUserPointsInfo(userId);
            return Result.success(info);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 管理员接口 ==========

    /**
     * 管理员获取奖品列表
     */
    @GetMapping("/admin/list")
    public Result<IPage<Reward>> adminGetRewardList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token); // 验证token有效性

            Page<Reward> rewardPage = new Page<>(page + 1, size);
            IPage<Reward> result = rewardService.getAdminRewardList(rewardPage, type, keyword, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员创建奖品
     */
    @PostMapping("/admin/create")
    public Result<Reward> adminCreateReward(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token); // 验证token有效性

            Reward reward = rewardService.createReward(params);
            return Result.success("创建成功", reward);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员更新奖品
     */
    @PutMapping("/admin/update")
    public Result<Reward> adminUpdateReward(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token); // 验证token有效性

            Reward reward = rewardService.updateReward(params);
            return Result.success("更新成功", reward);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员删除奖品
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> adminDeleteReward(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token); // 验证token有效性

            rewardService.deleteReward(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员处理兑换（发放奖品）
     */
    @PostMapping("/admin/exchange/{id}/process")
    public Result<Void> adminProcessExchange(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token); // 验证token有效性

            String exchangeCode = params.get("exchangeCode") != null ? params.get("exchangeCode").toString() : null;
            String remark = params.get("remark") != null ? params.get("remark").toString() : null;

            rewardService.processExchange(id, exchangeCode, remark);
            return Result.success("处理成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

