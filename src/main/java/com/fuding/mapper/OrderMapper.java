package com.fuding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuding.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 查询用户最近的订单ID列表
     */
    @Select("SELECT id FROM tb_order " +
            "WHERE user_id = #{userId} AND deleted = 0 " +
            "AND status IN (1,2,3) " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<Long> findRecentOrderIdsByUser(@Param("userId") Long userId,
                                        @Param("limit") Integer limit);
}

