package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.Product;
import com.fuding.mapper.OrderItemMapper;
import com.fuding.mapper.OrderMapper;
import com.fuding.mapper.ProductMapper;
import com.fuding.mapper.BrowseHistoryMapper;
import com.fuding.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 产品服务实现类
 */
@Service
@Transactional
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private BrowseHistoryMapper browseHistoryMapper;

    @Override
    public org.springframework.data.domain.Page<Product> findProducts(Pageable pageable, Long categoryId, String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        
        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Product::getName, keyword);
        }

        BigDecimal lo = minPrice;
        BigDecimal hi = maxPrice;
        if (lo != null && hi != null && lo.compareTo(hi) > 0) {
            BigDecimal t = lo;
            lo = hi;
            hi = t;
        }
        if (lo != null) {
            wrapper.ge(Product::getPrice, lo);
        }
        if (hi != null) {
            wrapper.le(Product::getPrice, hi);
        }
        
        wrapper.orderByDesc(Product::getCreateTime);
        
        Page<Product> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<Product> result = productMapper.selectPage(page, wrapper);
        
        // 转换为Spring Data Page
        return new org.springframework.data.domain.PageImpl<>(
            result.getRecords(),
            pageable,
            result.getTotal()
        );
    }

    @Override
    public Product findById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("产品不存在");
        }
        return product;
    }

    @Override
    public Product saveProduct(Product product) {
        productMapper.insert(product);
        return product;
    }

    @Override
    public Product updateProduct(Product product) {
        Product existingProduct = findById(product.getId());
        
        if (product.getName() != null) {
            existingProduct.setName(product.getName());
        }
        if (product.getDescription() != null) {
            existingProduct.setDescription(product.getDescription());
        }
        if (product.getPrice() != null) {
            existingProduct.setPrice(product.getPrice());
        }
        if (product.getStock() != null) {
            existingProduct.setStock(product.getStock());
        }
        if (product.getStatus() != null) {
            existingProduct.setStatus(product.getStatus());
        }

        productMapper.updateById(existingProduct);
        return existingProduct;
    }

    @Override
    public void delete(Long id) {
        productMapper.deleteById(id);
    }

    @Override
    public org.springframework.data.domain.Page<Product> findHotProducts(Pageable pageable) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        wrapper.orderByDesc(Product::getSales);
        
        Page<Product> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<Product> result = productMapper.selectPage(page, wrapper);

        return new org.springframework.data.domain.PageImpl<>(
                result.getRecords(),
                pageable,
                result.getTotal()
        );
    }

    @Override
    public List<Product> recommendForUser(Long userId, Integer limit) {
        if (userId == null) {
            return new ArrayList<>();
        }
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 1. 优先使用“已支付/已完成订单”做协同过滤推荐
        List<Long> orderIds = orderMapper.findRecentOrderIdsByUser(userId, 50);
        List<Long> candidateProductIds = new ArrayList<>();

        if (orderIds != null && !orderIds.isEmpty()) {
            candidateProductIds = orderItemMapper.findCoOccurProductsByOrders(userId, orderIds, limit);
        }

        // 2. 如果订单数据太少或没有结果，用浏览历史兜底（仅统计产品 target_type = 2）
        if (candidateProductIds == null || candidateProductIds.isEmpty()) {
            LambdaQueryWrapper<com.fuding.entity.BrowseHistory> historyWrapper =
                    new LambdaQueryWrapper<com.fuding.entity.BrowseHistory>()
                            .eq(com.fuding.entity.BrowseHistory::getUserId, userId)
                            .eq(com.fuding.entity.BrowseHistory::getTargetType, 2)
                            .orderByDesc(com.fuding.entity.BrowseHistory::getCreateTime)
                            .last("LIMIT " + limit);

            List<com.fuding.entity.BrowseHistory> histories = browseHistoryMapper.selectList(historyWrapper);
            if (histories != null && !histories.isEmpty()) {
                candidateProductIds = histories.stream()
                        .map(com.fuding.entity.BrowseHistory::getTargetId)
                        .distinct()
                        .limit(limit)
                        .collect(Collectors.toList());
            }
        }

        // 3. 如果仍然没有候选，就退化为热门产品推荐
        if (candidateProductIds == null || candidateProductIds.isEmpty()) {
            return getHotProducts(limit);
        }

        // 4. 查询产品详情，只返回上架商品，并保持推荐顺序
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Product::getId, candidateProductIds);
        wrapper.eq(Product::getStatus, 1);

        List<Product> products = productMapper.selectList(wrapper);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Product> sorted = new ArrayList<>();
        for (Long pid : candidateProductIds) {
            Product p = productMap.get(pid);
            if (p != null) {
                sorted.add(p);
            }
        }
        return sorted;
    }

    private List<Product> getHotProducts(Integer limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        wrapper.orderByDesc(Product::getSales);

        Page<Product> page = new Page<>(1, limit);
        IPage<Product> result = productMapper.selectPage(page, wrapper);
        return result.getRecords();
    }
}
