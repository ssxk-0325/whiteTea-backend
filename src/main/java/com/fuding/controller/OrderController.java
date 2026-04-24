package com.fuding.controller;

import com.fuding.common.Result;
import com.fuding.config.AlipayProperties;
import com.fuding.entity.Order;
import com.fuding.entity.OrderItem;
import com.fuding.entity.OrderReview;
import com.fuding.mapper.OrderItemMapper;
import com.fuding.service.AlipayTradeService;
import com.fuding.service.DeliveryTrackService;
import com.fuding.service.OrderReviewService;
import com.fuding.service.OrderService;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
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

    @Autowired
    private OrderReviewService orderReviewService;

    @Autowired
    private DeliveryTrackService deliveryTrackService;

    @Autowired
    private AlipayTradeService alipayTradeService;

    @Autowired
    private AlipayProperties alipayProperties;

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

            Integer deliveryType = params.get("deliveryType") != null ? Integer.valueOf(params.get("deliveryType").toString()) : 1;
            Long storeId = params.get("storeId") != null ? Long.valueOf(params.get("storeId").toString()) : null;
            Long addressId = params.get("addressId") != null ? Long.valueOf(params.get("addressId").toString()) : null;
            String receiverName = params.get("receiverName") != null ? params.get("receiverName").toString() : "";
            String receiverPhone = params.get("receiverPhone") != null ? params.get("receiverPhone").toString() : "";
            String receiverAddress = params.get("receiverAddress") != null ? params.get("receiverAddress").toString() : "";
            String remark = params.get("remark") != null ? params.get("remark").toString() : "";

            List<Long> cartIds = null;
            Object cartIdsObj = params.get("cartIds");
            if (cartIdsObj instanceof List) {
                List<?> rawList = (List<?>) cartIdsObj;
                cartIds = new ArrayList<>();
                for (Object o : rawList) {
                    if (o != null) {
                        cartIds.add(Long.valueOf(o.toString()));
                    }
                }
            }

            Long couponId = params.get("couponId") != null ? Long.valueOf(params.get("couponId").toString()) : null;
            Integer orderMode = params.get("orderMode") != null ? Integer.valueOf(params.get("orderMode").toString()) : 0;
            Order order = orderService.createOrder(userId, deliveryType, storeId, addressId, receiverName, receiverPhone, receiverAddress, remark, cartIds, couponId, orderMode);
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
            data.put("review", orderReviewService.getByOrderIdEnriched(id));

            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 提交订单评价（仅已完成订单）
     */
    @PostMapping("/{id}/review")
    public Result<OrderReview> submitOrderReview(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            Integer rating = params.get("rating") != null ? Integer.valueOf(params.get("rating").toString()) : null;
            String content = params.get("content") != null ? params.get("content").toString() : null;
            OrderReview review = orderReviewService.createReview(userId, id, rating, content);
            return Result.success("评价成功", review);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 同城配送模拟轨迹：发货后展示与收货点的直线距离，最多 5 次位置变化后送达（演示数据）
     */
    @GetMapping("/{id}/delivery-track")
    public Result<Map<String, Object>> getDeliveryTrack(@PathVariable Long id, HttpServletRequest request) {
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

            Map<String, Object> track = deliveryTrackService.buildDeliveryTrack(order);
            if (track == null) {
                return Result.success("无配送轨迹", null);
            }
            return Result.success(track);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取订单评价
     */
    @GetMapping("/{id}/review")
    public Result<OrderReview> getOrderReview(@PathVariable Long id, HttpServletRequest request) {
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
            return Result.success(orderReviewService.getByOrderIdEnriched(id));
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
                orderMap.put("deliveryType", order.getDeliveryType());
                orderMap.put("storeId", order.getStoreId());
                orderMap.put("createTime", order.getCreateTime());
                orderMap.put("payTime", order.getPayTime());
                orderMap.put("shipTime", order.getShipTime());
                orderMap.put("completeTime", order.getCompleteTime());
                orderMap.put("remark", order.getRemark());
                orderMap.put("couponId", order.getCouponId());
                orderMap.put("couponCode", order.getCouponCode());
                orderMap.put("orderMode", order.getOrderMode());
                orderMap.put("groupDiscountAmount", order.getGroupDiscountAmount());
                orderMap.put("wholesaleDiscountAmount", order.getWholesaleDiscountAmount());
                orderMap.put("couponDiscountAmount", order.getCouponDiscountAmount());
                orderMap.put("discountAmount", order.getDiscountAmount());
                orderMap.put("rewardPoints", order.getRewardPoints());

                // 获取订单项
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem> wrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                wrapper.eq(OrderItem::getOrderId, order.getId());
                List<OrderItem> items = orderItemMapper.selectList(wrapper);
                orderMap.put("items", items);

                if (order.getStatus() != null && order.getStatus() == 3) {
                    orderMap.put("hasReview", orderReviewService.existsByOrderId(order.getId()));
                } else {
                    orderMap.put("hasReview", false);
                }

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
     * 支付宝电脑网站支付：返回自动提交表单 HTML，前端写入页面后提交跳转收银台
     */
    @PostMapping("/{id}/alipay/pay")
    public Result<Map<String, String>> alipayPagePay(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            String formHtml = alipayTradeService.buildPagePayForm(userId, id);
            Map<String, String> data = new HashMap<>();
            data.put("formHtml", formHtml);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 主动查询支付宝订单并补单（内网穿透地址变更导致异步通知未送达时使用）
     */
    @PostMapping("/{id}/alipay/sync")
    public Result<Void> alipaySyncPayStatus(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            alipayTradeService.syncPayStatusFromAlipay(userId, id);
            return Result.success("已同步支付状态", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 支付宝异步通知（验签成功后更新订单）
     */
    @PostMapping(value = "/alipay/notify", produces = "text/plain;charset=UTF-8")
    public String alipayNotify(HttpServletRequest request) {
        boolean ok = alipayTradeService.handleNotify(request);
        return ok ? "success" : "fail";
    }

    /**
     * 支付宝同步跳转（用户支付完成后回到商户，仅用于前端跳转）
     */
    @GetMapping("/alipay/return")
    public void alipayReturn(HttpServletResponse response) throws IOException {
        String url = alipayProperties.getFrontendRedirectUrl();
        if (url == null || url.isEmpty()) {
            url = "/";
        }
        response.sendRedirect(url);
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
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId) {
        try {
            List<Order> orders = orderService.getAllOrders(status, keyword, userId);
            
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
                orderMap.put("deliveryType", order.getDeliveryType());
                orderMap.put("storeId", order.getStoreId());
                orderMap.put("createTime", order.getCreateTime());
                orderMap.put("payTime", order.getPayTime());
                orderMap.put("shipTime", order.getShipTime());
                orderMap.put("completeTime", order.getCompleteTime());
                orderMap.put("remark", order.getRemark());
                orderMap.put("couponId", order.getCouponId());
                orderMap.put("couponCode", order.getCouponCode());
                orderMap.put("orderMode", order.getOrderMode());
                orderMap.put("groupDiscountAmount", order.getGroupDiscountAmount());
                orderMap.put("wholesaleDiscountAmount", order.getWholesaleDiscountAmount());
                orderMap.put("couponDiscountAmount", order.getCouponDiscountAmount());
                orderMap.put("discountAmount", order.getDiscountAmount());
                orderMap.put("rewardPoints", order.getRewardPoints());

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

    /**
     * 管理后台：确认收货（将待收货订单改为已完成）
     */
    @PostMapping("/admin/{id}/confirm")
    public Result<Void> adminConfirmReceive(@PathVariable Long id) {
        try {
            orderService.confirmReceive(id);
            return Result.success("确认收货成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理后台：取消订单（仅待付款订单）
     */
    @PostMapping("/admin/{id}/cancel")
    public Result<Void> adminCancelOrder(@PathVariable Long id) {
        try {
            orderService.cancelOrder(id);
            return Result.success("取消订单成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理后台：配送模拟轨迹（与前台逻辑一致）
     */
    @GetMapping("/admin/{id}/delivery-track")
    public Result<Map<String, Object>> adminGetDeliveryTrack(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            Map<String, Object> track = deliveryTrackService.buildDeliveryTrack(order);
            if (track == null) {
                return Result.success("无配送轨迹", null);
            }
            return Result.success(track);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

