package com.fuding.controller;

import com.fuding.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/upload")
@CrossOrigin
public class FileUploadController {

    @Value("${server.servlet.context-path:}")
    private String servletContextPath;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.max-size}")
    private Long maxSize;

    /**
     * 上传图片
     */
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 检查文件大小
            if (file.getSize() > maxSize) {
                return Result.error("文件大小超过限制（最大10MB）");
            }

            // 检查文件类型
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)$")) {
                return Result.error("不支持的文件类型，仅支持jpg、jpeg、png、gif、webp");
            }

            // 生成唯一文件名
            String filename = UUID.randomUUID().toString() + extension;
            String imageDir = uploadPath + "images/";
            File dir = new File(imageDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 流式落盘，避免大文件时一次性读入内存
            Path path = Paths.get(imageDir + filename);
            file.transferTo(path.toFile());

            String url = buildPublicFileUrl("/uploads/white-tea/images/" + filename);
            return Result.success("上传成功", url);
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传视频
     */
    @PostMapping("/video")
    public Result<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 检查文件大小（与 spring.servlet.multipart 上限一致，200MB）
            long videoMaxSize = 200L * 1024 * 1024;
            if (file.getSize() > videoMaxSize) {
                return Result.error("文件大小超过限制（最大200MB）");
            }

            // 检查文件类型
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (!extension.matches("\\.(mp4|webm|ogg|avi|mov|wmv|flv)$")) {
                return Result.error("不支持的文件类型，仅支持mp4、webm、ogg、avi、mov、wmv、flv");
            }

            // 生成唯一文件名
            String filename = UUID.randomUUID().toString() + extension;
            String videoDir = uploadPath + "videos/";
            File dir = new File(videoDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 流式落盘，避免大文件时一次性读入内存
            Path path = Paths.get(videoDir + filename);
            file.transferTo(path.toFile());

            String url = buildPublicFileUrl("/uploads/white-tea/videos/" + filename);
            return Result.success("上传成功", url);
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    /** 拼接 context-path，使前端通过同一 /api 前缀访问上传文件 */
    private String buildPublicFileUrl(String path) {
        String cp = servletContextPath != null ? servletContextPath : "";
        if (cp.endsWith("/")) {
            cp = cp.substring(0, cp.length() - 1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return cp + path;
    }
}

