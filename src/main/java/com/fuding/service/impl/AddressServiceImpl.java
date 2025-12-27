package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.Address;
import com.fuding.mapper.AddressMapper;
import com.fuding.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收货地址服务实现类
 */
@Service
@Transactional
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public Address addAddress(Long userId, Address address) {
        address.setUserId(userId);
        
        // 如果设置为默认地址，取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            LambdaUpdateWrapper<Address> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Address::getUserId, userId)
                        .set(Address::getIsDefault, 0);
            addressMapper.update(null, updateWrapper);
        }
        
        addressMapper.insert(address);
        return address;
    }

    @Override
    public List<Address> getUserAddresses(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.orderByDesc(Address::getIsDefault);
        wrapper.orderByDesc(Address::getCreateTime);
        return addressMapper.selectList(wrapper);
    }

    @Override
    public Address getAddressById(Long id) {
        Address address = addressMapper.selectById(id);
        if (address == null) {
            throw new RuntimeException("地址不存在");
        }
        return address;
    }

    @Override
    public Address updateAddress(Address address) {
        Address existingAddress = getAddressById(address.getId());
        
        existingAddress.setReceiverName(address.getReceiverName());
        existingAddress.setReceiverPhone(address.getReceiverPhone());
        existingAddress.setProvince(address.getProvince());
        existingAddress.setCity(address.getCity());
        existingAddress.setDistrict(address.getDistrict());
        existingAddress.setDetail(address.getDetail());
        
        // 如果设置为默认地址，取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            LambdaUpdateWrapper<Address> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Address::getUserId, existingAddress.getUserId())
                        .ne(Address::getId, address.getId())
                        .set(Address::getIsDefault, 0);
            addressMapper.update(null, updateWrapper);
            existingAddress.setIsDefault(1);
        } else {
            existingAddress.setIsDefault(address.getIsDefault());
        }
        
        addressMapper.updateById(existingAddress);
        return existingAddress;
    }

    @Override
    public void deleteAddress(Long id) {
        addressMapper.deleteById(id);
    }

    @Override
    public void setDefaultAddress(Long userId, Long addressId) {
        // 取消所有默认地址
        LambdaUpdateWrapper<Address> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Address::getUserId, userId)
                    .set(Address::getIsDefault, 0);
        addressMapper.update(null, updateWrapper);
        
        // 设置新的默认地址
        Address address = getAddressById(addressId);
        if (!address.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该地址");
        }
        address.setIsDefault(1);
        addressMapper.updateById(address);
    }
}

