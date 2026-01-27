package com.fuding.service;

import com.fuding.entity.Store;
import java.util.List;

/**
 * 门店服务接口
 */
public interface StoreService {

    /**
     * 获取门店列表
     * @param city 城市（可选，用于筛选）
     * @return 门店列表
     */
    List<Store> getStoreList(String city);

    /**
     * 根据ID获取门店详情
     * @param id 门店ID
     * @return 门店信息
     */
    Store getStoreById(Long id);

    /**
     * 获取附近门店（根据经纬度范围）
     * @param longitude 经度
     * @param latitude 纬度
     * @param radius 半径（公里，默认5公里）
     * @return 附近门店列表
     */
    List<Store> getNearbyStores(Double longitude, Double latitude, Double radius);
}

