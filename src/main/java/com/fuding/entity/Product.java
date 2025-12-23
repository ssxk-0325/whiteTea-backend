package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 产品实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_product")
public class Product extends BaseEntity {

    /**
     * 产品名称
     */
    @TableField("name")
    private String name;

    /**
     * 产品描述
     */
    @TableField("description")
    private String description;

    /**
     * 产品图片（主图）
     */
    @TableField("image")
    private String image;

    /**
     * 产品图片列表（JSON格式）
     */
    @TableField("images")
    private String images;

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 原价
     */
    @TableField("original_price")
    private BigDecimal originalPrice;

    /**
     * 库存
     */
    @TableField("stock")
    private Integer stock = 0;

    /**
     * 销量
     */
    @TableField("sales")
    private Integer sales = 0;

    /**
     * 单位（如：克、斤、盒）
     */
    @TableField("unit")
    private String unit;

    /**
     * 规格
     */
    @TableField("specification")
    private String specification;

    /**
     * 产地
     */
    @TableField("origin")
    private String origin;

    /**
     * 年份
     */
    @TableField("year")
    private String year;

    /**
     * 状态：0-下架，1-上架
     */
    @TableField("status")
    private Integer status = 1;
}

