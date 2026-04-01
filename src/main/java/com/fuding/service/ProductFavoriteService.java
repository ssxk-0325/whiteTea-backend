package com.fuding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.entity.Product;

/**
 * 商品收藏
 */
public interface ProductFavoriteService {

    void addFavorite(Long userId, Long productId);

    void removeFavorite(Long userId, Long productId);

    boolean isFavorite(Long userId, Long productId);

    /** page 为 1 起始页码 */
    Page<Product> pageFavoriteProducts(Long userId, int page, int size);
}
