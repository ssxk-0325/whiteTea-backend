package com.fuding.service;

import com.fuding.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 产品服务接口
 */
public interface ProductService {

    /**
     * 分页查询产品
     */
    Page<Product> findProducts(Pageable pageable, Long categoryId, String keyword, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * 管理后台：分页查询产品（可含下架，按条件筛选）
     */
    Page<Product> findProductsForAdmin(Pageable pageable, Long categoryId, String keyword, Integer status,
                                       BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * 根据ID查找产品
     */
    Product findById(Long id);

    /**
     * 保存产品
     */
    Product saveProduct(Product product);

    /**
     * 更新产品
     */
    Product updateProduct(Product product);

    /**
     * 删除产品
     */
    void delete(Long id);

    /**
     * 查找热门产品
     */
    Page<Product> findHotProducts(Pageable pageable);

    /**
     * 为指定用户推荐产品（个性化推荐）
     */
    List<Product> recommendForUser(Long userId, Integer limit);
}

