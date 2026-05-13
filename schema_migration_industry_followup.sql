-- 产业服务后续闭环：采摘对接信息 + 到岗签到；培训资料与费用说明
-- 执行前请备份数据库

ALTER TABLE `tb_experience_activity`
  ADD COLUMN `pick_meeting_point` VARCHAR(300) DEFAULT NULL COMMENT '采摘招募：集合地点（仅审核通过用户可见）' AFTER `status`,
  ADD COLUMN `pick_contact_line` VARCHAR(200) DEFAULT NULL COMMENT '采摘招募：联系人/电话' AFTER `pick_meeting_point`,
  ADD COLUMN `pick_notice` TEXT DEFAULT NULL COMMENT '采摘招募：上岗须知' AFTER `pick_contact_line`,
  ADD COLUMN `training_materials` TEXT DEFAULT NULL COMMENT '批发培训：学习资料/链接（多行文本）' AFTER `pick_notice`,
  ADD COLUMN `training_extra_hint` VARCHAR(500) DEFAULT NULL COMMENT '批发培训：费用与参训补充说明' AFTER `training_materials`;

ALTER TABLE `tb_industry_application`
  ADD COLUMN `join_code` VARCHAR(64) DEFAULT NULL COMMENT '审核通过后对接码' AFTER `admin_remark`,
  ADD COLUMN `checked_in_at` DATETIME DEFAULT NULL COMMENT '采摘到岗签到时间' AFTER `join_code`,
  ADD UNIQUE KEY `uk_industry_join_code` (`join_code`);
