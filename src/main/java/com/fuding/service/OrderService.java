package com.fuding.service;

import com.fuding.entity.Order;
import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     */
    Order createOrder(Long userId, Long addressId, String receiverName, String receiverPhone, String receiverAddress, String remark);

    /**
     * 根据ID获取订单
     */
    Order getOrderById(Long orderId);

    /**
     * 获取用户订单列表
     */
    List<Order> getUserOrders(Long userId, Integer status);

    /**
     * 支付订单
     */
    void payOrder(Long orderId, Integer payType);

    /**
     * 确认收货
     */
    void confirmReceive(Long orderId);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId);

    /**
     * 发货
     */
    void shipOrder(Long orderId);

    /**
     * 获取所有订单列表（管理后台使用）
     */
    List<Order> getAllOrders(Integer status);
}

