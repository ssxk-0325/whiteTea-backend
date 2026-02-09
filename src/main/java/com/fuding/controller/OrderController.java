package com.fuding.controller;

import com.fuding.common.Result;
import com.fuding.entity.Order;
import com.fuding.entity.OrderItem;
import com.fuding.mapper.OrderItemMapper;
import com.fuding.service.OrderService;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<Order> createOrder(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            Long addressId = Long.valueOf(params.get("addressId").toString());
            String receiverName = params.get("receiverName").toString();
            String receiverPhone = params.get("receiverPhone").toString();
            String receiverAddress = params.get("receiverAddress").toString();
            String remark = params.get("remark") != null ? params.get("remark").toString() : "";

            Order order = orderService.createOrder(userId, addressId, receiverName, receiverPhone, receiverAddress, remark);
            return Result.success("订单创建成功", order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            Order order = orderService.getOrderById(id);
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权访问该订单");
            }

            // 获取订单项
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(OrderItem::getOrderId, id);
            List<OrderItem> items = orderItemMapper.selectList(wrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("order", order);
            data.put("items", items);

            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getOrderList(
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            List<Order> orders = orderService.getUserOrders(userId, status);
            
            // 为每个订单添加订单项
            List<Map<String, Object>> result = orders.stream().map(order -> {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("orderNo", order.getOrderNo());
                orderMap.put("totalAmount", order.getTotalAmount());
                orderMap.put("payAmount", order.getPayAmount());
                orderMap.put("status", order.getStatus());
                orderMap.put("payType", order.getPayType());
                orderMap.put("receiverName", order.getReceiverName());
                orderMap.put("receiverPhone", order.getReceiverPhone());
                orderMap.put("receiverAddress", order.getReceiverAddress());
                orderMap.put("createTime", order.getCreateTime());
                orderMap.put("payTime", order.getPayTime());
                orderMap.put("shipTime", order.getShipTime());
                orderMap.put("completeTime", order.getCompleteTime());
                orderMap.put("remark", order.getRemark());

                // 获取订单项
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem> wrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                wrapper.eq(OrderItem::getOrderId, order.getId());
                List<OrderItem> items = orderItemMapper.selectList(wrapper);
                orderMap.put("items", items);

                return orderMap;
            }).collect(Collectors.toList());

            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 支付订单
     */
    @PostMapping("/{id}/pay")
    public Result<Void> payOrder(@PathVariable Long id, @RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            Order order = orderService.getOrderById(id);
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权操作该订单");
            }

            Integer payType = Integer.valueOf(params.get("payType").toString());
            orderService.payOrder(id, payType);
            return Result.success("支付成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 确认收货
     */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmReceive(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            Order order = orderService.getOrderById(id);
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权操作该订单");
            }

            orderService.confirmReceive(id);
            return Result.success("确认收货成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            Order order = orderService.getOrderById(id);
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权操作该订单");
            }

            orderService.cancelOrder(id);
            return Result.success("订单取消成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理后台：获取所有订单列表
     */
    @GetMapping("/admin/list")
    public Result<List<Map<String, Object>>> getAllOrders(
            @RequestParam(required = false) Integer status) {
        try {
            List<Order> orders = orderService.getAllOrders(status);
            
            // 为每个订单添加订单项
            List<Map<String, Object>> result = orders.stream().map(order -> {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("orderNo", order.getOrderNo());
                orderMap.put("userId", order.getUserId());
                orderMap.put("totalAmount", order.getTotalAmount());
                orderMap.put("payAmount", order.getPayAmount());
                orderMap.put("status", order.getStatus());
                orderMap.put("payType", order.getPayType());
                orderMap.put("receiverName", order.getReceiverName());
                orderMap.put("receiverPhone", order.getReceiverPhone());
                orderMap.put("receiverAddress", order.getReceiverAddress());
                orderMap.put("createTime", order.getCreateTime());
                orderMap.put("payTime", order.getPayTime());
                orderMap.put("shipTime", order.getShipTime());
                orderMap.put("completeTime", order.getCompleteTime());
                orderMap.put("remark", order.getRemark());

                // 获取订单项
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem> wrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                wrapper.eq(OrderItem::getOrderId, order.getId());
                List<OrderItem> items = orderItemMapper.selectList(wrapper);
                orderMap.put("items", items);

                return orderMap;
            }).collect(Collectors.toList());

            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理后台：获取订单详情
     */
    @GetMapping("/admin/{id}")
    public Result<Map<String, Object>> adminGetOrderDetail(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            
            // 获取订单项
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(OrderItem::getOrderId, id);
            List<OrderItem> items = orderItemMapper.selectList(wrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("order", order);
            data.put("items", items);

            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理后台：发货
     */
    @PostMapping("/admin/{id}/ship")
    public Result<Void> adminShipOrder(@PathVariable Long id) {
        try {
            orderService.shipOrder(id);
            return Result.success("发货成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

