package com.fuding.service;

import com.fuding.entity.Order;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     * @param deliveryType 配送方式：1-线上配送，2-线下自提
     * @param storeId 自提门店ID（自提时必填）
     * @param addressId 收货地址ID（配送时必填）
     * @param cartIds 要结算的购物车项 id，null 或空表示整单结算（兼容旧接口）
     */
    Order createOrder(Long userId, Integer deliveryType, Long storeId, Long addressId, String receiverName, String receiverPhone, String receiverAddress, String remark, List<Long> cartIds, Long couponId, Integer orderMode);

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
     * 支付宝异步通知成功后标记订单已支付（幂等，按商户订单号 out_trade_no）
     */
    void payOrderFromAlipayNotify(String outTradeNo, String totalAmountStr);

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
    List<Order> getAllOrders(Integer status, String keyword, Long userId);
}

