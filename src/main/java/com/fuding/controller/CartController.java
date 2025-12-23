package com.fuding.controller;

import com.fuding.common.Result;
import com.fuding.entity.Cart;
import com.fuding.service.CartService;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/cart")
@CrossOrigin
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public Result<Cart> addToCart(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            Long productId = Long.valueOf(params.get("productId").toString());
            Integer quantity = Integer.valueOf(params.get("quantity").toString());

            Cart cart = cartService.addToCart(userId, productId, quantity);
            return Result.success("添加成功", cart);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户购物车
     */
    @GetMapping("/list")
    public Result<List<Cart>> getCartList(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            List<Cart> cartList = cartService.getUserCart(userId);
            return Result.success(cartList);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/update")
    public Result<Cart> updateQuantity(@RequestBody Map<String, Object> params) {
        try {
            Long cartId = Long.valueOf(params.get("cartId").toString());
            Integer quantity = Integer.valueOf(params.get("quantity").toString());
            Cart cart = cartService.updateQuantity(cartId, quantity);
            return Result.success("更新成功", cart);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCartItem(@PathVariable Long id) {
        try {
            cartService.deleteCartItem(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public Result<Void> clearCart(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            cartService.clearCart(userId);
            return Result.success("清空成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

