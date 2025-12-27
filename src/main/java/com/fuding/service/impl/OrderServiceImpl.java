package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.Cart;
import com.fuding.entity.Order;
import com.fuding.entity.OrderItem;
import com.fuding.entity.Product;
import com.fuding.mapper.CartMapper;
import com.fuding.mapper.OrderItemMapper;
import com.fuding.mapper.OrderMapper;
import com.fuding.mapper.ProductMapper;
import com.fuding.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 订单服务实现类
 */
@Service
@Transactional
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Order createOrder(Long userId, Long addressId, String receiverName, String receiverPhone, String receiverAddress, String remark) {
        // 获取购物车商品
        LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(Cart::getUserId, userId);
        List<Cart> cartList = cartMapper.selectList(cartWrapper);

        if (cartList == null || cartList.isEmpty()) {
            throw new RuntimeException("购物车为空，无法创建订单");
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setRemark(remark);
        order.setStatus(0); // 待付款
        order.setPayType(0); // 未支付

        // 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在：" + cart.getProductId());
            }
            if (product.getStock() < cart.getQuantity()) {
                throw new RuntimeException("商品库存不足：" + product.getName());
            }
            BigDecimal price = product.getPrice();
            BigDecimal quantity = new BigDecimal(cart.getQuantity());
            totalAmount = totalAmount.add(price.multiply(quantity));
        }

        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);

        // 保存订单
        orderMapper.insert(order);

        // 创建订单项并扣减库存
        for (Cart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(cart.getProductId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setSubtotal(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
            
            orderItemMapper.insert(orderItem);

            // 扣减库存
            product.setStock(product.getStock() - cart.getQuantity());
            product.setSales(product.getSales() + cart.getQuantity());
            productMapper.updateById(product);
        }

        // 清空购物车
        cartMapper.delete(cartWrapper);

        return order;
    }

    @Override
    public Order getOrderById(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return order;
    }

    @Override
    public List<Order> getUserOrders(Long userId, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        return orderMapper.selectList(wrapper);
    }

    @Override
    public void payOrder(Long orderId, Integer payType) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态不正确，无法支付");
        }
        order.setStatus(1); // 待发货
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    public void confirmReceive(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != 2) {
            throw new RuntimeException("订单状态不正确，无法确认收货");
        }
        order.setStatus(3); // 已完成
        order.setCompleteTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态不正确，无法取消");
        }
        order.setStatus(4); // 已取消
        
        // 恢复库存
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSales(product.getSales() - item.getQuantity());
                productMapper.updateById(product);
            }
        }
        
        orderMapper.updateById(order);
    }

    @Override
    public void shipOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态不正确，无法发货");
        }
        order.setStatus(2); // 待收货
        order.setShipTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "FT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

