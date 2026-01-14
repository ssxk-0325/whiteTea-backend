package com.fuding.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.entity.CommunityComment;
import com.fuding.entity.CommunityPost;
import java.util.List;
import java.util.Map;

/**
 * 社区服务接口
 */
public interface CommunityService {

    /**
     * 创建帖子
     */
    CommunityPost createPost(Long userId, String title, String content, String images, Integer type);

    /**
     * 获取帖子列表（分页）
     */
    IPage<Map<String, Object>> getPostList(Page<CommunityPost> page, Integer type, String keyword);

    /**
     * 获取帖子详情
     */
    Map<String, Object> getPostDetail(Long postId, Long userId);

    /**
     * 删除帖子
     */
    void deletePost(Long postId, Long userId);

    /**
     * 添加评论
     */
    CommunityComment addComment(Long userId, Long postId, Long parentId, String content);

    /**
     * 获取评论列表
     */
    List<Map<String, Object>> getCommentList(Long postId);

    /**
     * 删除评论
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 点赞帖子
     */
    boolean likePost(Long postId, Long userId);

    /**
     * 取消点赞
     */
    boolean unlikePost(Long postId, Long userId);

    /**
     * 点踩帖子
     */
    boolean dislikePost(Long postId, Long userId);

    /**
     * 取消点踩
     */
    boolean undislikePost(Long postId, Long userId);

    /**
     * 收藏帖子
     */
    boolean favoritePost(Long postId, Long userId);

    /**
     * 取消收藏
     */
    boolean unfavoritePost(Long postId, Long userId);

    /**
     * 检查用户是否点赞/点踩/收藏
     */
    Map<String, Boolean> checkUserActions(Long postId, Long userId);

    /**
     * 获取用户点赞的帖子列表
     */
    IPage<Map<String, Object>> getUserLikedPosts(Page<CommunityPost> page, Long userId);

    /**
     * 获取用户收藏的帖子列表
     */
    IPage<Map<String, Object>> getUserFavoritePosts(Page<CommunityPost> page, Long userId);
}

