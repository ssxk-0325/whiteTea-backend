package com.fuding;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 福鼎白茶服务平台主启动类
 * 
 * @author 陈泳铭
 * @date 2023
 */
@SpringBootApplication
@MapperScan("com.fuding.mapper")
public class WhiteTeaPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhiteTeaPlatformApplication.class, args);
        System.out.println("=================================");
        System.out.println("福鼎白茶服务平台启动成功！");
        System.out.println("访问地址: http://localhost:8080/api");
        System.out.println("=================================");
    }
}

