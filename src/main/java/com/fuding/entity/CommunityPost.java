package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社区帖子实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_community_post")
public class CommunityPost extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

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
     * 图片列表（JSON格式）
     */
    @TableField("images")
    private String images;

    /**
     * 类型：1-分享，2-提问，3-讨论
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
     * 评论数
     */
    @TableField("comment_count")
    private Integer commentCount = 0;

    /**
     * 状态：0-待审核，1-已发布，2-已删除
     */
    @TableField("status")
    private Integer status = 0;
}
