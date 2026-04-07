package com.fuding.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 仅在启用支付宝时创建客户端
 */
@Configuration
public class AlipayClientConfig {

    @Bean
    @ConditionalOnProperty(prefix = "alipay", name = "enabled", havingValue = "true")
    public AlipayClient alipayClient(AlipayProperties props) {
        return new DefaultAlipayClient(
                props.getGateway(),
                props.getAppId(),
                props.getAppPrivateKey(),
                "json",
                "UTF-8",
                props.getAlipayPublicKey(),
                "RSA2"
        );
    }
}
