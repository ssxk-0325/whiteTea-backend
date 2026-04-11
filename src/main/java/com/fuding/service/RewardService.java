package com.fuding.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.entity.Reward;
import com.fuding.entity.RewardExchange;

import java.util.List;
import java.util.Map;

/**
 * 奖品服务接口
 */
public interface RewardService {

    /**
     * 获取奖品列表（分页）
     */
    IPage<Reward> getRewardList(Page<Reward> page, Integer type, String keyword);

    /**
     * 获取奖品详情
     */
    Reward getRewardById(Long id);

    /**
     * 兑换奖品
     */
    RewardExchange exchangeReward(Long userId, Long rewardId);

    /**
     * 获取用户的兑换记录
     */
    List<RewardExchange> getUserExchanges(Long userId);

    /**
     * 结算页可选：用户已兑换且未下单核销的积分商城优惠券（Reward.type=2）
     */
    List<Map<String, Object>> listCheckoutCoupons(Long userId);

    /**
     * 获取用户的积分信息
     */
    Map<String, Object> getUserPointsInfo(Long userId);

    /**
     * 管理员获取奖品列表（包括所有状态）
     */
    IPage<Reward> getAdminRewardList(Page<Reward> page, Integer type, String keyword, Integer status);

    /**
     * 管理员创建奖品
     */
    Reward createReward(Map<String, Object> params);

    /**
     * 管理员更新奖品
     */
    Reward updateReward(Map<String, Object> params);

    /**
     * 管理员删除奖品
     */
    void deleteReward(Long id);

    /**
     * 管理员处理兑换（发放奖品）
     */
    void processExchange(Long exchangeId, String exchangeCode, String remark);
}

