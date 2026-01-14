package com.fuding.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.common.Result;
import com.fuding.entity.BrowseHistory;
import com.fuding.service.BrowseHistoryService;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 浏览历史控制器
 */
@RestController
@RequestMapping("/browse-history")
@CrossOrigin
public class BrowseHistoryController {

    @Autowired
    private BrowseHistoryService browseHistoryService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 记录浏览历史
     */
    @PostMapping("/record")
    public Result<Void> record(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            if (userId == null) return Result.success(null); // 游客不记录或返回成功不处理

            Integer targetType = (Integer) params.get("targetType");
            Long targetId = Long.valueOf(params.get("targetId").toString());
            String title = (String) params.get("title");
            String image = (String) params.get("image");

            browseHistoryService.record(userId, targetType, targetId, title, image);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取浏览历史列表
     */
    @GetMapping("/list")
    public Result<IPage<BrowseHistory>> getList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            if (userId == null) return Result.error("未登录");

            Page<BrowseHistory> historyPage = new Page<>(page, size);
            IPage<BrowseHistory> result = browseHistoryService.getUserHistory(historyPage, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除单条历史
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            BrowseHistory history = browseHistoryService.getById(id);
            if (history != null && history.getUserId().equals(userId)) {
                browseHistoryService.removeById(id);
            }
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 清空浏览历史
     */
    @DeleteMapping("/clear")
    public Result<Void> clear(HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            browseHistoryService.clearHistory(userId);
            return Result.success("清空成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                return jwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}

