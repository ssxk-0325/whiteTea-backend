package com.fuding.controller;

import com.fuding.common.Result;
import com.fuding.entity.OrderReview;
import com.fuding.entity.Product;
import com.fuding.entity.User;
import com.fuding.mapper.UserMapper;
import com.fuding.service.OrderReviewService;
import com.fuding.service.ProductService;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 产品控制器
 */
@RestController
@RequestMapping("/product")
@CrossOrigin
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderReviewService orderReviewService;

    /**
     * 商品相关订单评价（用于详情页展示）
     */
    @GetMapping("/reviews")
    public Result<List<OrderReview>> getProductReviews(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            return Result.success(orderReviewService.listByProductId(productId, size));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询产品
     */
    @GetMapping("/list")
    public Result<Page<Product>> getProductList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
            Page<Product> products = productService.findProducts(pageable, categoryId, keyword);
            return Result.success(products);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取产品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.findById(id);
            return Result.success(product);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取热门产品
     */
    @GetMapping("/hot")
    public Result<Page<Product>> getHotProducts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products = productService.findHotProducts(pageable);
            return Result.success(products);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 个性化推荐：为当前登录用户推荐产品
     */
    @GetMapping("/recommend")
    public Result<List<Product>> getRecommendProducts(
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            if (userId == null) {
                return Result.error("请先登录");
            }
            List<Product> products = productService.recommendForUser(userId, limit);
            return Result.success(products);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                return jwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private String ensureAdmin(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return "请先登录";
        User user = userMapper.selectById(userId);
        if (user == null) return "请先登录";
        if (user.getUserType() == null || user.getUserType() != 1) return "无权限";
        return null;
    }

    /**
     * 添加产品（管理员）
     */
    @PostMapping("/add")
    public Result<Product> addProduct(@RequestBody Product product, HttpServletRequest request) {
        try {
            String authError = ensureAdmin(request);
            if (authError != null) return Result.error(authError);
            Product savedProduct = productService.saveProduct(product);
            return Result.success("添加成功", savedProduct);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新产品（管理员）
     */
    @PutMapping("/update")
    public Result<Product> updateProduct(@RequestBody Product product, HttpServletRequest request) {
        try {
            String authError = ensureAdmin(request);
            if (authError != null) return Result.error(authError);
            Product updatedProduct = productService.updateProduct(product);
            return Result.success("更新成功", updatedProduct);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除产品（管理员）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id, HttpServletRequest request) {
        try {
            String authError = ensureAdmin(request);
            if (authError != null) return Result.error(authError);
            productService.delete(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

