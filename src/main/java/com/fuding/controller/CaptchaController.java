package com.fuding.controller;

import com.fuding.common.Result;
import com.fuding.service.CaptchaStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class CaptchaController {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CAPTCHA_LEN = 4;
    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(5);

    @Autowired
    private CaptchaStoreService captchaStoreService;

    @GetMapping("/captcha")
    public Result<Map<String, Object>> captcha() {
        try {
            String code = randomCode(CAPTCHA_LEN);
            String captchaId = UUID.randomUUID().toString().replace("-", "");
            captchaStoreService.put(captchaId, code, CAPTCHA_TTL);

            String imageBase64 = renderCaptchaBase64(code);

            Map<String, Object> data = new HashMap<>();
            data.put("captchaId", captchaId);
            data.put("imageBase64", imageBase64);
            data.put("expireSeconds", CAPTCHA_TTL.getSeconds());
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage() != null ? e.getMessage() : "验证码生成失败");
        }
    }

    private static String randomCode(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            int idx = RANDOM.nextInt(CAPTCHA_CHARS.length());
            sb.append(CAPTCHA_CHARS.charAt(idx));
        }
        return sb.toString();
    }

    private static String renderCaptchaBase64(String code) throws Exception {
        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("验证码为空");
        }

        int width = 120;
        int height = 44;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // background
        g.setColor(new Color(245, 247, 250));
        g.fillRect(0, 0, width, height);

        // noise lines
        for (int i = 0; i < 8; i++) {
            g.setColor(new Color(180 + RANDOM.nextInt(60), 180 + RANDOM.nextInt(60), 180 + RANDOM.nextInt(60)));
            int x1 = RANDOM.nextInt(width);
            int y1 = RANDOM.nextInt(height);
            int x2 = RANDOM.nextInt(width);
            int y2 = RANDOM.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }

        // noise dots
        for (int i = 0; i < 60; i++) {
            g.setColor(new Color(150 + RANDOM.nextInt(80), 150 + RANDOM.nextInt(80), 150 + RANDOM.nextInt(80)));
            int x = RANDOM.nextInt(width);
            int y = RANDOM.nextInt(height);
            g.fillRect(x, y, 2, 2);
        }

        // text
        g.setFont(new Font("Arial", Font.BOLD, 26));
        FontMetrics fm = g.getFontMetrics();
        int charWidth = width / (code.length() + 1);
        int baseY = (height - fm.getHeight()) / 2 + fm.getAscent();
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            g.setColor(new Color(40 + RANDOM.nextInt(80), 40 + RANDOM.nextInt(80), 40 + RANDOM.nextInt(80)));
            double angle = (RANDOM.nextDouble() - 0.5) * 0.5; // -0.25..0.25 rad
            int x = (i + 1) * charWidth - 8 + RANDOM.nextInt(6);
            int y = baseY + (RANDOM.nextInt(7) - 3);
            g.rotate(angle, x, y);
            g.drawString(String.valueOf(c), x, y);
            g.rotate(-angle, x, y);
        }
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
        // data url for <img src="">
        return "data:image/png;base64," + base64;
    }
}

