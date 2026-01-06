package com.fuding.controller;

import com.fuding.common.Result;
import com.fuding.entity.CustomerServiceTag;
import com.fuding.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tag控制器
 */
@RestController
@RequestMapping("/tag")
@CrossOrigin
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 获取Top N Tag列表
     */
    @GetMapping("/top")
    public Result<List<CustomerServiceTag>> getTopTags(@RequestParam(defaultValue = "10") int topN) {
        try {
            List<CustomerServiceTag> tags = tagService.getTopTags(topN);
            return Result.success(tags);
        } catch (Exception e) {
            return Result.error("获取Tag列表失败：" + e.getMessage());
        }
    }

    /**
     * 点击Tag，获取答案
     */
    @PostMapping("/{tagId}/click")
    public Result<Map<String, Object>> clickTag(@PathVariable Long tagId) {
        try {
            String answer = tagService.clickTag(tagId);
            if (answer == null) {
                return Result.error("Tag不存在");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("answer", answer);
            data.put("tagId", tagId);

            return Result.success("获取答案成功", data);
        } catch (Exception e) {
            return Result.error("获取答案失败：" + e.getMessage());
        }
    }

    /**
     * 手动触发Tag生成（管理员使用）
     */
    @PostMapping("/generate")
    public Result<Void> generateTags(@RequestParam(defaultValue = "10") int topN) {
        try {
            tagService.generateTopTags(topN);
            return Result.success("Tag生成成功", null);
        } catch (Exception e) {
            return Result.error("Tag生成失败：" + e.getMessage());
        }
    }
}

