package com.fuding.controller;

import com.fuding.common.Result;
import com.fuding.entity.CustomerServiceMessage;
import com.fuding.entity.CustomerServiceSession;
import com.fuding.entity.User;
import com.fuding.service.ChatService;
import com.fuding.service.UserService;
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

    @Autowired
    private UserService userService;

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
            data.put("status", session.getStatus());

            return Result.success("会话创建成功", data);
        } catch (Exception e) {
            return Result.error("创建会话失败：" + e.getMessage());
        }
    }

    /**
     * 获取会话详情（需本人会话）
     */
    @GetMapping("/session/{sessionId}")
    public Result<CustomerServiceSession> getSession(@PathVariable Long sessionId, HttpServletRequest request) {
        try {
            Long userId = parseUserId(request);
            CustomerServiceSession session = chatService.getSessionById(sessionId);
            if (session == null) {
                return Result.error("会话不存在");
            }
            if (!userId.equals(session.getUserId())) {
                return Result.error("无权限访问该会话");
            }
            return Result.success(session);
        } catch (Exception e) {
            return Result.error("获取会话失败：" + e.getMessage());
        }
    }

    /**
     * 获取会话消息列表（需本人会话）
     */
    @GetMapping("/session/{sessionId}/messages")
    public Result<List<CustomerServiceMessage>> getMessages(@PathVariable Long sessionId, HttpServletRequest request) {
        try {
            Long userId = parseUserId(request);
            assertSessionOwner(sessionId, userId);
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
            Long userId = parseUserId(request);

            Long sessionId = Long.valueOf(params.get("sessionId").toString());
            String content = params.get("content").toString();

            assertSessionOwner(sessionId, userId);

            CustomerServiceMessage aiMessage = chatService.processUserMessage(sessionId, content);

            Map<String, Object> data = new HashMap<>();
            data.put("message", aiMessage);
            CustomerServiceSession session = chatService.getSessionById(sessionId);
            if (session != null) {
                data.put("status", session.getStatus());
            }

            return Result.success("消息发送成功", data);
        } catch (Exception e) {
            return Result.error("发送消息失败：" + e.getMessage());
        }
    }

    /**
     * 用户申请转人工
     */
    @PostMapping("/session/{sessionId}/transfer-human")
    public Result<CustomerServiceSession> transferHuman(@PathVariable Long sessionId, HttpServletRequest request) {
        try {
            Long userId = parseUserId(request);
            assertSessionOwner(sessionId, userId);
            CustomerServiceSession updated = chatService.transferToHuman(sessionId);
            return Result.success("已提交转人工", updated);
        } catch (Exception e) {
            return Result.error("转人工失败：" + e.getMessage());
        }
    }

    /**
     * 结束会话（需本人会话）
     */
    @PostMapping("/session/{sessionId}/end")
    public Result<Void> endSession(@PathVariable Long sessionId, HttpServletRequest request) {
        try {
            Long userId = parseUserId(request);
            assertSessionOwner(sessionId, userId);
            chatService.endSession(sessionId);
            return Result.success("会话已结束", null);
        } catch (Exception e) {
            return Result.error("结束会话失败：" + e.getMessage());
        }
    }

    // ---------- 管理员 ----------

    @GetMapping("/admin/sessions")
    public Result<List<Map<String, Object>>> adminSessions(@RequestParam(required = false) Integer status,
                                                            @RequestParam(required = false) String keyword,
                                                            HttpServletRequest request) {
        try {
            requireAdmin(request);
            return Result.success(chatService.getAdminSessions(status, keyword));
        } catch (Exception e) {
            return Result.error("获取会话列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/admin/session/{sessionId}/messages")
    public Result<List<CustomerServiceMessage>> adminMessages(@PathVariable Long sessionId,
                                                                HttpServletRequest request) {
        try {
            requireAdmin(request);
            return Result.success(chatService.getMessages(sessionId));
        } catch (Exception e) {
            return Result.error("获取会话消息失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/session/{sessionId}/reply")
    public Result<CustomerServiceMessage> adminReply(@PathVariable Long sessionId,
                                                     @RequestBody Map<String, Object> params,
                                                     HttpServletRequest request) {
        try {
            requireAdmin(request);
            String content = params.get("content") == null ? "" : params.get("content").toString();
            if (content.trim().isEmpty()) {
                return Result.error("回复内容不能为空");
            }
            CustomerServiceMessage message = chatService.adminReply(sessionId, content);
            return Result.success("发送成功", message);
        } catch (Exception e) {
            return Result.error("发送失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/session/{sessionId}/close")
    public Result<Void> adminClose(@PathVariable Long sessionId, HttpServletRequest request) {
        try {
            requireAdmin(request);
            chatService.endSession(sessionId);
            return Result.success("会话已结束", null);
        } catch (Exception e) {
            return Result.error("结束会话失败：" + e.getMessage());
        }
    }

    private Long parseUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    private void assertSessionOwner(Long sessionId, Long userId) {
        CustomerServiceSession session = chatService.getSessionById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        if (!userId.equals(session.getUserId())) {
            throw new RuntimeException("无权限操作该会话");
        }
    }

    private void requireAdmin(HttpServletRequest request) {
        Long userId = parseUserId(request);
        User user = userService.findById(userId);
        if (user == null || user.getUserType() == null || user.getUserType() != 1) {
            throw new RuntimeException("无管理员权限");
        }
    }
}
