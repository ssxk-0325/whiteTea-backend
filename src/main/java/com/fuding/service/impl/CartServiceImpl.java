package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

        // 先尝试恢复逻辑删除的记录
        int restored = cartMapper.restoreDeletedCart(userId, productId);
        
        // 检查购物车中是否已有该商品（包括刚恢复的）
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.eq(Cart::getProductId, productId);
        Cart existingCart = cartMapper.selectOne(wrapper);
        
        if (existingCart != null) {
            // 如果恢复了记录，数量从0开始；否则累加
            if (restored > 0) {
                existingCart.setQuantity(quantity);
            } else {
                existingCart.setQuantity(existingCart.getQuantity() + quantity);
            }
            existingCart.setSelected(1); // 恢复时设置为选中
            cartMapper.updateById(existingCart);
            return existingCart;
        } else {
            // 不存在则插入新记录
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
        List<Cart> cartList = cartMapper.selectList(wrapper);
        
        // 关联查询产品信息
        for (Cart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());
            cart.setProduct(product);
        }
        
        return cartList;
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
