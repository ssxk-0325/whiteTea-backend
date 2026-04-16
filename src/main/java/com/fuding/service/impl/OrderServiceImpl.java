package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.Address;
import com.fuding.entity.Cart;
import com.fuding.entity.Order;
import com.fuding.entity.OrderItem;
import com.fuding.entity.Product;
import com.fuding.entity.Store;
import com.fuding.entity.Reward;
import com.fuding.entity.RewardExchange;
import com.fuding.entity.User;
import com.fuding.mapper.AddressMapper;
import com.fuding.mapper.CartMapper;
import com.fuding.mapper.OrderItemMapper;
import com.fuding.mapper.OrderMapper;
import com.fuding.mapper.ProductMapper;
import com.fuding.mapper.StoreMapper;
import com.fuding.mapper.RewardExchangeMapper;
import com.fuding.mapper.RewardMapper;
import com.fuding.mapper.UserMapper;
import com.fuding.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private RewardExchangeMapper rewardExchangeMapper;

    @Autowired
    private RewardMapper rewardMapper;

    @Override
    public Order createOrder(Long userId, Integer deliveryType, Long storeId, Long addressId, String receiverName, String receiverPhone, String receiverAddress, String remark, List<Long> cartIds, Long couponId, Integer orderMode) {
        // 获取购物车商品（可指定购物车项 id，仅结算选中商品）
        LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(Cart::getUserId, userId);
        if (cartIds != null && !cartIds.isEmpty()) {
            cartWrapper.in(Cart::getId, cartIds);
        }
        List<Cart> cartList = cartMapper.selectList(cartWrapper);

        if (cartList == null || cartList.isEmpty()) {
            throw new RuntimeException("购物车为空，无法创建订单");
        }
        if (cartIds != null && !cartIds.isEmpty() && cartList.size() != cartIds.size()) {
            throw new RuntimeException("部分购物车项不存在或已失效，请刷新购物车后重试");
        }

        if (deliveryType == null) {
            deliveryType = 1;
        }
        if (deliveryType == 2) {
            // 线下自提：校验门店并填充收货信息
            if (storeId == null) {
                throw new RuntimeException("请选择自提门店");
            }
            Store store = storeMapper.selectById(storeId);
            if (store == null) {
                throw new RuntimeException("门店不存在");
            }
            User user = userMapper.selectById(userId);
            receiverName = (user != null && user.getNickname() != null && !user.getNickname().isEmpty()) ? user.getNickname() : "到店自提";
            receiverPhone = (user != null && user.getPhone() != null) ? user.getPhone() : "";
            receiverAddress = store.getName() + " " + (store.getAddress() != null ? store.getAddress() : "");
        } else {
            // 线上配送：校验地址
            if (addressId == null) {
                throw new RuntimeException("请选择收货地址");
            }
            Address address = addressMapper.selectById(addressId);
            if (address == null) {
                throw new RuntimeException("收货地址不存在");
            }
            receiverName = address.getReceiverName();
            receiverPhone = address.getReceiverPhone();
            receiverAddress = (address.getProvince() != null ? address.getProvince() : "") + (address.getCity() != null ? address.getCity() : "") + (address.getDistrict() != null ? address.getDistrict() : "") + (address.getDetail() != null ? address.getDetail() : "");
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setDeliveryType(deliveryType);
        order.setStoreId(deliveryType == 2 ? storeId : null);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setRemark(remark);
        order.setStatus(0); // 待付款
        order.setPayType(0); // 未支付

        // 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
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
            totalQuantity += cart.getQuantity();
        }

        if (orderMode == null) {
            orderMode = 0;
        }
        BigDecimal groupDiscountAmount = BigDecimal.ZERO;
        if (orderMode == 1) {
            // 拼团统一 9 折
            groupDiscountAmount = totalAmount.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal wholesaleRate = BigDecimal.ZERO;
        if (totalQuantity >= 20) {
            wholesaleRate = new BigDecimal("0.15");
        } else if (totalQuantity >= 10) {
            wholesaleRate = new BigDecimal("0.08");
        }
        BigDecimal wholesaleDiscountAmount = totalAmount.multiply(wholesaleRate).setScale(2, RoundingMode.HALF_UP);

        BigDecimal couponDiscountAmount = BigDecimal.ZERO;
        RewardExchange mallCouponExchange = null;
        Reward mallCouponReward = null;
        if (couponId != null) {
            mallCouponExchange = rewardExchangeMapper.selectById(couponId);
            if (mallCouponExchange == null || !mallCouponExchange.getUserId().equals(userId)) {
                throw new RuntimeException("优惠券不存在");
            }
            if (mallCouponExchange.getOrderId() != null) {
                throw new RuntimeException("该优惠券已使用");
            }
            if (mallCouponExchange.getStatus() != null && mallCouponExchange.getStatus() == 2) {
                throw new RuntimeException("优惠券已取消");
            }
            mallCouponReward = rewardMapper.selectById(mallCouponExchange.getRewardId());
            if (mallCouponReward == null || mallCouponReward.getType() == null || mallCouponReward.getType() != 2) {
                throw new RuntimeException("仅支持使用积分商城兑换的优惠券");
            }
            Integer rule = mallCouponReward.getCouponDiscountType() != null ? mallCouponReward.getCouponDiscountType() : 1;
            couponDiscountAmount = calculateCouponDiscount(rule, totalAmount);
        }

        BigDecimal discountAmount = groupDiscountAmount.add(wholesaleDiscountAmount).add(couponDiscountAmount);
        if (discountAmount.compareTo(totalAmount) > 0) {
            discountAmount = totalAmount;
        }
        BigDecimal payAmount = totalAmount.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        int rewardPoints = payAmount.setScale(0, RoundingMode.DOWN).intValue();
        if (orderMode == 1) {
            rewardPoints += 10;
        }

        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setOrderMode(orderMode);
        order.setGroupDiscountAmount(groupDiscountAmount);
        order.setWholesaleDiscountAmount(wholesaleDiscountAmount);
        order.setCouponDiscountAmount(couponDiscountAmount);
        order.setDiscountAmount(discountAmount);
        order.setRewardPoints(rewardPoints);
        if (mallCouponExchange != null) {
            order.setCouponId(mallCouponExchange.getId());
            String code = mallCouponExchange.getExchangeCode();
            order.setCouponCode(code != null ? code : "");
        }

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

        // 仅删除已下单的购物车项
        cartMapper.delete(cartWrapper);

        if (mallCouponExchange != null) {
            mallCouponExchange.setOrderId(order.getId());
            rewardExchangeMapper.updateById(mallCouponExchange);
        }

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
        applyPaidOrder(order, payType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrderFromAlipayNotify(String outTradeNo, String totalAmountStr) {
        LambdaQueryWrapper<Order> w = new LambdaQueryWrapper<>();
        w.eq(Order::getOrderNo, outTradeNo);
        Order order = orderMapper.selectOne(w);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 0) {
            return;
        }
        BigDecimal paid = new BigDecimal(totalAmountStr);
        if (order.getPayAmount().compareTo(paid) != 0) {
            throw new RuntimeException("支付金额与订单不一致");
        }
        applyPaidOrder(order, 2);
    }

    private void applyPaidOrder(Order order, Integer payType) {
        order.setStatus(1); // 待发货
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            int current = user.getPoints() == null ? 0 : user.getPoints();
            int add = order.getRewardPoints() == null ? 0 : order.getRewardPoints();
            user.setPoints(current + Math.max(add, 0));
            userMapper.updateById(user);
        }
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

    @Override
    public List<Order> getAllOrders(Integer status, String keyword, Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim();
            wrapper.and(w -> w.like(Order::getOrderNo, k)
                    .or().like(Order::getReceiverPhone, k)
                    .or().like(Order::getReceiverName, k));
        }
        wrapper.orderByDesc(Order::getCreateTime);
        return orderMapper.selectList(wrapper);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "FT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private BigDecimal calculateCouponDiscount(Integer couponType, BigDecimal totalAmount) {
        if (couponType == null || totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount;
        switch (couponType) {
            case 1:
                discount = new BigDecimal("20");
                break;
            case 2:
                discount = new BigDecimal("30");
                break;
            case 3:
                discount = new BigDecimal("50");
                break;
            case 4:
                discount = new BigDecimal("80");
                break;
            case 5:
                discount = totalAmount.multiply(new BigDecimal("0.05"));
                break;
            case 6:
                discount = totalAmount.multiply(new BigDecimal("0.12"));
                break;
            default:
                discount = new BigDecimal("10");
        }
        if (discount.compareTo(totalAmount) > 0) {
            return totalAmount;
        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }
}

