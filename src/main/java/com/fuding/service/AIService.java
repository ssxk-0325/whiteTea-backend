package com.fuding.service;

import java.util.List;

/**
 * AI服务接口
 * 用于调用大模型API
 */
public interface AIService {

    /**
     * 调用大模型API生成回复
     * 
     * @param userMessage 用户消息
     * @param conversationHistory 对话历史
     * @return AI生成的回复
     */
    String generateReply(String userMessage, List<String> conversationHistory);

    /**
     * 调用大模型API生成回复（带上下文）
     * 
     * @param userMessage 用户消息
     * @param conversationHistory 对话历史（格式：["用户:xxx", "AI:xxx", ...]）
     * @param systemPrompt 系统提示词
     * @return AI生成的回复
     */
    String generateReplyWithContext(String userMessage, List<String> conversationHistory, String systemPrompt);
}

