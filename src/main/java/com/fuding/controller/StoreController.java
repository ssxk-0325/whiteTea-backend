package com.fuding.controller;

import com.fuding.common.Result;
import com.fuding.entity.Store;
import com.fuding.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门店控制器
 */
@RestController
@RequestMapping("/store")
@CrossOrigin
public class StoreController {

    @Autowired
    private StoreService storeService;

    /**
     * 获取门店列表
     * @param city 城市（可选参数）
     */
    @GetMapping("/list")
    public Result<List<Store>> getStoreList(@RequestParam(required = false) String city) {
        try {
            List<Store> stores = storeService.getStoreList(city);
            return Result.success(stores);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取门店详情
     */
    @GetMapping("/{id}")
    public Result<Store> getStoreById(@PathVariable Long id) {
        try {
            Store store = storeService.getStoreById(id);
            return Result.success(store);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取附近门店
     * @param longitude 经度
     * @param latitude 纬度
     * @param radius 搜索半径（公里，默认5公里）
     */
    @GetMapping("/nearby")
    public Result<List<Store>> getNearbyStores(
            @RequestParam Double longitude,
            @RequestParam Double latitude,
            @RequestParam(required = false, defaultValue = "5.0") Double radius) {
        try {
            List<Store> stores = storeService.getNearbyStores(longitude, latitude, radius);
            return Result.success(stores);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

