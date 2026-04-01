package com.fuding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuding.entity.OrderReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderReviewMapper extends BaseMapper<OrderReview> {

    @Select("SELECT r.* FROM tb_order_review r WHERE r.deleted = 0 AND EXISTS (" +
            "SELECT 1 FROM tb_order_item oi WHERE oi.order_id = r.order_id AND oi.product_id = #{productId} AND oi.deleted = 0) " +
            "ORDER BY r.create_time DESC LIMIT #{limit}")
    List<OrderReview> listByProductId(@Param("productId") Long productId, @Param("limit") int limit);
}
