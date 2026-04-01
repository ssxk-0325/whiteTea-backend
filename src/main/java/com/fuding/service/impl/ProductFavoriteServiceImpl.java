package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.Product;
import com.fuding.entity.ProductFavorite;
import com.fuding.mapper.ProductFavoriteMapper;
import com.fuding.mapper.ProductMapper;
import com.fuding.service.ProductFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductFavoriteServiceImpl extends ServiceImpl<ProductFavoriteMapper, ProductFavorite> implements ProductFavoriteService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        LambdaQueryWrapper<ProductFavorite> w = new LambdaQueryWrapper<>();
        w.eq(ProductFavorite::getUserId, userId).eq(ProductFavorite::getProductId, productId);
        if (this.count(w) > 0) {
            return;
        }
        ProductFavorite f = new ProductFavorite();
        f.setUserId(userId);
        f.setProductId(productId);
        this.save(f);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long userId, Long productId) {
        LambdaQueryWrapper<ProductFavorite> w = new LambdaQueryWrapper<>();
        w.eq(ProductFavorite::getUserId, userId).eq(ProductFavorite::getProductId, productId);
        this.remove(w);
    }

    @Override
    public boolean isFavorite(Long userId, Long productId) {
        LambdaQueryWrapper<ProductFavorite> w = new LambdaQueryWrapper<>();
        w.eq(ProductFavorite::getUserId, userId).eq(ProductFavorite::getProductId, productId);
        return this.count(w) > 0;
    }

    @Override
    public Page<Product> pageFavoriteProducts(Long userId, int page, int size) {
        LambdaQueryWrapper<ProductFavorite> w = new LambdaQueryWrapper<>();
        w.eq(ProductFavorite::getUserId, userId).orderByDesc(ProductFavorite::getCreateTime);
        Page<ProductFavorite> fp = new Page<>(page, size);
        Page<ProductFavorite> favPage = this.page(fp, w);
        List<Long> productIds = favPage.getRecords().stream().map(ProductFavorite::getProductId).collect(Collectors.toList());
        Page<Product> result = new Page<>(favPage.getCurrent(), favPage.getSize(), favPage.getTotal());
        if (productIds.isEmpty()) {
            return result;
        }
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>().in(Product::getId, productIds));
        Map<Long, Product> map = products.stream().collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        List<Product> ordered = productIds.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
        result.setRecords(ordered);
        return result;
    }
}
