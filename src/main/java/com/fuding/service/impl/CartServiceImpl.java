package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.Cart;
import com.fuding.entity.Product;
import com.fuding.mapper.CartMapper;
import com.fuding.mapper.ProductMapper;
import com.fuding.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 购物车服务实现类
 */
@Service
@Transactional
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Cart addToCart(Long userId, Long productId, Integer quantity) {
        // 检查产品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("产品不存在");
        }

        // 检查库存
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }

        // 检查购物车中是否已有该商品
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.eq(Cart::getProductId, productId);
        Cart existingCart = cartMapper.selectOne(wrapper);
        
        if (existingCart != null) {
            existingCart.setQuantity(existingCart.getQuantity() + quantity);
            cartMapper.updateById(existingCart);
            return existingCart;
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setSelected(1);
            cartMapper.insert(cart);
            return cart;
        }
    }

    @Override
    public List<Cart> getUserCart(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        return cartMapper.selectList(wrapper);
    }

    @Override
    public Cart updateQuantity(Long cartId, Integer quantity) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            throw new RuntimeException("购物车项不存在");
        }

        // 检查库存
        Product product = productMapper.selectById(cart.getProductId());
        if (product == null) {
            throw new RuntimeException("产品不存在");
        }
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }

        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
        return cart;
    }

    @Override
    public void deleteCartItem(Long cartId) {
        cartMapper.deleteById(cartId);
    }

    @Override
    public void clearCart(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        cartMapper.delete(wrapper);
    }
}
