package com.fuding.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaStoreService {

    private static final String REDIS_KEY_PREFIX = "captcha:";
    private final Map<String, LocalCaptcha> localStore = new ConcurrentHashMap<>();

    @Autowired(required = false)
    @Nullable
    private StringRedisTemplate stringRedisTemplate;

    public void put(String captchaId, String code, Duration ttl) {
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(code)) {
            return;
        }
        if (tryPutToRedis(captchaId, code, ttl)) {
            return;
        }
        long expireAt = System.currentTimeMillis() + ttl.toMillis();
        localStore.put(captchaId, new LocalCaptcha(code.toLowerCase(), expireAt));
    }

    public String getAndRemove(String captchaId) {
        if (!StringUtils.hasText(captchaId)) {
            return null;
        }
        String fromRedis = tryGetAndRemoveFromRedis(captchaId);
        if (fromRedis != null) {
            return fromRedis;
        }
        LocalCaptcha local = localStore.remove(captchaId);
        if (local == null) {
            return null;
        }
        if (System.currentTimeMillis() > local.expireAt) {
            return null;
        }
        return local.code;
    }

    private boolean tryPutToRedis(String captchaId, String code, Duration ttl) {
        try {
            if (stringRedisTemplate == null) {
                return false;
            }
            stringRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX + captchaId, code.toLowerCase(), ttl);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private String tryGetAndRemoveFromRedis(String captchaId) {
        try {
            if (stringRedisTemplate == null) {
                return null;
            }
            String key = REDIS_KEY_PREFIX + captchaId;
            String expected = stringRedisTemplate.opsForValue().get(key);
            if (expected != null) {
                stringRedisTemplate.delete(key);
            }
            return expected;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static class LocalCaptcha {
        private final String code;
        private final long expireAt;

        private LocalCaptcha(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }
}

