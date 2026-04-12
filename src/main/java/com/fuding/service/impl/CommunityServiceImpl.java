package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.CommunityComment;
import com.fuding.entity.CommunityPost;
import com.fuding.entity.CommunityPostDislike;
import com.fuding.entity.CommunityPostFavorite;
import com.fuding.entity.CommunityPostLike;
import com.fuding.entity.User;
import com.fuding.mapper.CommunityCommentMapper;
import com.fuding.mapper.CommunityPostDislikeMapper;
import com.fuding.mapper.CommunityPostFavoriteMapper;
import com.fuding.mapper.CommunityPostLikeMapper;
import com.fuding.mapper.CommunityPostMapper;
import com.fuding.mapper.UserMapper;
import com.fuding.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 社区服务实现类
 */
@Service
@Transactional
public class CommunityServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost> implements CommunityService {

    @Autowired
    private CommunityPostMapper postMapper;

    @Autowired
    private CommunityCommentMapper commentMapper;

    @Autowired
    private CommunityPostLikeMapper likeMapper;

    @Autowired
    private CommunityPostDislikeMapper dislikeMapper;

    @Autowired
    private CommunityPostFavoriteMapper favoriteMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public CommunityPost createPost(Long userId, String title, String content, String images, Integer type) {
        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        post.setImages(images);
        post.setType(type);
        post.setStatus(1); // 直接发布，不审核
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        postMapper.insert(post);
        return post;
    }

    @Override
    public IPage<Map<String, Object>> getPostList(Page<CommunityPost> page, Integer type, String keyword) {
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPost::getStatus, 1); // 只查询已发布的
        if (type != null) {
            wrapper.eq(CommunityPost::getType, type);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(CommunityPost::getTitle, keyword)
                    .or().like(CommunityPost::getContent, keyword));
        }
        wrapper.orderByDesc(CommunityPost::getCreateTime);

        IPage<CommunityPost> postPage = postMapper.selectPage(page, wrapper);

        // 转换为包含用户信息的Map
        IPage<Map<String, Object>> resultPage = postPage.convert(post -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("title", post.getTitle());
            map.put("content", post.getContent());
            map.put("images", post.getImages());
            map.put("type", post.getType());
            map.put("viewCount", post.getViewCount());
            map.put("likeCount", post.getLikeCount());
            map.put("commentCount", post.getCommentCount());
            map.put("createTime", post.getCreateTime());

            // 获取用户信息
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatar", user.getAvatar());
                map.put("user", userInfo);
            }

            return map;
        });

        return resultPage;
    }

    @Override
    public Map<String, Object> getPostDetail(Long postId, Long userId) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null || post.getStatus() != 1) {
            throw new RuntimeException("帖子不存在或已删除");
        }

        // 增加浏览量
        post.setViewCount(post.getViewCount() + 1);
        postMapper.updateById(post);

        Map<String, Object> result = new HashMap<>();
        result.put("id", post.getId());
        result.put("title", post.getTitle());
        result.put("content", post.getContent());
        result.put("images", post.getImages());
        result.put("type", post.getType());
        result.put("viewCount", post.getViewCount());
        result.put("likeCount", post.getLikeCount());
        result.put("commentCount", post.getCommentCount());
        result.put("createTime", post.getCreateTime());

        // 获取用户信息
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            result.put("user", userInfo);
        }

        // 检查用户操作状态
        if (userId != null) {
            result.put("userActions", checkUserActions(postId, userId));
        } else {
            Map<String, Boolean> actions = new HashMap<>();
            actions.put("liked", false);
            actions.put("disliked", false);
            actions.put("favorited", false);
            result.put("userActions", actions);
        }

        return result;
    }

    /** 用户类型 1 = 管理员，与 User 实体约定一致 */
    private boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        User u = userMapper.selectById(userId);
        return u != null && u.getUserType() != null && u.getUserType() == 1;
    }

    /** 删除帖子关联的评论、点赞、点踩、收藏（逻辑删除） */
    private void removePostRelations(Long postId) {
        LambdaQueryWrapper<CommunityComment> cw = new LambdaQueryWrapper<>();
        cw.eq(CommunityComment::getPostId, postId);
        commentMapper.delete(cw);

        LambdaQueryWrapper<CommunityPostLike> lw = new LambdaQueryWrapper<>();
        lw.eq(CommunityPostLike::getPostId, postId);
        likeMapper.delete(lw);

        LambdaQueryWrapper<CommunityPostDislike> dw = new LambdaQueryWrapper<>();
        dw.eq(CommunityPostDislike::getPostId, postId);
        dislikeMapper.delete(dw);

        LambdaQueryWrapper<CommunityPostFavorite> fw = new LambdaQueryWrapper<>();
        fw.eq(CommunityPostFavorite::getPostId, postId);
        favoriteMapper.delete(fw);
    }

    @Override
    public void deletePost(Long postId, Long userId) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        boolean owner = post.getUserId().equals(userId);
        if (!owner && !isAdmin(userId)) {
            throw new RuntimeException("无权删除该帖子");
        }
        removePostRelations(postId);
        postMapper.deleteById(postId);
    }

    @Override
    public CommunityComment addComment(Long userId, Long postId, Long parentId, String content) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null || post.getStatus() != 1) {
            throw new RuntimeException("帖子不存在或已删除");
        }

        CommunityComment comment = new CommunityComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(parentId != null ? parentId : 0L);
        comment.setContent(content);
        comment.setLikeCount(0);
        commentMapper.insert(comment);

        // 更新帖子评论数
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(post);

        return comment;
    }

    @Override
    public List<Map<String, Object>> getCommentList(Long postId) {
        LambdaQueryWrapper<CommunityComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityComment::getPostId, postId);
        wrapper.eq(CommunityComment::getParentId, 0); // 只获取顶级评论
        wrapper.orderByDesc(CommunityComment::getCreateTime);
        List<CommunityComment> comments = commentMapper.selectList(wrapper);

        return comments.stream().map(comment -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("content", comment.getContent());
            map.put("likeCount", comment.getLikeCount());
            map.put("createTime", comment.getCreateTime());

            // 获取用户信息
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatar", user.getAvatar());
                map.put("user", userInfo);
            }

            // 获取回复列表
            LambdaQueryWrapper<CommunityComment> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(CommunityComment::getPostId, postId);
            replyWrapper.eq(CommunityComment::getParentId, comment.getId());
            replyWrapper.orderByAsc(CommunityComment::getCreateTime);
            List<CommunityComment> replies = commentMapper.selectList(replyWrapper);

            List<Map<String, Object>> replyList = replies.stream().map(reply -> {
                Map<String, Object> replyMap = new HashMap<>();
                replyMap.put("id", reply.getId());
                replyMap.put("content", reply.getContent());
                replyMap.put("likeCount", reply.getLikeCount());
                replyMap.put("createTime", reply.getCreateTime());

                User replyUser = userMapper.selectById(reply.getUserId());
                if (replyUser != null) {
                    Map<String, Object> replyUserInfo = new HashMap<>();
                    replyUserInfo.put("id", replyUser.getId());
                    replyUserInfo.put("username", replyUser.getUsername());
                    replyUserInfo.put("nickname", replyUser.getNickname());
                    replyUserInfo.put("avatar", replyUser.getAvatar());
                    replyMap.put("user", replyUserInfo);
                }

                return replyMap;
            }).collect(Collectors.toList());

            map.put("replies", replyList);
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        CommunityComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        boolean owner = comment.getUserId().equals(userId);
        if (!owner && !isAdmin(userId)) {
            throw new RuntimeException("无权删除该评论");
        }

        // 更新帖子评论数：删除顶层评论时同时删除其下所有回复
        int dec = 1;
        Long parentId = comment.getParentId();
        if (parentId == null || parentId == 0L) {
            LambdaQueryWrapper<CommunityComment> rw = new LambdaQueryWrapper<>();
            rw.eq(CommunityComment::getParentId, commentId);
            Long replyCount = commentMapper.selectCount(rw);
            int n = replyCount != null ? replyCount.intValue() : 0;
            commentMapper.delete(rw);
            dec = 1 + n;
        }

        CommunityPost post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            post.setCommentCount(Math.max(0, post.getCommentCount() - dec));
            postMapper.updateById(post);
        }

        commentMapper.deleteById(commentId);
    }

    @Override
    public boolean likePost(Long postId, Long userId) {
        // 检查是否已点赞
        LambdaQueryWrapper<CommunityPostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPostLike::getPostId, postId);
        wrapper.eq(CommunityPostLike::getUserId, userId);
        CommunityPostLike existing = likeMapper.selectOne(wrapper);

        if (existing != null) {
            return false; // 已点赞
        }

        // 检查是否点踩，如果点踩则取消
        LambdaQueryWrapper<CommunityPostDislike> dislikeWrapper = new LambdaQueryWrapper<>();
        dislikeWrapper.eq(CommunityPostDislike::getPostId, postId);
        dislikeWrapper.eq(CommunityPostDislike::getUserId, userId);
        CommunityPostDislike dislike = dislikeMapper.selectOne(dislikeWrapper);
        if (dislike != null) {
            dislikeMapper.deleteById(dislike.getId());
        }

        // 尝试恢复已删除的记录（使用原生 SQL 绕过逻辑删除）
        // 如果恢复失败（没有已删除的记录），则插入新记录
        int restored = likeMapper.restoreDeletedLike(postId, userId);

        if (restored == 0) {
            // 没有已删除的记录，插入新记录
            CommunityPostLike like = new CommunityPostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            likeMapper.insert(like);
        }

        // 更新帖子点赞数
        CommunityPost post = postMapper.selectById(postId);
        if (post != null) {
            post.setLikeCount(post.getLikeCount() + 1);
            postMapper.updateById(post);
        }

        return true;
    }

    @Override
    public boolean unlikePost(Long postId, Long userId) {
        LambdaQueryWrapper<CommunityPostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPostLike::getPostId, postId);
        wrapper.eq(CommunityPostLike::getUserId, userId);
        CommunityPostLike like = likeMapper.selectOne(wrapper);

        if (like == null) {
            return false; // 未点赞
        }

        likeMapper.deleteById(like.getId());

        // 更新帖子点赞数
        CommunityPost post = postMapper.selectById(postId);
        if (post != null) {
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            postMapper.updateById(post);
        }

        return true;
    }

    @Override
    public boolean dislikePost(Long postId, Long userId) {
        // 检查是否已点踩
        LambdaQueryWrapper<CommunityPostDislike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPostDislike::getPostId, postId);
        wrapper.eq(CommunityPostDislike::getUserId, userId);
        CommunityPostDislike existing = dislikeMapper.selectOne(wrapper);

        if (existing != null) {
            return false; // 已点踩
        }

        // 检查是否点赞，如果点赞则取消
        LambdaQueryWrapper<CommunityPostLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(CommunityPostLike::getPostId, postId);
        likeWrapper.eq(CommunityPostLike::getUserId, userId);
        CommunityPostLike like = likeMapper.selectOne(likeWrapper);
        if (like != null) {
            likeMapper.deleteById(like.getId());
            CommunityPost post = postMapper.selectById(postId);
            if (post != null) {
                post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
                postMapper.updateById(post);
            }
        }

        // 尝试恢复已删除的点踩记录（使用原生 SQL 绕过逻辑删除）
        // 如果恢复失败（没有已删除的记录），则插入新记录
        int restored = dislikeMapper.restoreDeletedDislike(postId, userId);

        if (restored == 0) {
            // 没有已删除的记录，插入新记录
            CommunityPostDislike dislike = new CommunityPostDislike();
            dislike.setPostId(postId);
            dislike.setUserId(userId);
            dislikeMapper.insert(dislike);
        }

        return true;
    }

    @Override
    public boolean undislikePost(Long postId, Long userId) {
        LambdaQueryWrapper<CommunityPostDislike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPostDislike::getPostId, postId);
        wrapper.eq(CommunityPostDislike::getUserId, userId);
        CommunityPostDislike dislike = dislikeMapper.selectOne(wrapper);

        if (dislike == null) {
            return false; // 未点踩
        }

        dislikeMapper.deleteById(dislike.getId());
        return true;
    }

    @Override
    public boolean favoritePost(Long postId, Long userId) {
        // 检查是否已收藏
        LambdaQueryWrapper<CommunityPostFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPostFavorite::getPostId, postId);
        wrapper.eq(CommunityPostFavorite::getUserId, userId);
        CommunityPostFavorite existing = favoriteMapper.selectOne(wrapper);

        if (existing != null) {
            return false; // 已收藏
        }

        // 尝试恢复已删除的收藏记录（使用原生 SQL 绕过逻辑删除）
        // 如果恢复失败（没有已删除的记录），则插入新记录
        int restored = favoriteMapper.restoreDeletedFavorite(postId, userId);

        if (restored == 0) {
            // 没有已删除的记录，插入新记录
            CommunityPostFavorite favorite = new CommunityPostFavorite();
            favorite.setPostId(postId);
            favorite.setUserId(userId);
            favoriteMapper.insert(favorite);
        }

        return true;
    }

    @Override
    public boolean unfavoritePost(Long postId, Long userId) {
        LambdaQueryWrapper<CommunityPostFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPostFavorite::getPostId, postId);
        wrapper.eq(CommunityPostFavorite::getUserId, userId);
        CommunityPostFavorite favorite = favoriteMapper.selectOne(wrapper);

        if (favorite == null) {
            return false; // 未收藏
        }

        favoriteMapper.deleteById(favorite.getId());
        return true;
    }

    @Override
    public Map<String, Boolean> checkUserActions(Long postId, Long userId) {
        Map<String, Boolean> actions = new HashMap<>();

        // 检查点赞
        LambdaQueryWrapper<CommunityPostLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(CommunityPostLike::getPostId, postId);
        likeWrapper.eq(CommunityPostLike::getUserId, userId);
        actions.put("liked", likeMapper.selectCount(likeWrapper) > 0);

        // 检查点踩
        LambdaQueryWrapper<CommunityPostDislike> dislikeWrapper = new LambdaQueryWrapper<>();
        dislikeWrapper.eq(CommunityPostDislike::getPostId, postId);
        dislikeWrapper.eq(CommunityPostDislike::getUserId, userId);
        actions.put("disliked", dislikeMapper.selectCount(dislikeWrapper) > 0);

        // 检查收藏
        LambdaQueryWrapper<CommunityPostFavorite> favoriteWrapper = new LambdaQueryWrapper<>();
        favoriteWrapper.eq(CommunityPostFavorite::getPostId, postId);
        favoriteWrapper.eq(CommunityPostFavorite::getUserId, userId);
        actions.put("favorited", favoriteMapper.selectCount(favoriteWrapper) > 0);

        return actions;
    }

    @Override
    public IPage<Map<String, Object>> getUserLikedPosts(Page<CommunityPost> page, Long userId) {
        // 先查询点赞的帖子ID
        LambdaQueryWrapper<CommunityPostLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(CommunityPostLike::getUserId, userId);
        List<CommunityPostLike> likes = likeMapper.selectList(likeWrapper);

        if (likes.isEmpty()) {
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }

        List<Long> postIds = likes.stream().map(CommunityPostLike::getPostId).collect(Collectors.toList());

        // 查询帖子
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CommunityPost::getId, postIds);
        wrapper.eq(CommunityPost::getStatus, 1);
        wrapper.orderByDesc(CommunityPost::getCreateTime);

        IPage<CommunityPost> postPage = postMapper.selectPage(page, wrapper);

        // 转换为包含用户信息的Map
        return postPage.convert(post -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("title", post.getTitle());
            map.put("content", post.getContent());
            map.put("images", post.getImages());
            map.put("type", post.getType());
            map.put("viewCount", post.getViewCount());
            map.put("likeCount", post.getLikeCount());
            map.put("commentCount", post.getCommentCount());
            map.put("createTime", post.getCreateTime());

            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatar", user.getAvatar());
                map.put("user", userInfo);
            }

            return map;
        });
    }

    @Override
    public IPage<Map<String, Object>> getUserFavoritePosts(Page<CommunityPost> page, Long userId) {
        // 先查询收藏的帖子ID
        LambdaQueryWrapper<CommunityPostFavorite> favoriteWrapper = new LambdaQueryWrapper<>();
        favoriteWrapper.eq(CommunityPostFavorite::getUserId, userId);
        List<CommunityPostFavorite> favorites = favoriteMapper.selectList(favoriteWrapper);

        if (favorites.isEmpty()) {
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }

        List<Long> postIds = favorites.stream().map(CommunityPostFavorite::getPostId).collect(Collectors.toList());

        // 查询帖子
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CommunityPost::getId, postIds);
        wrapper.eq(CommunityPost::getStatus, 1);
        wrapper.orderByDesc(CommunityPost::getCreateTime);

        IPage<CommunityPost> postPage = postMapper.selectPage(page, wrapper);

        // 转换为包含用户信息的Map
        return postPage.convert(post -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("title", post.getTitle());
            map.put("content", post.getContent());
            map.put("images", post.getImages());
            map.put("type", post.getType());
            map.put("viewCount", post.getViewCount());
            map.put("likeCount", post.getLikeCount());
            map.put("commentCount", post.getCommentCount());
            map.put("createTime", post.getCreateTime());

            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatar", user.getAvatar());
                map.put("user", userInfo);
            }

            return map;
        });
    }
}

