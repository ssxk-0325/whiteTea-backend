package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.Order;
import com.fuding.entity.OrderReview;
import com.fuding.entity.User;
import com.fuding.mapper.OrderMapper;
import com.fuding.mapper.OrderReviewMapper;
import com.fuding.mapper.UserMapper;
import com.fuding.service.OrderReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderReviewServiceImpl extends ServiceImpl<OrderReviewMapper, OrderReview> implements OrderReviewService {

    private static final int ORDER_STATUS_COMPLETED = 3;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderReview createReview(Long userId, Long orderId, Integer rating, String content) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new RuntimeException("请选择1-5星评分");
        }
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该订单");
        }
        if (order.getStatus() == null || order.getStatus() != ORDER_STATUS_COMPLETED) {
            throw new RuntimeException("仅已完成订单可评价");
        }
        if (existsByOrderId(orderId)) {
            throw new RuntimeException("该订单已评价");
        }
        OrderReview r = new OrderReview();
        r.setOrderId(orderId);
        r.setUserId(userId);
        r.setRating(rating);
        r.setContent(content != null ? content.trim() : null);
        this.save(r);
        return r;
    }

    @Override
    public OrderReview getByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderReview> w = new LambdaQueryWrapper<>();
        w.eq(OrderReview::getOrderId, orderId);
        return this.getOne(w);
    }

    @Override
    public OrderReview getByOrderIdEnriched(Long orderId) {
        OrderReview r = getByOrderId(orderId);
        if (r == null) {
            return null;
        }
        User u = userMapper.selectById(r.getUserId());
        if (u != null) {
            r.setUserNickname(u.getNickname() != null && !u.getNickname().isEmpty() ? u.getNickname() : u.getUsername());
        }
        return r;
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderReview> w = new LambdaQueryWrapper<>();
        w.eq(OrderReview::getOrderId, orderId);
        return this.count(w) > 0;
    }

    @Override
    public List<OrderReview> listByProductId(Long productId, int limit) {
        List<OrderReview> list = baseMapper.listByProductId(productId, Math.min(Math.max(limit, 1), 50));
        Map<Long, OrderReview> uniq = new LinkedHashMap<>();
        for (OrderReview r : list) {
            uniq.putIfAbsent(r.getId(), r);
        }
        for (OrderReview r : uniq.values()) {
            User u = userMapper.selectById(r.getUserId());
            if (u != null) {
                r.setUserNickname(u.getNickname() != null && !u.getNickname().isEmpty() ? u.getNickname() : u.getUsername());
            }
        }
        return new ArrayList<>(uniq.values());
    }
}
