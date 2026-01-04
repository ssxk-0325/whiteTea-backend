package com.fuding.controller;

import com.fuding.common.Result;
import com.fuding.entity.CustomerServiceMessage;
import com.fuding.entity.CustomerServiceSession;
import com.fuding.service.ChatService;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客服控制器
 */
@RestController
@RequestMapping("/customer-service")
@CrossOrigin
public class CustomerServiceController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 创建或获取会话
     */
    @PostMapping("/session/create")
    public Result<Map<String, Object>> createSession(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            CustomerServiceSession session = chatService.createOrGetSession(userId);

            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", session.getId());
            data.put("sessionNo", session.getSessionNo());

            return Result.success("会话创建成功", data);
        } catch (Exception e) {
            return Result.error("创建会话失败：" + e.getMessage());
        }
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/session/{sessionId}/messages")
    public Result<List<CustomerServiceMessage>> getMessages(@PathVariable Long sessionId) {
        try {
            List<CustomerServiceMessage> messages = chatService.getMessages(sessionId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("获取消息失败：" + e.getMessage());
        }
    }

    /**
     * 发送消息（HTTP方式，用于测试）
     */
    @PostMapping("/message/send")
    public Result<Map<String, Object>> sendMessage(
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token); // 验证token

            Long sessionId = Long.valueOf(params.get("sessionId").toString());
            String content = params.get("content").toString();

            // 处理用户消息并生成AI回复
            CustomerServiceMessage aiMessage = chatService.processUserMessage(sessionId, content);

            Map<String, Object> data = new HashMap<>();
            data.put("message", aiMessage);

            return Result.success("消息发送成功", data);
        } catch (Exception e) {
            return Result.error("发送消息失败：" + e.getMessage());
        }
    }

    /**
     * 结束会话
     */
    @PostMapping("/session/{sessionId}/end")
    public Result<Void> endSession(@PathVariable Long sessionId) {
        try {
            chatService.endSession(sessionId);
            return Result.success("会话已结束", null);
        } catch (Exception e) {
            return Result.error("结束会话失败：" + e.getMessage());
        }
    }
}

