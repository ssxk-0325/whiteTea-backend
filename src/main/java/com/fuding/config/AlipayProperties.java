package com.fuding.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝开放平台配置（沙箱/正式均使用同一套参数，沙箱网关见 gateway）
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    /**
     * 是否启用支付宝支付（未配置密钥时请保持 false）
     */
    private boolean enabled = false;

    /**
     * 网关：沙箱 https://openapi.alipaydev.com/gateway.do ，正式 https://openapi.alipay.com/gateway.do
     */
    private String gateway = "https://openapi.alipaydev.com/gateway.do";

    private String appId = "";

    /**
     * 应用私钥（PKCS8 一行或 PEM，勿提交真实密钥到公开仓库）
     */
    private String appPrivateKey = "";

    /**
     * 支付宝公钥（非应用公钥）
     */
    private String alipayPublicKey = "";

    /**
     * 对外访问本服务的根地址，须含 context-path，例如 http://localhost:8080/api
     * 用于拼接异步通知、同步跳转地址；异步通知需公网可达（本地可用内网穿透）
     */
    private String serverBaseUrl = "http://localhost:8080/api";

    /**
     * 用户支付完成后浏览器跳转的前端地址
     */
    private String frontendRedirectUrl = "http://localhost:5173/orders";
}
