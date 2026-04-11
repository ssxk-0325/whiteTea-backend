-- 已有库升级：结算优惠券改为积分商城兑换券（执行一次）
ALTER TABLE `tb_reward`
  ADD COLUMN `coupon_discount_type` TINYINT(1) DEFAULT NULL COMMENT '优惠券规则(仅type=2)：1减20 2减30 3减50 4减80 5减5% 6减12%' AFTER `type`;

ALTER TABLE `tb_reward_exchange`
  ADD COLUMN `order_id` BIGINT(20) DEFAULT NULL COMMENT '下单使用该券抵扣时关联的订单ID' AFTER `remark`,
  ADD KEY `idx_order_id` (`order_id`);
