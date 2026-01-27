package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.Store;
import com.fuding.mapper.StoreMapper;
import com.fuding.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 门店服务实现类
 */
@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements StoreService {

    @Autowired
    private StoreMapper storeMapper;

    @Override
    public List<Store> getStoreList(String city) {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Store::getStatus, 1); // 只查询营业中的门店
        
        if (city != null && !city.trim().isEmpty()) {
            wrapper.eq(Store::getCity, city);
        }
        
        wrapper.orderByAsc(Store::getCreateTime);
        return storeMapper.selectList(wrapper);
    }

    @Override
    public Store getStoreById(Long id) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            throw new RuntimeException("门店不存在");
        }
        return store;
    }

    @Override
    public List<Store> getNearbyStores(Double longitude, Double latitude, Double radius) {
        if (longitude == null || latitude == null) {
            return getStoreList(null);
        }
        
        // 默认搜索半径5公里
        final double searchRadius = (radius == null || radius <= 0) ? 5.0 : radius;
        final double finalLongitude = longitude;
        final double finalLatitude = latitude;
        
        // 获取所有营业中的门店
        List<Store> allStores = getStoreList(null);
        
        // 计算距离并筛选
        return allStores.stream()
                .filter(store -> {
                    if (store.getLongitude() == null || store.getLatitude() == null) {
                        return false;
                    }
                    double distance = calculateDistance(
                            finalLatitude, finalLongitude,
                            store.getLatitude().doubleValue(), store.getLongitude().doubleValue()
                    );
                    return distance <= searchRadius;
                })
                .sorted((s1, s2) -> {
                    double d1 = calculateDistance(
                            finalLatitude, finalLongitude,
                            s1.getLatitude().doubleValue(), s1.getLongitude().doubleValue()
                    );
                    double d2 = calculateDistance(
                            finalLatitude, finalLongitude,
                            s2.getLatitude().doubleValue(), s2.getLongitude().doubleValue()
                    );
                    return Double.compare(d1, d2);
                })
                .collect(Collectors.toList());
    }

    /**
     * 计算两点之间的距离（公里）
     * 使用Haversine公式
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径（公里）
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}

