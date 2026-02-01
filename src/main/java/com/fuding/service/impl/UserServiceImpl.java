package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.User;
import com.fuding.mapper.UserMapper;
import com.fuding.service.UserService;
import com.fuding.util.JwtUtil;
import com.fuding.util.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现类
 */
@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public User register(String username, String password, String phone) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查手机号是否已存在
        if (phone != null && !phone.isEmpty()) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, phone);
            if (userMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("手机号已被注册");
            }
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(Md5Util.encrypt(password));
        user.setPhone(phone);
        user.setNickname(username);
        user.setUserType(0); // 普通用户
        user.setStatus(1); // 启用

        userMapper.insert(user);
        return user;
    }

    @Override
    public String login(String username, String password) {
        // 查找用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码
        if (!user.getPassword().equals(Md5Util.encrypt(password))) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 生成Token
        return jwtUtil.generateToken(user.getId(), user.getUsername());
    }

    @Override
    public User findById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    @Override
    public User updateUser(User user) {
        User existingUser = findById(user.getId());
        
        if (user.getNickname() != null) {
            existingUser.setNickname(user.getNickname());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }
        if (user.getGender() != null) {
            existingUser.setGender(user.getGender());
        }
        if (user.getBirthday() != null) {
            existingUser.setBirthday(user.getBirthday());
        }

        userMapper.updateById(existingUser);
        return existingUser;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = findById(userId);
        
        // 验证旧密码
        if (!user.getPassword().equals(Md5Util.encrypt(oldPassword))) {
            throw new RuntimeException("原密码错误");
        }

        // 更新密码
        user.setPassword(Md5Util.encrypt(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public IPage<User> getUserList(Page<User> page, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 如果有关键词，搜索用户名、昵称、手机号或邮箱
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(User::getUsername, keyword)
                .or()
                .like(User::getNickname, keyword)
                .or()
                .like(User::getPhone, keyword)
                .or()
                .like(User::getEmail, keyword)
            );
        }
        
        // 按创建时间倒序排列
        wrapper.orderByDesc(User::getCreateTime);
        
        // 不返回密码字段
        IPage<User> result = userMapper.selectPage(page, wrapper);
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return result;
    }
}
