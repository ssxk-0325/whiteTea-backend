package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客服会话实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_customer_service_session")
public class CustomerServiceSession extends BaseEntity {

    /**
     * 会话编号
     */
    @TableField("session_no")
    private String sessionNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 会话状态：0-进行中，1-已结束，2-已转人工
     */
    @TableField("status")
    private Integer status = 0;

    /**
     * 最后一条消息时间
     */
    @TableField("last_message_time")
    private java.time.LocalDateTime lastMessageTime;
}

