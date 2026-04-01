package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品收藏
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_product_favorite")
public class ProductFavorite extends BaseEntity {

    @TableField("user_id")
    private Long userId;

    @TableField("product_id")
    private Long productId;

    @TableField(exist = false)
    private Product product;
}
