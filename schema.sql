-- ============================================
-- 福鼎白茶服务平台数据库表结构
-- 数据库名: white_tea_db
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `white_tea_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `white_tea_db`;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `gender` TINYINT(1) DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `user_type` TINYINT(1) DEFAULT 0 COMMENT '用户类型：0-普通用户，1-管理员',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 产品分类表
-- ============================================
DROP TABLE IF EXISTS `tb_category`;
CREATE TABLE `tb_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '分类描述',
  `image` VARCHAR(255) DEFAULT NULL COMMENT '分类图片',
  `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
  `sort_order` INT(11) DEFAULT 0 COMMENT '排序',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品分类表';

-- ============================================
-- 3. 产品表
-- ============================================
DROP TABLE IF EXISTS `tb_product`;
CREATE TABLE `tb_product` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '产品名称',
  `description` TEXT COMMENT '产品描述',
  `image` VARCHAR(255) DEFAULT NULL COMMENT '产品图片（主图）',
  `images` TEXT COMMENT '产品图片列表（JSON格式）',
  `category_id` BIGINT(20) NOT NULL COMMENT '分类ID',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `stock` INT(11) DEFAULT 0 COMMENT '库存',
  `sales` INT(11) DEFAULT 0 COMMENT '销量',
  `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位（如：克、斤、盒）',
  `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格',
  `origin` VARCHAR(100) DEFAULT NULL COMMENT '产地',
  `year` VARCHAR(10) DEFAULT NULL COMMENT '年份',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sales` (`sales`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表';

-- ============================================
-- 4. 购物车表
-- ============================================
DROP TABLE IF EXISTS `tb_cart`;
CREATE TABLE `tb_cart` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `product_id` BIGINT(20) NOT NULL COMMENT '产品ID',
  `quantity` INT(11) DEFAULT 1 COMMENT '数量',
  `selected` TINYINT(1) DEFAULT 1 COMMENT '是否选中：0-未选中，1-选中',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- ============================================
-- 5. 订单表
-- ============================================
DROP TABLE IF EXISTS `tb_order`;
CREATE TABLE `tb_order` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
  `receiver_address` VARCHAR(255) NOT NULL COMMENT '收货地址',
  `status` TINYINT(1) DEFAULT 0 COMMENT '订单状态：0-待付款，1-待发货，2-待收货，3-已完成，4-已取消，5-退款中，6-已退款',
  `pay_type` TINYINT(1) DEFAULT 0 COMMENT '支付方式：0-未支付，1-微信，2-支付宝，3-银行卡',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================
-- 6. 订单项表
-- ============================================
DROP TABLE IF EXISTS `tb_order_item`;
CREATE TABLE `tb_order_item` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID',
  `product_id` BIGINT(20) NOT NULL COMMENT '产品ID',
  `product_name` VARCHAR(100) DEFAULT NULL COMMENT '产品名称（快照）',
  `product_image` VARCHAR(255) DEFAULT NULL COMMENT '产品图片（快照）',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `quantity` INT(11) NOT NULL COMMENT '数量',
  `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';

-- ============================================
-- 7. 收货地址表
-- ============================================
DROP TABLE IF EXISTS `tb_address`;
CREATE TABLE `tb_address` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区县',
  `detail` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT(1) DEFAULT 0 COMMENT '是否默认：0-否，1-是',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- ============================================
-- 8. 文化内容表（白茶文化科普中心）
-- ============================================
DROP TABLE IF EXISTS `tb_culture_content`;
CREATE TABLE `tb_culture_content` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT COMMENT '内容（文章内容或视频描述）',
  `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片',
  `content_type` TINYINT(1) DEFAULT 1 COMMENT '内容类型：1-文章，2-视频',
  `type` TINYINT(1) DEFAULT 1 COMMENT '分类类型：1-白茶知识，2-制作工艺，3-品鉴技巧，4-历史文化',
  `video_url` VARCHAR(500) DEFAULT NULL COMMENT '视频URL（仅视频类型使用）',
  `video_duration` INT(11) DEFAULT NULL COMMENT '视频时长（秒，仅视频类型使用）',
  `view_count` INT(11) DEFAULT 0 COMMENT '浏览量',
  `like_count` INT(11) DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-草稿，1-发布',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_content_type` (`content_type`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_view_count` (`view_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文化内容表';

-- ============================================
-- 9. 体验活动表
-- ============================================
DROP TABLE IF EXISTS `tb_experience_activity`;
CREATE TABLE `tb_experience_activity` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '活动名称',
  `description` TEXT COMMENT '活动描述',
  `image` VARCHAR(255) DEFAULT NULL COMMENT '活动图片',
  `type` TINYINT(1) DEFAULT 1 COMMENT '活动类型：1-茶艺课，2-茶园参观，3-线下品鉴会，4-制茶体验',
  `price` DECIMAL(10,2) DEFAULT NULL COMMENT '价格',
  `start_time` DATETIME DEFAULT NULL COMMENT '活动开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '活动结束时间',
  `coupon_start_time` DATETIME DEFAULT NULL COMMENT '抢券开始时间',
  `coupon_end_time` DATETIME DEFAULT NULL COMMENT '抢券结束时间',
  `total_coupons` INT(11) DEFAULT 0 COMMENT '总券数',
  `issued_coupons` INT(11) DEFAULT 0 COMMENT '已发放券数',
  `max_participants` INT(11) DEFAULT 0 COMMENT '报名人数上限',
  `current_participants` INT(11) DEFAULT 0 COMMENT '已报名人数',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束，3-已取消',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_coupon_start_time` (`coupon_start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='体验活动表';

-- ============================================
-- 10. 用户活动券表
-- ============================================
DROP TABLE IF EXISTS `tb_user_activity_coupon`;
CREATE TABLE `tb_user_activity_coupon` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `activity_id` BIGINT(20) NOT NULL COMMENT '活动ID',
  `coupon_code` VARCHAR(50) NOT NULL COMMENT '券码（唯一）',
  `coupon_name` VARCHAR(100) NOT NULL COMMENT '券名称',
  `coupon_type` TINYINT(1) DEFAULT 1 COMMENT '券类型：1-茶艺课，2-茶园参观，3-线下品鉴会，4-制茶体验',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-未使用，1-已使用，2-已过期',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coupon_code` (`coupon_code`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_status` (`status`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户活动券表';

-- ============================================
-- 10. 社区帖子表
-- ============================================
DROP TABLE IF EXISTS `tb_community_post`;
CREATE TABLE `tb_community_post` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT COMMENT '内容',
  `images` TEXT COMMENT '图片列表（JSON格式）',
  `type` TINYINT(1) DEFAULT 1 COMMENT '类型：1-分享，2-提问，3-讨论',
  `view_count` INT(11) DEFAULT 0 COMMENT '浏览量',
  `like_count` INT(11) DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT(11) DEFAULT 0 COMMENT '评论数',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-待审核，1-已发布，2-已删除',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区帖子表';

-- ============================================
-- 11. 社区评论表
-- ============================================
DROP TABLE IF EXISTS `tb_community_comment`;
CREATE TABLE `tb_community_comment` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` BIGINT(20) NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父评论ID（用于回复，0表示顶级评论）',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `like_count` INT(11) DEFAULT 0 COMMENT '点赞数',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区评论表';

-- ============================================
-- 12. 社区帖子点赞表
-- ============================================
DROP TABLE IF EXISTS `tb_community_post_like`;
CREATE TABLE `tb_community_post_like` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` BIGINT(20) NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区帖子点赞表';

-- ============================================
-- 13. 社区帖子点踩表
-- ============================================
DROP TABLE IF EXISTS `tb_community_post_dislike`;
CREATE TABLE `tb_community_post_dislike` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` BIGINT(20) NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区帖子点踩表';

-- ============================================
-- 14. 社区帖子收藏表
-- ============================================
DROP TABLE IF EXISTS `tb_community_post_favorite`;
CREATE TABLE `tb_community_post_favorite` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` BIGINT(20) NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区帖子收藏表';

-- ============================================
-- 15. 趣味问答问题表
-- ============================================
DROP TABLE IF EXISTS `tb_quiz_question`;
CREATE TABLE `tb_quiz_question` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `question` TEXT NOT NULL COMMENT '问题内容',
  `options` TEXT NOT NULL COMMENT '选项（JSON格式，如：["选项A","选项B","选项C","选项D"]）',
  `correct_answer` INT(11) NOT NULL COMMENT '正确答案索引（0-3）',
  `explanation` TEXT COMMENT '答案解析',
  `category` TINYINT(1) DEFAULT 1 COMMENT '分类：1-互动，2-文化，3-活动',
  `difficulty` TINYINT(1) DEFAULT 1 COMMENT '难度：1-简单，2-中等，3-困难',
  `image` VARCHAR(255) DEFAULT NULL COMMENT '问题图片',
  `view_count` INT(11) DEFAULT 0 COMMENT '答题次数',
  `correct_count` INT(11) DEFAULT 0 COMMENT '答对次数',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-草稿，1-发布',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_difficulty` (`difficulty`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='趣味问答问题表';

-- ============================================
-- 16. 用户答题记录表
-- ============================================
DROP TABLE IF EXISTS `tb_quiz_answer`;
CREATE TABLE `tb_quiz_answer` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `question_id` BIGINT(20) NOT NULL COMMENT '问题ID',
  `user_answer` INT(11) NOT NULL COMMENT '用户答案索引（0-3）',
  `is_correct` TINYINT(1) DEFAULT 0 COMMENT '是否答对：0-错误，1-正确',
  `create_time` DATETIME DEFAULT NULL COMMENT '答题时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_question_id` (`question_id`),
  KEY `idx_create_time` (`create_time`),
  UNIQUE KEY `uk_user_question` (`user_id`, `question_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户答题记录表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入默认管理员用户
-- 用户名：admin
-- 密码：admin123（使用加盐MD5加密，盐值：fuding_white_tea_2023）
-- 注意：如果登录失败，请执行下面的UPDATE语句更新密码
INSERT INTO `tb_user` (`username`, `password`, `nickname`, `user_type`, `status`, `create_time`, `update_time`) 
VALUES ('admin', 'E10ADC3949BA59ABBE56E057F20F883E', '管理员', 1, 1, NOW(), NOW());

-- 如果登录失败，请取消下面的注释并执行，将密码更新为正确的加盐MD5值
-- 密码 admin123 加盐后的MD5值：需要运行程序生成
-- UPDATE `tb_user` SET `password` = '7689D227F21769F641941E9DABD03370' WHERE `username` = 'admin';

-- 插入默认分类数据
INSERT INTO `tb_category` (`name`, `description`, `parent_id`, `sort_order`, `status`, `create_time`, `update_time`) VALUES
('白毫银针', '白毫银针是白茶中的珍品', 0, 1, 1, NOW(), NOW()),
('白牡丹', '白牡丹是白茶中的上品', 0, 2, 1, NOW(), NOW()),
('寿眉', '寿眉是白茶中的常见品种', 0, 3, 1, NOW(), NOW()),
('贡眉', '贡眉是白茶中的传统品种', 0, 4, 1, NOW(), NOW());

-- ============================================
-- 17. 客服会话表
-- ============================================
DROP TABLE IF EXISTS `tb_customer_service_session`;
CREATE TABLE `tb_customer_service_session` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_no` VARCHAR(50) NOT NULL COMMENT '会话编号',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `status` TINYINT(1) DEFAULT 0 COMMENT '会话状态：0-进行中，1-已结束，2-已转人工',
  `last_message_time` DATETIME DEFAULT NULL COMMENT '最后一条消息时间',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_no` (`session_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服会话表';

-- ============================================
-- 18. 客服消息表
-- ============================================
DROP TABLE IF EXISTS `tb_customer_service_message`;
CREATE TABLE `tb_customer_service_message` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` BIGINT(20) NOT NULL COMMENT '会话ID',
  `sender_type` TINYINT(1) NOT NULL COMMENT '发送者类型：0-用户，1-客服（AI），2-人工客服',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `message_type` TINYINT(1) DEFAULT 0 COMMENT '消息类型：0-文本，1-图片，2-文件',
  `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_sender_type` (`sender_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服消息表';

-- ============================================
-- 19. 客服问题统计表（用于生成Tag）
-- ============================================
DROP TABLE IF EXISTS `tb_customer_service_question`;
CREATE TABLE `tb_customer_service_question` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `question` TEXT NOT NULL COMMENT '问题内容',
  `question_hash` VARCHAR(64) NOT NULL COMMENT '问题哈希值（用于去重）',
  `ask_count` INT(11) DEFAULT 1 COMMENT '提问次数',
  `user_count` INT(11) DEFAULT 1 COMMENT '提问用户数',
  `last_ask_time` DATETIME DEFAULT NULL COMMENT '最后提问时间',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_hash` (`question_hash`),
  KEY `idx_ask_count` (`ask_count`),
  KEY `idx_last_ask_time` (`last_ask_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服问题统计表';

-- ============================================
-- 20. 客服Tag表
-- ============================================
DROP TABLE IF EXISTS `tb_customer_service_tag`;
CREATE TABLE `tb_customer_service_tag` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tag_name` VARCHAR(100) NOT NULL COMMENT 'Tag名称',
  `tag_description` VARCHAR(255) DEFAULT NULL COMMENT 'Tag描述',
  `answer` TEXT COMMENT 'Tag对应的答案',
  `hit_count` INT(11) DEFAULT 0 COMMENT '点击次数',
  `question_count` INT(11) DEFAULT 0 COMMENT '关联的问题数量',
  `score` DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Tag得分（用于排序）',
  `sort_order` INT(11) DEFAULT 0 COMMENT '排序',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_score` (`score`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服Tag表';

-- ============================================
-- 21. Tag与问题关联表
-- ============================================
DROP TABLE IF EXISTS `tb_customer_service_tag_question`;
CREATE TABLE `tb_customer_service_tag_question` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tag_id` BIGINT(20) NOT NULL COMMENT 'Tag ID',
  `question_id` BIGINT(20) NOT NULL COMMENT '问题ID',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_question` (`tag_id`, `question_id`),
  KEY `idx_tag_id` (`tag_id`),
  KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tag与问题关联表';

