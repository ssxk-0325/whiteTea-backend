package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 购物车实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_cart")
public class Cart extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 产品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 数量
     */
    @TableField("quantity")
    private Integer quantity = 1;

    /**
     * 是否选中：0-未选中，1-选中
     */
    @TableField("selected")
    private Integer selected = 1;
}
