package com.fuding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.common.Result;
import com.fuding.entity.Product;
import com.fuding.service.ProductFavoriteService;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 商品收藏（路径独立于 /product/{id}，避免路由冲突）
 */
@RestController
@RequestMapping("/product/favorite")
@CrossOrigin
public class ProductFavoriteController {

    @Autowired
    private ProductFavoriteService productFavoriteService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long requireUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    @PostMapping("/{productId}")
    public Result<Void> add(@PathVariable Long productId, HttpServletRequest request) {
        try {
            Long userId = requireUserId(request);
            productFavoriteService.addFavorite(userId, productId);
            return Result.success("收藏成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{productId}")
    public Result<Void> remove(@PathVariable Long productId, HttpServletRequest request) {
        try {
            Long userId = requireUserId(request);
            productFavoriteService.removeFavorite(userId, productId);
            return Result.success("已取消收藏", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/check")
    public Result<Map<String, Object>> check(@RequestParam Long productId, HttpServletRequest request) {
        try {
            Long userId = requireUserId(request);
            boolean favorited = productFavoriteService.isFavorite(userId, productId);
            Map<String, Object> data = new HashMap<>();
            data.put("favorited", favorited);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            Long userId = requireUserId(request);
            int cur = page != null && page > 0 ? page : 1;
            int s = size != null && size > 0 ? size : 10;
            Page<Product> result = productFavoriteService.pageFavoriteProducts(userId, cur, s);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
