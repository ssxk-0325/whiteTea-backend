package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fuding.entity.CustomerServiceMessage;
import com.fuding.entity.CustomerServiceSession;
import com.fuding.mapper.CustomerServiceMessageMapper;
import com.fuding.mapper.CustomerServiceSessionMapper;
import com.fuding.service.AIService;
import com.fuding.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    @Override
    @Transactional
    public CustomerServiceSession createOrGetSession(Long userId) {
        // 查找用户最近的进行中会话
        LambdaQueryWrapper<CustomerServiceSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerServiceSession::getUserId, userId);
        wrapper.eq(CustomerServiceSession::getStatus, 0); // 进行中
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
        // 1. 保存用户消息
        sendMessage(sessionId, userMessage, 0); // 0-用户

        // 2. 获取对话历史（最近10条）
        List<CustomerServiceMessage> recentMessages = getMessages(sessionId);
        List<String> conversationHistory = recentMessages.stream()
                .limit(10)
                .map(msg -> {
                    String role = msg.getSenderType() == 0 ? "用户" : "AI";
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

