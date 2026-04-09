package com.fuding.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.common.Result;
import com.fuding.entity.User;
import com.fuding.service.CaptchaStoreService;
import com.fuding.service.UserService;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CaptchaStoreService captchaStoreService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            String phone = params.get("phone");

            User user = userService.register(username, password, phone);
            return Result.success("注册成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            String captchaId = params.get("captchaId");
            String captchaCode = params.get("captchaCode");

            validateCaptcha(captchaId, captchaCode);

            String token = userService.login(username, password);
            User user = userService.findByUsername(username);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);

            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private void validateCaptcha(String captchaId, String captchaCode) {
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            throw new RuntimeException("请输入验证码");
        }
        String expected = captchaStoreService.getAndRemove(captchaId);
        if (!StringUtils.hasText(expected)) {
            throw new RuntimeException("验证码已过期，请刷新后重试");
        }
        if (!expected.equalsIgnoreCase(captchaCode.trim())) {
            throw new RuntimeException("验证码错误");
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.findById(userId);
            // 不返回密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据用户ID获取用户信息（公开接口，用于查看其他用户信息）
     */
    @GetMapping("/info/{userId}")
    public Result<User> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            // 不返回密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<User> updateUser(@RequestBody User user, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            user.setId(userId);
            User updatedUser = userService.updateUser(user);
            updatedUser.setPassword(null);
            return Result.success("更新成功", updatedUser);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");

            userService.changePassword(userId, oldPassword, newPassword);
            return Result.success("密码修改成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员获取用户列表
     */
    @GetMapping("/admin/list")
    public Result<IPage<User>> getUserList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            // 验证token有效性
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token);

            // MyBatis-Plus 的 Page 从 1 开始，前端传的是从 0 开始，需要 +1
            Page<User> userPage = new Page<>(page + 1, size);
            IPage<User> result = userService.getUserList(userPage, keyword);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员更新用户信息
     */
    @PutMapping("/admin/update")
    public Result<User> adminUpdateUser(@RequestBody User user, HttpServletRequest request) {
        try {
            // 验证token有效性
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token);

            User updatedUser = userService.adminUpdateUser(user);
            return Result.success("更新成功", updatedUser);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

