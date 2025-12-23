package com.fuding.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fuding.common.Result;
import com.fuding.entity.Category;
import com.fuding.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@RestController
@RequestMapping("/category")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 获取所有分类
     */
    @GetMapping("/list")
    public Result<List<Category>> getCategoryList() {
        try {
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Category::getStatus, 1);
            wrapper.orderByAsc(Category::getSortOrder);
            List<Category> categories = categoryMapper.selectList(wrapper);
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据父分类ID获取子分类
     */
    @GetMapping("/children/{parentId}")
    public Result<List<Category>> getChildrenCategories(@PathVariable Long parentId) {
        try {
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Category::getParentId, parentId);
            wrapper.eq(Category::getStatus, 1);
            wrapper.orderByAsc(Category::getSortOrder);
            List<Category> categories = categoryMapper.selectList(wrapper);
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加分类（管理员）
     */
    @PostMapping("/add")
    public Result<Category> addCategory(@RequestBody Category category) {
        try {
            categoryMapper.insert(category);
            return Result.success("添加成功", category);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新分类（管理员）
     */
    @PutMapping("/update")
    public Result<Category> updateCategory(@RequestBody Category category) {
        try {
            categoryMapper.updateById(category);
            return Result.success("更新成功", category);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
