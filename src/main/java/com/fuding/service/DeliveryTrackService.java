package com.fuding.service;

import com.fuding.entity.Order;

import java.util.Map;

/**
 * 同城配送模拟轨迹（演示）：发货后按时间推进节点，展示与收货坐标直线距离。
 */
public interface DeliveryTrackService {

    /**
     * @return 无线上配送或未发货时返回 null；自提订单返回 null
     */
    Map<String, Object> buildDeliveryTrack(Order order);
}
