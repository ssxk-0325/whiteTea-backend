package com.fuding.service;

import com.fuding.entity.Address;
import java.util.List;

/**
 * 收货地址服务接口
 */
public interface AddressService {

    /**
     * 添加地址
     */
    Address addAddress(Long userId, Address address);

    /**
     * 获取用户地址列表
     */
    List<Address> getUserAddresses(Long userId);

    /**
     * 根据ID获取地址
     */
    Address getAddressById(Long id);

    /**
     * 更新地址
     */
    Address updateAddress(Address address);

    /**
     * 删除地址
     */
    void deleteAddress(Long id);

    /**
     * 设置默认地址
     */
    void setDefaultAddress(Long userId, Long addressId);
}

