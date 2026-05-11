-- 退货/退款流程：订单表扩展字段（已有库执行一次）
USE `white_tea_db`;

ALTER TABLE `tb_order` ADD COLUMN `refund_prev_status` TINYINT(1) DEFAULT NULL COMMENT '申请退款前状态（1/2/3）';
ALTER TABLE `tb_order` ADD COLUMN `refund_reason` VARCHAR(500) DEFAULT NULL COMMENT '用户退款/退货说明';
ALTER TABLE `tb_order` ADD COLUMN `refund_apply_time` DATETIME DEFAULT NULL COMMENT '退款申请时间';
ALTER TABLE `tb_order` ADD COLUMN `refund_audit_time` DATETIME DEFAULT NULL COMMENT '退款审核时间';
ALTER TABLE `tb_order` ADD COLUMN `refund_admin_remark` VARCHAR(500) DEFAULT NULL COMMENT '驳回原因等';
