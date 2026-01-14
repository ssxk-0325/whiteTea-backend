package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 浏览历史实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_browse_history")
public class BrowseHistory extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 内容类型 (1: 帖子, 2: 产品, 3: 文化内容)
     */
    @TableField("target_type")
    private Integer targetType;

    /**
     * 内容ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 标题 (冗余以便快速显示)
     */
    @TableField("title")
    private String title;

    /**
     * 图片 (冗余以便快速显示)
     */
    @TableField("image")
    private String image;
}

