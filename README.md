# 福鼎白茶服务平台 - 后端

## 项目简介
基于 SpringBoot + MyBatis-Plus 的福鼎白茶服务平台后端系统

## 技术栈
- Spring Boot 2.7.14
- MyBatis-Plus 3.5.3.1
- MySQL 8.0
- Redis
- JWT
- Maven

## 项目结构
```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/fuding/
│   │   │   ├── common/          # 通用类
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/      # 控制器
│   │   │   ├── entity/          # 实体类
│   │   │   ├── mapper/          # Mapper接口
│   │   │   ├── service/         # 服务层
│   │   │   ├── util/            # 工具类
│   │   │   └── WhiteTeaPlatformApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
└── schema.sql                   # 数据库表结构SQL
```

## 数据库配置
1. 创建数据库：`white_tea_db`
2. 执行 `schema.sql` 文件创建表结构
3. 修改 `application.yml` 中的数据库连接信息

## 启动步骤
1. 确保已安装 JDK 1.8+、Maven、MySQL、Redis
2. 创建数据库并执行 schema.sql
3. 修改 application.yml 中的数据库配置
4. 运行 `WhiteTeaPlatformApplication.java`
5. 访问地址：http://localhost:8080/api

## API 接口
- 用户相关：/api/user/**
- 产品相关：/api/product/**
- 分类相关：/api/category/**
- 购物车相关：/api/cart/**

## MyBatis-Plus 特性
- 自动填充创建时间和更新时间
- 逻辑删除支持
- 分页插件
- 条件构造器

## 注意事项
- 默认端口：8080
- JWT Token 有效期：24小时
- 文件上传路径需要提前创建
- 逻辑删除字段：deleted（0-未删除，1-已删除）
