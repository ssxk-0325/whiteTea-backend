-- 单门店升级：订单表不再保存自提门店 ID（自提统一使用 tb_store 中唯一营业门店）
-- 在已存在库上执行前请备份。若列不存在会报错，可忽略或先检查 information_schema。

USE `white_tea_db`;

ALTER TABLE `tb_order` DROP COLUMN `store_id`;
