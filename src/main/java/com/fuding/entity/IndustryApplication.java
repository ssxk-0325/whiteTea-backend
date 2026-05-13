package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 产业服务报名/加入申请（采摘招募、批发与培训）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_industry_application")
public class IndustryApplication extends BaseEntity {

    @TableField("activity_id")
    private Long activityId;

    @TableField("user_id")
    private Long userId;

    @TableField("real_name")
    private String realName;

    @TableField("phone")
    private String phone;

    @TableField("location")
    private String location;

    @TableField("remark")
    private String remark;

    /**
     * 状态：0-待审核，1-通过，2-驳回
     */
    @TableField("status")
    private Integer status = 0;

    @TableField("admin_remark")
    private String adminRemark;

    /** 审核通过后生成的对接码（现场核对） */
    @TableField("join_code")
    private String joinCode;

    /** 采摘招募：管理员到岗签到时间 */
    @TableField("checked_in_at")
    private LocalDateTime checkedInAt;
}

