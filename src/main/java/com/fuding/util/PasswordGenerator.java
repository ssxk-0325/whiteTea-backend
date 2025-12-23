package com.fuding.util;

/**
 * 密码生成工具类（用于生成正确的管理员密码）
 * 运行此类的main方法可以生成密码的MD5加盐值
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        String password = "admin123";
        String encrypted = Md5Util.encrypt(password);
        System.out.println("=================================");
        System.out.println("密码: " + password);
        System.out.println("加盐MD5值: " + encrypted);
        System.out.println("=================================");
        System.out.println("请将以下SQL语句中的密码值更新为上面的MD5值：");
        System.out.println("UPDATE `tb_user` SET `password` = '" + encrypted + "' WHERE `username` = 'admin';");
        System.out.println("=================================");
    }
}

