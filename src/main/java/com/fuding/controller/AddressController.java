package com.fuding.controller;

import com.fuding.common.Result;
import com.fuding.entity.Address;
import com.fuding.service.AddressService;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 收货地址控制器
 */
@RestController
@RequestMapping("/address")
@CrossOrigin
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 添加地址
     */
    @PostMapping("/add")
    public Result<Address> addAddress(@RequestBody Address address, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            Address savedAddress = addressService.addAddress(userId, address);
            return Result.success("地址添加成功", savedAddress);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户地址列表
     */
    @GetMapping("/list")
    public Result<List<Address>> getAddressList(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            List<Address> addresses = addressService.getUserAddresses(userId);
            return Result.success(addresses);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新地址
     */
    @PutMapping("/update")
    public Result<Address> updateAddress(@RequestBody Address address, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            Address existingAddress = addressService.getAddressById(address.getId());
            if (!existingAddress.getUserId().equals(userId)) {
                return Result.error("无权操作该地址");
            }
            
            Address updatedAddress = addressService.updateAddress(address);
            return Result.success("地址更新成功", updatedAddress);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            Address address = addressService.getAddressById(id);
            if (!address.getUserId().equals(userId)) {
                return Result.error("无权操作该地址");
            }
            
            addressService.deleteAddress(id);
            return Result.success("地址删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 设置默认地址
     */
    @PostMapping("/{id}/set-default")
    public Result<Void> setDefaultAddress(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            addressService.setDefaultAddress(userId, id);
            return Result.success("设置成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

