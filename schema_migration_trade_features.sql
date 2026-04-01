-- 已有库增量执行：商品收藏 + 订单评价（若表已存在会报错，可忽略或手动删表后执行）
USE `white_tea_db`;

CREATE TABLE IF NOT EXISTS `tb_product_favorite` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `product_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品收藏表';

CREATE TABLE IF NOT EXISTS `tb_order_review` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `rating` TINYINT(1) NOT NULL COMMENT '评分：1-5',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` INT(11) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单评价表';

-- 订单优惠与返积分字段（若已存在可忽略执行报错）
ALTER TABLE `tb_order` ADD COLUMN `coupon_id` BIGINT(20) DEFAULT NULL COMMENT '优惠券ID';
ALTER TABLE `tb_order` ADD COLUMN `coupon_code` VARCHAR(50) DEFAULT NULL COMMENT '优惠券码';
ALTER TABLE `tb_order` ADD COLUMN `order_mode` TINYINT(1) DEFAULT 0 COMMENT '下单模式：0-普通购买，1-拼团购买';
ALTER TABLE `tb_order` ADD COLUMN `group_discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '拼团折扣金额';
ALTER TABLE `tb_order` ADD COLUMN `wholesale_discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '批发折扣金额';
ALTER TABLE `tb_order` ADD COLUMN `coupon_discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠券抵扣金额';
ALTER TABLE `tb_order` ADD COLUMN `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '总优惠金额';
ALTER TABLE `tb_order` ADD COLUMN `reward_points` INT(11) DEFAULT 0 COMMENT '购物奖励积分';
