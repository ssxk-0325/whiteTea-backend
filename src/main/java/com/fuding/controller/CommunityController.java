package com.fuding.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.common.Result;
import com.fuding.entity.CommunityComment;
import com.fuding.entity.CommunityPost;
import com.fuding.service.CommunityService;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 社区控制器
 */
@RestController
@RequestMapping("/community")
@CrossOrigin
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 创建帖子
     */
    @PostMapping("/post/create")
    public Result<CommunityPost> createPost(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            String title = params.get("title").toString();
            String content = params.get("content").toString();
            String images = params.get("images") != null ? params.get("images").toString() : null;
            Integer type = params.get("type") != null ? Integer.valueOf(params.get("type").toString()) : 1;

            CommunityPost post = communityService.createPost(userId, title, content, images, type);
            return Result.success("发布成功", post);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取帖子列表
     */
    @GetMapping("/post/list")
    public Result<IPage<Map<String, Object>>> getPostList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword) {
        try {
            // MyBatis-Plus 的 Page 从 1 开始，前端传的是从 0 开始，需要 +1
            Page<CommunityPost> postPage = new Page<>(page + 1, size);
            IPage<Map<String, Object>> result = communityService.getPostList(postPage, type, keyword);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/post/{id}")
    public Result<Map<String, Object>> getPostDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = null;
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    token = token.substring(7);
                    userId = jwtUtil.getUserIdFromToken(token);
                } catch (Exception e) {
                    // Token无效，继续以游客身份访问
                }
            }

            Map<String, Object> post = communityService.getPostDetail(id, userId);
            return Result.success(post);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/post/{id}")
    public Result<Void> deletePost(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            communityService.deletePost(id, userId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加评论
     */
    @PostMapping("/comment/add")
    public Result<CommunityComment> addComment(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            Long postId = Long.valueOf(params.get("postId").toString());
            Long parentId = params.get("parentId") != null ? Long.valueOf(params.get("parentId").toString()) : null;
            String content = params.get("content").toString();

            CommunityComment comment = communityService.addComment(userId, postId, parentId, content);
            return Result.success("评论成功", comment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取评论列表
     */
    @GetMapping("/comment/list/{postId}")
    public Result<List<Map<String, Object>>> getCommentList(@PathVariable Long postId) {
        try {
            List<Map<String, Object>> comments = communityService.getCommentList(postId);
            return Result.success(comments);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            communityService.deleteComment(id, userId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 点赞帖子
     */
    @PostMapping("/post/{id}/like")
    public Result<Map<String, Object>> likePost(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            boolean success = communityService.likePost(id, userId);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("liked", success);
            result.put("userActions", communityService.checkUserActions(id, userId));
            return Result.success(success ? "点赞成功" : "已点赞", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/post/{id}/like")
    public Result<Map<String, Object>> unlikePost(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            boolean success = communityService.unlikePost(id, userId);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("liked", false);
            result.put("userActions", communityService.checkUserActions(id, userId));
            return Result.success("取消点赞成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 点踩帖子
     */
    @PostMapping("/post/{id}/dislike")
    public Result<Map<String, Object>> dislikePost(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            boolean success = communityService.dislikePost(id, userId);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("disliked", success);
            result.put("userActions", communityService.checkUserActions(id, userId));
            return Result.success(success ? "点踩成功" : "已点踩", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消点踩
     */
    @DeleteMapping("/post/{id}/dislike")
    public Result<Map<String, Object>> undislikePost(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            boolean success = communityService.undislikePost(id, userId);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("disliked", false);
            result.put("userActions", communityService.checkUserActions(id, userId));
            return Result.success("取消点踩成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 收藏帖子
     */
    @PostMapping("/post/{id}/favorite")
    public Result<Map<String, Object>> favoritePost(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            boolean success = communityService.favoritePost(id, userId);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("favorited", success);
            result.put("userActions", communityService.checkUserActions(id, userId));
            return Result.success(success ? "收藏成功" : "已收藏", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/post/{id}/favorite")
    public Result<Map<String, Object>> unfavoritePost(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            boolean success = communityService.unfavoritePost(id, userId);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("favorited", false);
            result.put("userActions", communityService.checkUserActions(id, userId));
            return Result.success("取消收藏成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户点赞的帖子列表
     */
    @GetMapping("/post/likes")
    public Result<IPage<Map<String, Object>>> getUserLikedPosts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            // MyBatis-Plus 的 Page 从 1 开始，前端传的是从 0 开始，需要 +1
            Page<CommunityPost> postPage = new Page<>(page + 1, size);
            IPage<Map<String, Object>> result = communityService.getUserLikedPosts(postPage, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户收藏的帖子列表
     */
    @GetMapping("/post/favorites")
    public Result<IPage<Map<String, Object>>> getUserFavoritePosts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            // MyBatis-Plus 的 Page 从 1 开始，前端传的是从 0 开始，需要 +1
            Page<CommunityPost> postPage = new Page<>(page + 1, size);
            IPage<Map<String, Object>> result = communityService.getUserFavoritePosts(postPage, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

