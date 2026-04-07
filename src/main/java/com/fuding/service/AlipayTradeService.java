package com.fuding.service;

import javax.servlet.http.HttpServletRequest;

/**
 * 支付宝电脑网站支付（沙箱/正式）
 */
public interface AlipayTradeService {

    /**
     * 生成跳转支付宝收银台的 HTML 表单片段（含自动提交 form）
     */
    String buildPagePayForm(Long userId, Long orderId);

    /**
     * 处理异步通知：验签并更新订单
     * @return true 表示处理成功，应回复支付宝 success
     */
    boolean handleNotify(HttpServletRequest request);
}
