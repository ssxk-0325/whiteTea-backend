package com.fuding.service;

import com.fuding.entity.CustomerServiceMessage;
import com.fuding.entity.CustomerServiceSession;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 创建或获取会话
     */
    CustomerServiceSession createOrGetSession(Long userId);

    /**
     * 发送消息
     */
    CustomerServiceMessage sendMessage(Long sessionId, String content, Integer senderType);

    /**
     * 获取会话消息列表
     */
    List<CustomerServiceMessage> getMessages(Long sessionId);

    /**
     * 处理用户消息并生成AI回复
     */
    CustomerServiceMessage processUserMessage(Long sessionId, String userMessage);

    /**
     * 结束会话
     */
    void endSession(Long sessionId);
}

