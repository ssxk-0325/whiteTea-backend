package com.fuding.service;

import com.fuding.entity.CustomerServiceMessage;
import com.fuding.entity.CustomerServiceSession;

import java.util.List;
import java.util.Map;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 创建或获取会话
     */
    CustomerServiceSession createOrGetSession(Long userId);

    /**
     * 根据ID获取会话
     */
    CustomerServiceSession getSessionById(Long sessionId);

    /**
     * 发送消息
     */
    CustomerServiceMessage sendMessage(Long sessionId, String content, Integer senderType);

    /**
     * 获取会话消息列表
     */
    List<CustomerServiceMessage> getMessages(Long sessionId);

    /**
     * 处理用户消息并生成AI回复（已转人工时只保存用户消息，不调用大模型，返回 null）
     */
    CustomerServiceMessage processUserMessage(Long sessionId, String userMessage);

    /**
     * 用户申请转人工
     */
    CustomerServiceSession transferToHuman(Long sessionId);

    /**
     * 管理员会话列表（含用户信息摘要）
     */
    List<Map<String, Object>> getAdminSessions(Integer status, String keyword);

    /**
     * 管理员回复（若会话尚未转人工则自动标记为转人工）
     */
    CustomerServiceMessage adminReply(Long sessionId, String content);

    /**
     * 结束会话
     */
    void endSession(Long sessionId);
}

