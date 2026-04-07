package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.Reward;
import com.fuding.entity.RewardExchange;
import com.fuding.entity.User;
import com.fuding.mapper.RewardExchangeMapper;
import com.fuding.mapper.RewardMapper;
import com.fuding.mapper.UserMapper;
import com.fuding.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 奖品服务实现类
 */
@Service
@Transactional
public class RewardServiceImpl extends ServiceImpl<RewardMapper, Reward> implements RewardService {

    @Autowired
    private RewardMapper rewardMapper;

    @Autowired
    private RewardExchangeMapper exchangeMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public IPage<Reward> getRewardList(Page<Reward> page, Integer type, String keyword) {
        LambdaQueryWrapper<Reward> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reward::getStatus, 1); // 只查询上架的
        if (type != null) {
            wrapper.eq(Reward::getType, type);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Reward::getName, keyword);
        }
        wrapper.orderByAsc(Reward::getSortOrder);
        wrapper.orderByDesc(Reward::getCreateTime);
        return rewardMapper.selectPage(page, wrapper);
    }

    @Override
    public Reward getRewardById(Long id) {
        Reward reward = rewardMapper.selectById(id);
        if (reward == null || reward.getStatus() != 1) {
            throw new RuntimeException("奖品不存在或已下架");
        }
        return reward;
    }

    @Override
    public RewardExchange exchangeReward(Long userId, Long rewardId) {
        // 检查奖品是否存在
        Reward reward = rewardMapper.selectById(rewardId);
        if (reward == null || reward.getStatus() != 1) {
            throw new RuntimeException("奖品不存在或已下架");
        }

        // 检查库存
        if (reward.getStock() != null && reward.getStock() <= 0) {
            throw new RuntimeException("奖品库存不足");
        }

        // 检查用户积分
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getPoints() == null || user.getPoints() < reward.getPointsRequired()) {
            throw new RuntimeException("积分不足，需要" + reward.getPointsRequired() + "积分");
        }

        // 扣除积分
        user.setPoints(user.getPoints() - reward.getPointsRequired());
        userMapper.updateById(user);

        // 减少库存
        if (reward.getStock() != null) {
            reward.setStock(reward.getStock() - 1);
        }
        reward.setTotalExchanged(reward.getTotalExchanged() + 1);
        rewardMapper.updateById(reward);

        // 创建兑换记录
        RewardExchange exchange = new RewardExchange();
        exchange.setUserId(userId);
        exchange.setRewardId(rewardId);
        exchange.setRewardName(reward.getName());
        exchange.setPointsUsed(reward.getPointsRequired());
        exchange.setStatus(0); // 待处理

        // 如果是虚拟奖品，生成兑换码
        if (reward.getType() == 3) {
            exchange.setExchangeCode(generateExchangeCode());
        }

        exchangeMapper.insert(exchange);

        return exchange;
    }

    @Override
    public List<RewardExchange> getUserExchanges(Long userId) {
        LambdaQueryWrapper<RewardExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RewardExchange::getUserId, userId);
        wrapper.orderByDesc(RewardExchange::getCreateTime);
        return exchangeMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> getUserPointsInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 统计兑换记录
        LambdaQueryWrapper<RewardExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RewardExchange::getUserId, userId);
        List<RewardExchange> exchanges = exchangeMapper.selectList(wrapper);

        int totalExchanged = exchanges.size();
        int totalPointsUsed = exchanges.stream()
                .mapToInt(RewardExchange::getPointsUsed)
                .sum();

        Map<String, Object> info = new HashMap<>();
        info.put("currentPoints", user.getPoints() != null ? user.getPoints() : 0);
        info.put("totalExchanged", totalExchanged);
        info.put("totalPointsUsed", totalPointsUsed);
        return info;
    }

    @Override
    public IPage<Reward> getAdminRewardList(Page<Reward> page, Integer type, String keyword, Integer status) {
        LambdaQueryWrapper<Reward> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(Reward::getType, type);
        }
        if (status != null) {
            wrapper.eq(Reward::getStatus, status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Reward::getName, keyword);
        }
        wrapper.orderByAsc(Reward::getSortOrder);
        wrapper.orderByDesc(Reward::getCreateTime);
        return rewardMapper.selectPage(page, wrapper);
    }

    @Override
    public Reward createReward(Map<String, Object> params) {
        Reward reward = new Reward();
        reward.setName(params.get("name").toString());
        reward.setDescription(params.get("description") != null ? params.get("description").toString() : null);
        reward.setImage(params.get("image") != null ? params.get("image").toString() : null);
        reward.setPointsRequired(Integer.valueOf(params.get("pointsRequired").toString()));
        reward.setStock(params.get("stock") != null ? Integer.valueOf(params.get("stock").toString()) : null);
        reward.setType(params.get("type") != null ? Integer.valueOf(params.get("type").toString()) : 1);
        reward.setStatus(params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : 1);
        reward.setSortOrder(params.get("sortOrder") != null ? Integer.valueOf(params.get("sortOrder").toString()) : 0);
        reward.setTotalExchanged(0);

        rewardMapper.insert(reward);
        return reward;
    }

    @Override
    public Reward updateReward(Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Reward reward = rewardMapper.selectById(id);
        if (reward == null) {
            throw new RuntimeException("奖品不存在");
        }

        if (params.get("name") != null) {
            reward.setName(params.get("name").toString());
        }
        if (params.get("description") != null) {
            reward.setDescription(params.get("description").toString());
        }
        if (params.get("image") != null) {
            reward.setImage(params.get("image").toString());
        }
        if (params.get("pointsRequired") != null) {
            reward.setPointsRequired(Integer.valueOf(params.get("pointsRequired").toString()));
        }
        if (params.get("stock") != null) {
            reward.setStock(Integer.valueOf(params.get("stock").toString()));
        }
        if (params.get("type") != null) {
            reward.setType(Integer.valueOf(params.get("type").toString()));
        }
        if (params.get("status") != null) {
            reward.setStatus(Integer.valueOf(params.get("status").toString()));
        }
        if (params.get("sortOrder") != null) {
            reward.setSortOrder(Integer.valueOf(params.get("sortOrder").toString()));
        }

        rewardMapper.updateById(reward);
        return reward;
    }

    @Override
    public void deleteReward(Long id) {
        rewardMapper.deleteById(id);
    }

    @Override
    public void processExchange(Long exchangeId, String exchangeCode, String remark) {
        RewardExchange exchange = exchangeMapper.selectById(exchangeId);
        if (exchange == null) {
            throw new RuntimeException("兑换记录不存在");
        }

        exchange.setStatus(1); // 已发放
        if (exchangeCode != null && !exchangeCode.trim().isEmpty()) {
            exchange.setExchangeCode(exchangeCode);
        }
        if (remark != null && !remark.trim().isEmpty()) {
            exchange.setRemark(remark);
        }

        exchangeMapper.updateById(exchange);
    }

    /**
     * 生成兑换码
     */
    private String generateExchangeCode() {
        return "REWARD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}

