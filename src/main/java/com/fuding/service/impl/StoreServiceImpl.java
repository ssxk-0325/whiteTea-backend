package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.Store;
import com.fuding.mapper.StoreMapper;
import com.fuding.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

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
        
        wrapper.orderByAsc(Store::getId);
        wrapper.last("LIMIT 1");
        return storeMapper.selectList(wrapper);
    }

    @Override
    public Store getDefaultStore() {
        List<Store> list = getStoreList(null);
        return list.isEmpty() ? null : list.get(0);
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
        Store def = getDefaultStore();
        if (def == null) {
            return Collections.emptyList();
        }
        // 单门店模式：接口兼容附近检索，实际仅返回唯一门店
        return Collections.singletonList(def);
    }
}

