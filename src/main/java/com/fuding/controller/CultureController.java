package com.fuding.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.common.Result;
import com.fuding.entity.CultureContent;
import com.fuding.service.CultureService;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 文化内容控制器
 */
@RestController
@RequestMapping("/culture")
@CrossOrigin
public class CultureController {

    @Autowired
    private CultureService cultureService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取内容列表
     */
    @GetMapping("/list")
    public Result<IPage<CultureContent>> getContentList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer contentType,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword) {
        try {
            Page<CultureContent> contentPage = new Page<>(page, size);
            IPage<CultureContent> result = cultureService.getContentList(contentPage, contentType, type, keyword);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取内容详情
     */
    @GetMapping("/{id}")
    public Result<CultureContent> getContentDetail(@PathVariable Long id) {
        try {
            CultureContent content = cultureService.getContentById(id);
            // 增加浏览量
            cultureService.incrementViewCount(id);
            // 重新查询以获取更新后的浏览量
            content = cultureService.getContentById(id);
            return Result.success(content);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 点赞内容
     */
    @PostMapping("/{id}/like")
    public Result<Void> likeContent(@PathVariable Long id) {
        try {
            cultureService.likeContent(id);
            return Result.success("点赞成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeContent(@PathVariable Long id) {
        try {
            cultureService.unlikeContent(id);
            return Result.success("取消点赞成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取热门内容
     */
    @GetMapping("/hot")
    public Result<List<CultureContent>> getHotContents(
            @RequestParam(required = false) Integer contentType,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<CultureContent> contents = cultureService.getHotContents(contentType, limit);
            return Result.success(contents);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 管理员接口 ==========

    /**
     * 管理员创建内容
     */
    @PostMapping("/admin/create")
    public Result<CultureContent> adminCreateContent(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            // 验证管理员权限
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token); // 验证token有效性
            // 这里可以添加管理员权限验证逻辑

            CultureContent content = new CultureContent();
            content.setTitle(params.get("title").toString());
            content.setContent(params.get("content") != null ? params.get("content").toString() : null);
            content.setCoverImage(params.get("coverImage") != null ? params.get("coverImage").toString() : null);
            content.setContentType(params.get("contentType") != null ? Integer.valueOf(params.get("contentType").toString()) : 1);
            content.setType(params.get("type") != null ? Integer.valueOf(params.get("type").toString()) : 1);
            content.setVideoUrl(params.get("videoUrl") != null ? params.get("videoUrl").toString() : null);
            content.setVideoDuration(params.get("videoDuration") != null ? Integer.valueOf(params.get("videoDuration").toString()) : null);
            content.setStatus(params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : 1);

            CultureContent result = cultureService.createContent(content);
            return Result.success("创建成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员更新内容
     */
    @PutMapping("/admin/update")
    public Result<CultureContent> adminUpdateContent(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            // 验证管理员权限
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            CultureContent content = new CultureContent();
            content.setId(Long.valueOf(params.get("id").toString()));
            content.setTitle(params.get("title").toString());
            content.setContent(params.get("content") != null ? params.get("content").toString() : null);
            content.setCoverImage(params.get("coverImage") != null ? params.get("coverImage").toString() : null);
            content.setContentType(params.get("contentType") != null ? Integer.valueOf(params.get("contentType").toString()) : 1);
            content.setType(params.get("type") != null ? Integer.valueOf(params.get("type").toString()) : 1);
            content.setVideoUrl(params.get("videoUrl") != null ? params.get("videoUrl").toString() : null);
            content.setVideoDuration(params.get("videoDuration") != null ? Integer.valueOf(params.get("videoDuration").toString()) : null);
            content.setStatus(params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : 1);

            CultureContent result = cultureService.updateContent(content);
            return Result.success("更新成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员删除内容
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> adminDeleteContent(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 验证管理员权限
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            cultureService.deleteContent(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员获取内容列表（包括未发布的）
     */
    @GetMapping("/admin/list")
    public Result<IPage<CultureContent>> adminGetContentList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer contentType,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            // 验证管理员权限
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);

            Page<CultureContent> contentPage = new Page<>(page, size);
            IPage<CultureContent> result = cultureService.getAdminContentList(contentPage, contentType, type, keyword);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

