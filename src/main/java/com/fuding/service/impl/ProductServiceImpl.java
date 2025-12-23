package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.Product;
import com.fuding.mapper.ProductMapper;
import com.fuding.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 产品服务实现类
 */
@Service
@Transactional
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public org.springframework.data.domain.Page<Product> findProducts(Pageable pageable, Long categoryId, String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        
        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Product::getName, keyword);
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
}
