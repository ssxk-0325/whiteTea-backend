package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fuding.entity.CustomerServiceMessage;
import com.fuding.entity.CustomerServiceSession;
import com.fuding.entity.User;
import com.fuding.mapper.CustomerServiceMessageMapper;
import com.fuding.mapper.CustomerServiceSessionMapper;
import com.fuding.service.AIService;
import com.fuding.service.ChatService;
import com.fuding.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 聊天服务实现类
 */
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private CustomerServiceSessionMapper sessionMapper;

    @Autowired
    private CustomerServiceMessageMapper messageMapper;

    @Autowired
    private AIService aiService;

    @Autowired
    private com.fuding.service.TagService tagService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public CustomerServiceSession createOrGetSession(Long userId) {
        // 查找用户最近的进行中会话（AI 进行中 或 已转人工）
        LambdaQueryWrapper<CustomerServiceSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerServiceSession::getUserId, userId);
        wrapper.in(CustomerServiceSession::getStatus, 0, 2);
        wrapper.orderByDesc(CustomerServiceSession::getCreateTime);
        wrapper.last("LIMIT 1");
        
        CustomerServiceSession session = sessionMapper.selectOne(wrapper);
        
        if (session == null) {
            // 创建新会话
            session = new CustomerServiceSession();
            session.setSessionNo(generateSessionNo());
            session.setUserId(userId);
            session.setStatus(0);
            session.setLastMessageTime(LocalDateTime.now());
            sessionMapper.insert(session);
        }
        
        return session;
    }

    @Override
    public CustomerServiceSession getSessionById(Long sessionId) {
        return sessionMapper.selectById(sessionId);
    }

    @Override
    @Transactional
    public CustomerServiceMessage sendMessage(Long sessionId, String content, Integer senderType) {
        CustomerServiceMessage message = new CustomerServiceMessage();
        message.setSessionId(sessionId);
        message.setContent(content);
        message.setSenderType(senderType);
        message.setMessageType(0); // 文本消息
        message.setIsRead(0);
        messageMapper.insert(message);

        // 更新会话最后消息时间
        CustomerServiceSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setLastMessageTime(LocalDateTime.now());
            sessionMapper.updateById(session);
        }

        return message;
    }

    @Override
    public List<CustomerServiceMessage> getMessages(Long sessionId) {
        LambdaQueryWrapper<CustomerServiceMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerServiceMessage::getSessionId, sessionId);
        wrapper.orderByAsc(CustomerServiceMessage::getCreateTime);
        return messageMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public CustomerServiceMessage processUserMessage(Long sessionId, String userMessage) {
        CustomerServiceSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }

        // 1. 保存用户消息
        sendMessage(sessionId, userMessage, 0); // 0-用户

        // 1.5. 记录问题（用于Tag统计）
        tagService.recordQuestion(userMessage, session.getUserId());

        // 已转人工：不再调用大模型，由管理员在后台回复
        if (Integer.valueOf(2).equals(session.getStatus())) {
            return null;
        }

        // 2. 获取对话历史（最近10条）
        // 注意：getMessages 已包含刚写入的当前用户消息，若再与 userMessage 拼接会重复一条 user，部分大模型会返回异常或空正文
        List<CustomerServiceMessage> recentMessages = getMessages(sessionId);
        List<CustomerServiceMessage> historyMessages = new ArrayList<>(recentMessages);
        if (!historyMessages.isEmpty()) {
            CustomerServiceMessage last = historyMessages.get(historyMessages.size() - 1);
            if (Integer.valueOf(0).equals(last.getSenderType())
                    && userMessage != null && userMessage.equals(last.getContent())) {
                historyMessages.remove(historyMessages.size() - 1);
            }
        }
        List<String> conversationHistory = historyMessages.stream()
                .limit(10)
                .map(msg -> {
                    String role;
                    if (Integer.valueOf(0).equals(msg.getSenderType())) {
                        role = "用户";
                    } else if (Integer.valueOf(2).equals(msg.getSenderType())) {
                        role = "人工";
                    } else {
                        role = "AI";
                    }
                    return role + ":" + msg.getContent();
                })
                .collect(Collectors.toList());

        // 3. 调用AI服务生成回复
        String aiReply = aiService.generateReply(userMessage, conversationHistory);

        // 4. 保存AI回复
        CustomerServiceMessage aiMessage = sendMessage(sessionId, aiReply, 1); // 1-AI

        return aiMessage;
    }

    @Override
    @Transactional
    public CustomerServiceSession transferToHuman(Long sessionId) {
        CustomerServiceSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        if (Integer.valueOf(1).equals(session.getStatus())) {
            throw new RuntimeException("会话已结束，无法转人工");
        }
        if (Integer.valueOf(2).equals(session.getStatus())) {
            return session;
        }
        session.setStatus(2);
        session.setLastMessageTime(LocalDateTime.now());
        sessionMapper.updateById(session);
        sendMessage(sessionId, "已为您转接人工客服，请稍候，工作人员将尽快回复您。", 2);
        return session;
    }

    @Override
    public List<Map<String, Object>> getAdminSessions(Integer status, String keyword) {
        LambdaQueryWrapper<CustomerServiceSession> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(CustomerServiceSession::getStatus, status);
        } else {
            wrapper.in(CustomerServiceSession::getStatus, 0, 2);
        }
        wrapper.orderByDesc(CustomerServiceSession::getLastMessageTime);
        List<CustomerServiceSession> sessions = sessionMapper.selectList(wrapper);
        List<Map<String, Object>> list = sessions.stream().map(s -> {
            Map<String, Object> item = new HashMap<>();
            item.put("session", s);
            try {
                User u = userService.findById(s.getUserId());
                if (u != null) {
                    u.setPassword(null);
                }
                item.put("user", u);
            } catch (Exception e) {
                item.put("user", null);
            }
            return item;
        }).collect(Collectors.toList());
        if (keyword == null || keyword.trim().isEmpty()) {
            return list;
        }
        String k = keyword.trim().toLowerCase();
        return list.stream().filter(item -> {
            CustomerServiceSession s = (CustomerServiceSession) item.get("session");
            User u = (User) item.get("user");
            String no = s != null && s.getSessionNo() != null ? s.getSessionNo().toLowerCase() : "";
            String un = u != null && u.getUsername() != null ? u.getUsername().toLowerCase() : "";
            String nn = u != null && u.getNickname() != null ? u.getNickname().toLowerCase() : "";
            String uid = s != null && s.getUserId() != null ? String.valueOf(s.getUserId()) : "";
            return no.contains(k) || un.contains(k) || nn.contains(k) || uid.contains(k);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerServiceMessage adminReply(Long sessionId, String content) {
        CustomerServiceSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        if (Integer.valueOf(1).equals(session.getStatus())) {
            throw new RuntimeException("会话已结束，无法回复");
        }
        if (!Integer.valueOf(2).equals(session.getStatus())) {
            transferToHuman(sessionId);
        }
        return sendMessage(sessionId, content.trim(), 2);
    }

    @Override
    @Transactional
    public void endSession(Long sessionId) {
        CustomerServiceSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setStatus(1); // 已结束
            sessionMapper.updateById(session);
        }
    }

    /**
     * 生成会话编号
     */
    private String generateSessionNo() {
        return "CS" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

