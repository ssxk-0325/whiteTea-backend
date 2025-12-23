package com.fuding.util;

import java.security.MessageDigest;

/**
 * MD5加密工具类
 */
public class Md5Util {

    private static final String SALT = "fuding_white_tea_2023";

    /**
     * MD5加密
     */
    public static String encrypt(String password) {
        try {
            String str = password + SALT;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes("utf-8"));
            return toHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 转换为16进制
     */
    private static String toHex(byte[] bytes) {
        final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            ret.append(HEX_DIGITS[(b >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[b & 0x0f]);
        }
        return ret.toString();
    }
}

