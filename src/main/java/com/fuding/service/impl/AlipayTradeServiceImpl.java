package com.fuding.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.fuding.config.AlipayProperties;
import com.fuding.entity.Order;
import com.fuding.service.AlipayTradeService;
import com.fuding.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class AlipayTradeServiceImpl implements AlipayTradeService {

    private static final Logger log = LoggerFactory.getLogger(AlipayTradeServiceImpl.class);

    @Autowired
    private AlipayProperties alipayProperties;

    @Autowired(required = false)
    private AlipayClient alipayClient;

    @Autowired
    private OrderService orderService;

    @Override
    public String buildPagePayForm(Long userId, Long orderId) {
        if (!alipayProperties.isEnabled() || alipayClient == null) {
            throw new RuntimeException("支付宝支付未启用，请在 application.yml 中配置 alipay.enabled=true 及密钥");
        }
        if (alipayProperties.getAppId() == null || alipayProperties.getAppId().isEmpty()) {
            throw new RuntimeException("未配置 alipay.app-id");
        }

        Order order = orderService.getOrderById(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该订单");
        }
        if (order.getStatus() == null || order.getStatus() != 0) {
            throw new RuntimeException("订单状态不正确，无法发起支付");
        }

        String base = alipayProperties.getServerBaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String notifyUrl = base + "/order/alipay/notify";
        String returnUrl = base + "/order/alipay/return";

        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(notifyUrl);
            request.setReturnUrl(returnUrl);

            JSONObject biz = new JSONObject();
            biz.put("out_trade_no", order.getOrderNo());
            biz.put("total_amount", order.getPayAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
            biz.put("subject", "福鼎白茶订单-" + order.getOrderNo());
            biz.put("product_code", "FAST_INSTANT_TRADE_PAY");
            request.setBizContent(biz.toJSONString());

            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (response == null || response.getBody() == null) {
                throw new RuntimeException("支付宝下单失败：无返回内容");
            }
            return response.getBody();
        } catch (AlipayApiException e) {
            log.error("支付宝 pageExecute 失败", e);
            throw new RuntimeException("支付宝下单失败：" + e.getMessage());
        }
    }

    @Override
    public boolean handleNotify(HttpServletRequest request) {
        if (!alipayProperties.isEnabled()) {
            return false;
        }
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            if (values == null || values.length == 0) {
                continue;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                sb.append(values[i]);
                if (i < values.length - 1) {
                    sb.append(",");
                }
            }
            params.put(name, sb.toString());
        }

        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    "UTF-8",
                    "RSA2"
            );
            if (!signVerified) {
                log.warn("支付宝异步通知验签失败");
                return false;
            }
        } catch (AlipayApiException e) {
            log.error("支付宝验签异常", e);
            return false;
        }

        String tradeStatus = params.get("trade_status");
        if (tradeStatus == null) {
            return false;
        }
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            log.info("忽略非成功状态通知: {}", tradeStatus);
            return true;
        }

        String outTradeNo = params.get("out_trade_no");
        String totalAmount = params.get("total_amount");
        if (outTradeNo == null || totalAmount == null) {
            return false;
        }

        try {
            orderService.payOrderFromAlipayNotify(outTradeNo, totalAmount);
            return true;
        } catch (Exception e) {
            log.error("处理支付宝通知更新订单失败", e);
            return false;
        }
    }
}
