package com.fuding.service;

import com.fuding.entity.Cart;
import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 添加商品到购物车
     */
    Cart addToCart(Long userId, Long productId, Integer quantity);

    /**
     * 获取用户购物车
     */
    List<Cart> getUserCart(Long userId);

    /**
     * 更新购物车商品数量
     */
    Cart updateQuantity(Long cartId, Integer quantity);

    /**
     * 删除购物车商品
     */
    void deleteCartItem(Long cartId);

    /**
     * 清空购物车
     */
    void clearCart(Long userId);
}

