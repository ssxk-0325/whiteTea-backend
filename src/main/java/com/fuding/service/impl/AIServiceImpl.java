package com.fuding.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fuding.service.AIService;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI服务实现类
 * 支持多种大模型API（文心一言、通义千问、ChatGPT等）
 */
@Service
public class AIServiceImpl implements AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);

    @Value("${ai.api.url:}")
    private String aiApiUrl;

    @Value("${ai.api.key:}")
    private String aiApiKey;

    @Value("${ai.api.type:wenxin}") // wenxin, tongyi, openai
    private String aiApiType;

    @Value("${ai.api.model:}")
    private String aiModel;

    /**
     * 默认系统提示词
     */
    private static final String DEFAULT_SYSTEM_PROMPT = "你是一个专业的福鼎白茶服务平台客服助手。" +
            "你的职责是帮助用户解答关于白茶产品、订单、支付、物流等相关问题。" +
            "请用友好、专业、简洁的语言回答用户的问题。" +
            "如果遇到无法解决的问题，建议用户联系人工客服。";

    @Override
    public String generateReply(String userMessage, List<String> conversationHistory) {
        return generateReplyWithContext(userMessage, conversationHistory, DEFAULT_SYSTEM_PROMPT);
    }

    @Override
    public String generateReplyWithContext(String userMessage, List<String> conversationHistory, String systemPrompt) {
        try {
            // 根据配置的API类型调用不同的接口
            switch (aiApiType.toLowerCase()) {
                case "wenxin":
                    return callWenxinAPI(userMessage, conversationHistory, systemPrompt);
                case "tongyi":
                    return callTongyiAPI(userMessage, conversationHistory, systemPrompt);
                case "openai":
                    return callOpenAIAPI(userMessage, conversationHistory, systemPrompt);
                default:
                    // 默认使用模拟回复（用于测试）
                    return getMockReply(userMessage);
            }
        } catch (Exception e) {
            logger.error("调用AI API失败：", e);
            return "抱歉，我现在无法回答您的问题，请稍后再试或联系人工客服。";
        }
    }

    /**
     * 调用文心一言API
     */
    private String callWenxinAPI(String userMessage, List<String> conversationHistory, String systemPrompt) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(aiApiUrl);

            // 设置请求头
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + aiApiKey);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiModel.isEmpty() ? "ernie-bot-turbo" : aiModel);
            
            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统提示词
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
            
            // 添加对话历史
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                for (String history : conversationHistory) {
                    String[] parts = history.split(":", 2);
                    if (parts.length == 2) {
                        Map<String, String> msg = new HashMap<>();
                        msg.put("role", parts[0].trim().equals("用户") ? "user" : "assistant");
                        msg.put("content", parts[1].trim());
                        messages.add(msg);
                    }
                }
            }
            
            // 添加当前用户消息
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_output_tokens", 2000);

            StringEntity entity = new StringEntity(JSON.toJSONString(requestBody), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            String responseBody = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

            // 解析响应
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            if (jsonResponse.containsKey("result")) {
                return jsonResponse.getString("result");
            } else if (jsonResponse.containsKey("error")) {
                logger.error("文心一言API错误：{}", jsonResponse.getString("error"));
                return getMockReply(userMessage);
            }

            httpClient.close();
            return getMockReply(userMessage);
        } catch (Exception e) {
            logger.error("调用文心一言API异常：", e);
            return getMockReply(userMessage);
        }
    }

    /**
     * 调用通义千问API
     */
    private String callTongyiAPI(String userMessage, List<String> conversationHistory, String systemPrompt) {
        // 通义千问API调用逻辑（类似文心一言）
        // 这里可以根据通义千问的实际API格式进行调整
        return callWenxinAPI(userMessage, conversationHistory, systemPrompt);
    }

    /**
     * 调用OpenAI API
     */
    private String callOpenAIAPI(String userMessage, List<String> conversationHistory, String systemPrompt) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(aiApiUrl);

            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + aiApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiModel.isEmpty() ? "gpt-3.5-turbo" : aiModel);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
            
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                for (String history : conversationHistory) {
                    String[] parts = history.split(":", 2);
                    if (parts.length == 2) {
                        Map<String, String> msg = new HashMap<>();
                        msg.put("role", parts[0].trim().equals("用户") ? "user" : "assistant");
                        msg.put("content", parts[1].trim());
                        messages.add(msg);
                    }
                }
            }
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            StringEntity entity = new StringEntity(JSON.toJSONString(requestBody), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            String responseBody = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

            JSONObject jsonResponse = JSON.parseObject(responseBody);
            if (jsonResponse.containsKey("choices") && jsonResponse.getJSONArray("choices").size() > 0) {
                JSONObject choice = jsonResponse.getJSONArray("choices").getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                return message.getString("content");
            }

            httpClient.close();
            return getMockReply(userMessage);
        } catch (Exception e) {
            logger.error("调用OpenAI API异常：", e);
            return getMockReply(userMessage);
        }
    }

    /**
     * 模拟回复（用于测试或API不可用时）
     */
    private String getMockReply(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        
        if (lowerMessage.contains("订单") || lowerMessage.contains("发货")) {
            return "关于订单问题，您可以在\"我的订单\"页面查看订单状态。如果订单已支付，通常会在48小时内发货。";
        } else if (lowerMessage.contains("价格") || lowerMessage.contains("多少钱")) {
            return "我们的产品价格因品种和规格而异，您可以在产品详情页查看具体价格。如有优惠活动，我们会及时通知。";
        } else if (lowerMessage.contains("支付") || lowerMessage.contains("付款")) {
            return "我们支持多种支付方式，包括微信支付、支付宝等。支付完成后，订单会立即生效。";
        } else if (lowerMessage.contains("物流") || lowerMessage.contains("快递")) {
            return "订单发货后，您可以在订单详情中查看物流信息。我们使用顺丰、中通等主流快递公司配送。";
        } else if (lowerMessage.contains("退货") || lowerMessage.contains("退款")) {
            return "如需退货或退款，请在订单详情页申请，或联系人工客服处理。我们会在收到退货后尽快处理退款。";
        } else {
            return "感谢您的咨询！我是福鼎白茶服务平台的智能客服。如果您有关于产品、订单、支付等方面的问题，我很乐意为您解答。如需更详细的帮助，也可以联系人工客服。";
        }
    }
}

