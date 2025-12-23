package com.fuding.service;

import com.fuding.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    User register(String username, String password, String phone);

    /**
     * 用户登录
     */
    String login(String username, String password);

    /**
     * 根据ID查找用户
     */
    User findById(Long id);

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 更新用户信息
     */
    User updateUser(User user);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
}

