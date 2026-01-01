package com.fuding.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fuding.entity.Order;
import com.fuding.mapper.OrderMapper;
import com.fuding.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 订单自动发货定时任务
 * 检查超过48小时未发货的订单，自动发货
 * 
 * @author 陈泳铭
 */
@Component
public class OrderAutoShipTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderAutoShipTask.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    /**
     * 定时任务：每小时执行一次，检查超过48小时未发货的订单并自动发货
     * cron表达式：0 0 * * * ? 表示每小时的第0分钟执行
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void autoShipOrders() {
        logger.info("开始执行订单自动发货定时任务...");
        
        try {
            // 查询状态为1（待发货）的所有订单
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getStatus, 1); // 待发货状态
            wrapper.isNull(Order::getShipTime); // 未发货的订单
            List<Order> orders = orderMapper.selectList(wrapper);

            if (orders == null || orders.isEmpty()) {
                logger.info("没有待发货的订单");
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            int autoShippedCount = 0;

            for (Order order : orders) {
                // 检查订单支付时间是否超过48小时
                // 优先使用支付时间，如果没有支付时间则使用创建时间（理论上待发货订单应该有支付时间）
                LocalDateTime checkTime = order.getPayTime() != null ? order.getPayTime() : order.getCreateTime();
                
                if (checkTime != null) {
                    long hours = ChronoUnit.HOURS.between(checkTime, now);
                    
                    if (hours >= 48) {
                        try {
                            // 自动发货
                            orderService.shipOrder(order.getId());
                            autoShippedCount++;
                            logger.info("订单 {} 超过48小时未发货，已自动发货。{}时间：{}，当前时间：{}", 
                                    order.getOrderNo(), 
                                    order.getPayTime() != null ? "支付" : "创建",
                                    checkTime, 
                                    now);
                        } catch (Exception e) {
                            logger.error("自动发货订单 {} 失败：{}", order.getOrderNo(), e.getMessage());
                        }
                    }
                }
            }

            logger.info("订单自动发货任务完成，共处理 {} 个订单，自动发货 {} 个订单", orders.size(), autoShippedCount);
        } catch (Exception e) {
            logger.error("执行订单自动发货定时任务时发生错误：", e);
        }
    }
}

