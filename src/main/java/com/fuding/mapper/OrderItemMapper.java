package com.fuding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuding.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单项Mapper接口
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 基于订单ID列表统计关联产品（排除当前用户已购买的产品）
     */
    @Select({
            "<script>",
            "SELECT oi2.product_id ",
            "FROM tb_order_item oi1 ",
            "JOIN tb_order_item oi2 ON oi1.order_id = oi2.order_id ",
            "WHERE oi1.order_id IN ",
            "<foreach collection='orderIds' item='oid' open='(' separator=',' close=')'>",
            "  #{oid}",
            "</foreach>",
            "AND oi1.product_id != oi2.product_id ",
            "GROUP BY oi2.product_id ",
            "ORDER BY COUNT(*) DESC ",
            "LIMIT #{limit}",
            "</script>"
    })
    List<Long> findCoOccurProductsByOrders(@Param("userId") Long userId,
                                           @Param("orderIds") List<Long> orderIds,
                                           @Param("limit") Integer limit);
}

