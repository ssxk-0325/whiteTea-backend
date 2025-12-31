package com.fuding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuding.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 购物车Mapper接口
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    /**
     * 恢复已删除的购物车记录（绕过逻辑删除）
     */
    @Update("UPDATE tb_cart SET deleted = 0 WHERE user_id = #{userId} AND product_id = #{productId} AND deleted = 1")
    int restoreDeletedCart(@Param("userId") Long userId, @Param("productId") Long productId);
}

