package com.fuding.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fuding.entity.CustomerServiceMessage;
import com.fuding.service.ChatService;
import com.fuding.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket消息处理器
 */
@Controller
public class CustomerServiceWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceWebSocketHandler.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    // @Autowired
    // private JwtUtil jwtUtil; // 预留，用于后续token验证

    /**
     * 处理用户发送的消息
     * 客户端发送到：/app/chat.sendMessage
     */
    @MessageMapping("/chat.sendMessage")
    public void handleMessage(@Payload String messageJson) {
        try {
            JSONObject messageObj = JSON.parseObject(messageJson);
            Long sessionId = messageObj.getLong("sessionId");
            String content = messageObj.getString("content");
            // String token = messageObj.getString("token");

            // 验证token（可选，如果前端已经验证过）
            // Long userId = jwtUtil.getUserIdFromToken(token);

            // 保存用户消息
            CustomerServiceMessage userMessage = chatService.sendMessage(sessionId, content, 0);

            // 发送用户消息给客户端
            Map<String, Object> userMsgData = new HashMap<>();
            userMsgData.put("type", "user_message");
            userMsgData.put("message", userMessage);
            messagingTemplate.convertAndSend("/topic/session/" + sessionId, userMsgData);

            // 处理消息并生成AI回复
            CustomerServiceMessage aiMessage = chatService.processUserMessage(sessionId, content);

            // 发送AI回复给客户端
            Map<String, Object> aiMsgData = new HashMap<>();
            aiMsgData.put("type", "ai_message");
            aiMsgData.put("message", aiMessage);
            messagingTemplate.convertAndSend("/topic/session/" + sessionId, aiMsgData);

        } catch (Exception e) {
            logger.error("处理WebSocket消息失败：", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("type", "error");
            errorData.put("message", "处理消息失败：" + e.getMessage());
            messagingTemplate.convertAndSend("/topic/error", errorData);
        }
    }
}

