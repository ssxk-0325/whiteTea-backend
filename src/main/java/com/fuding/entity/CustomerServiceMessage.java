package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客服消息实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_customer_service_message")
public class CustomerServiceMessage extends BaseEntity {

    /**
     * 会话ID
     */
    @TableField("session_id")
    private Long sessionId;

    /**
     * 发送者类型：0-用户，1-客服（AI），2-人工客服
     */
    @TableField("sender_type")
    private Integer senderType;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 消息类型：0-文本，1-图片，2-文件
     */
    @TableField("message_type")
    private Integer messageType = 0;

    /**
     * 是否已读：0-未读，1-已读
     */
    @TableField("is_read")
    private Integer isRead = 0;
}

