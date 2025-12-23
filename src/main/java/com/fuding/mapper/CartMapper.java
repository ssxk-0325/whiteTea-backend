package com.fuding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuding.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车Mapper接口
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}

