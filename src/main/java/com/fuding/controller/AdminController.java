package com.fuding.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fuding.common.Result;
import com.fuding.entity.Order;
import com.fuding.mapper.OrderMapper;
import com.fuding.mapper.ProductMapper;
import com.fuding.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台控制器
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 总用户数
            Long userCount = userMapper.selectCount(new LambdaQueryWrapper<>());
            stats.put("userCount", userCount);
            
            // 总产品数
            Long productCount = productMapper.selectCount(new LambdaQueryWrapper<>());
            stats.put("productCount", productCount);
            
            // 总订单数
            Long orderCount = orderMapper.selectCount(new LambdaQueryWrapper<>());
            stats.put("orderCount", orderCount);
            
            // 总销售额（已完成订单的总金额）
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getStatus, 2); // 已完成的订单
            BigDecimal totalSales = orderMapper.selectList(orderWrapper)
                .stream()
                .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalSales", totalSales);
            
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

