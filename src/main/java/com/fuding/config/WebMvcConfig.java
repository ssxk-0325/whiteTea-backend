package com.fuding.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 将本地上传目录映射为可访问的静态 URL（与 FileUploadController 返回路径一致）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String normalized = uploadPath.replace("\\", "/");
        if (!normalized.endsWith("/")) {
            normalized += "/";
        }
        String location = "file:" + normalized;
        registry.addResourceHandler("/uploads/white-tea/**")
                .addResourceLocations(location);
    }
}
