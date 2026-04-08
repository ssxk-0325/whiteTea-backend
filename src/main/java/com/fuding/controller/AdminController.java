package com.fuding.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fuding.common.Result;
import com.fuding.entity.Order;
import com.fuding.entity.User;
import com.fuding.mapper.OrderMapper;
import com.fuding.mapper.ProductMapper;
import com.fuding.mapper.UserMapper;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private JwtUtil jwtUtil;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    private String ensureAdmin(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Long userId = jwtUtil.getUserIdFromToken(token);
                User user = userMapper.selectById(userId);
                if (user != null && user.getUserType() != null && user.getUserType() == 1) {
                    return null;
                }
                return "无权限";
            } catch (Exception e) {
                return "Token 无效";
            }
        }
        return "请先登录";
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(HttpServletRequest request) {
        try {
            String authError = ensureAdmin(request);
            if (authError != null) return Result.error(authError);

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
            orderWrapper.eq(Order::getStatus, 3); // 已完成的订单
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

    /**
     * 获取图表数据
     */
    @GetMapping("/chart-data")
    public Result<Map<String, Object>> getChartData(HttpServletRequest request) {
        try {
            String authError = ensureAdmin(request);
            if (authError != null) return Result.error(authError);

            Map<String, Object> chartData = new HashMap<>();
            
            // 1. 订单状态分布（饼图）
            Map<String, Long> orderStatusMap = new HashMap<>();
            orderStatusMap.put("待付款", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 0)));
            orderStatusMap.put("待发货", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1)));
            orderStatusMap.put("待收货", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 2)));
            orderStatusMap.put("已完成", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 3)));
            orderStatusMap.put("已取消", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 4)));
            
            List<Map<String, Object>> orderStatusData = new ArrayList<>();
            Map<String, Object> status1 = new HashMap<>();
            status1.put("name", "待付款");
            status1.put("value", orderStatusMap.get("待付款"));
            orderStatusData.add(status1);
            
            Map<String, Object> status2 = new HashMap<>();
            status2.put("name", "待发货");
            status2.put("value", orderStatusMap.get("待发货"));
            orderStatusData.add(status2);
            
            Map<String, Object> status3 = new HashMap<>();
            status3.put("name", "待收货");
            status3.put("value", orderStatusMap.get("待收货"));
            orderStatusData.add(status3);
            
            Map<String, Object> status4 = new HashMap<>();
            status4.put("name", "已完成");
            status4.put("value", orderStatusMap.get("已完成"));
            orderStatusData.add(status4);
            
            Map<String, Object> status5 = new HashMap<>();
            status5.put("name", "已取消");
            status5.put("value", orderStatusMap.get("已取消"));
            orderStatusData.add(status5);
            chartData.put("orderStatus", orderStatusData);
            
            // 2. 月度销售额（柱状图）- 最近6个月
            List<Map<String, Object>> monthlySales = new ArrayList<>();
            LocalDate now = LocalDate.now();
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("M月");
            
            for (int i = 5; i >= 0; i--) {
                LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
                LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                LocalDateTime startDateTime = monthStart.atStartOfDay();
                LocalDateTime endDateTime = monthEnd.atTime(23, 59, 59);
                
                LambdaQueryWrapper<Order> salesWrapper = new LambdaQueryWrapper<>();
                salesWrapper.eq(Order::getStatus, 3); // 已完成的订单
                salesWrapper.ge(Order::getCreateTime, startDateTime);
                salesWrapper.le(Order::getCreateTime, endDateTime);
                
                List<Order> orders = orderMapper.selectList(salesWrapper);
                BigDecimal totalSales = orders.stream()
                    .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                Map<String, Object> salesItem = new HashMap<>();
                salesItem.put("month", monthStart.format(monthFormatter));
                salesItem.put("sales", totalSales.doubleValue());
                monthlySales.add(salesItem);
            }
            chartData.put("monthlySales", monthlySales);
            
            // 3. 用户增长趋势（折线图）- 最近7天
            List<Map<String, Object>> userGrowth = new ArrayList<>();
            
            for (int i = 6; i >= 0; i--) {
                LocalDate date = now.minusDays(i);
                LocalDateTime dayStart = date.atStartOfDay();
                LocalDateTime dayEnd = date.atTime(23, 59, 59);
                
                LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.ge(User::getCreateTime, dayStart);
                userWrapper.le(User::getCreateTime, dayEnd);
                
                Long userCount = userMapper.selectCount(userWrapper);
                
                // 格式化日期为周几
                String[] weekDays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
                int dayOfWeek = date.getDayOfWeek().getValue();
                // Java的DayOfWeek从周一开始是1，周日是7，需要调整
                int index = (dayOfWeek == 7) ? 6 : dayOfWeek - 1;
                String dayLabel = weekDays[index];
                
                Map<String, Object> growthItem = new HashMap<>();
                growthItem.put("date", dayLabel);
                growthItem.put("count", userCount);
                userGrowth.add(growthItem);
            }
            chartData.put("userGrowth", userGrowth);
            
            return Result.success(chartData);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

