package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文化内容实体类（白茶文化科普中心）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_culture_content")
public class CultureContent extends BaseEntity {

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 封面图片
     */
    @TableField("cover_image")
    private String coverImage;

    /**
     * 类型：1-白茶知识，2-制作工艺，3-品鉴技巧，4-历史文化
     */
    @TableField("type")
    private Integer type = 1;

    /**
     * 浏览量
     */
    @TableField("view_count")
    private Integer viewCount = 0;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount = 0;

    /**
     * 状态：0-草稿，1-发布
     */
    @TableField("status")
    private Integer status = 1;
}
