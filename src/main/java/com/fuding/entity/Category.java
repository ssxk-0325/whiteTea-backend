package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品分类实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_category")
public class Category extends BaseEntity {

    /**
     * 分类名称
     */
    @TableField("name")
    private String name;

    /**
     * 分类描述
     */
    @TableField("description")
    private String description;

    /**
     * 分类图片
     */
    @TableField("image")
    private String image;

    /**
     * 父分类ID，0表示顶级分类
     */
    @TableField("parent_id")
    private Long parentId = 0L;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder = 0;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status = 1;
}

