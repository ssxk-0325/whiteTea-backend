package com.fuding.service;

import com.fuding.entity.OrderReview;

import java.util.List;

public interface OrderReviewService {

    OrderReview createReview(Long userId, Long orderId, Integer rating, String content);

    OrderReview getByOrderId(Long orderId);

    /** 含用户昵称，用于订单详情展示 */
    OrderReview getByOrderIdEnriched(Long orderId);

    boolean existsByOrderId(Long orderId);

    List<OrderReview> listByProductId(Long productId, int limit);
}
