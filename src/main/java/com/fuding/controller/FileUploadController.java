package com.fuding.controller;

import com.fuding.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

            // 保存文件
            Path path = Paths.get(imageDir + filename);
            Files.write(path, file.getBytes());

            // 返回访问URL（实际项目中应该配置静态资源访问路径）
            String url = "/uploads/white-tea/images/" + filename;
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

            // 检查文件大小（视频可以更大，这里设置为100MB）
            long videoMaxSize = 100 * 1024 * 1024; // 100MB
            if (file.getSize() > videoMaxSize) {
                return Result.error("文件大小超过限制（最大100MB）");
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

            // 保存文件
            Path path = Paths.get(videoDir + filename);
            Files.write(path, file.getBytes());

            // 返回访问URL
            String url = "/uploads/white-tea/videos/" + filename;
            return Result.success("上传成功", url);
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}

