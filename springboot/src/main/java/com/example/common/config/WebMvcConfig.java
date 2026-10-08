package com.example.common.config;

import com.example.utils.UploadPathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:../upload/files}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadRoot = UploadPathUtils.resolve(uploadDir);
        registry.addResourceHandler("/files/**")
                .addResourceLocations(
                    "classpath:/static/files/",
                    uploadRoot.toUri().toString()
                );
    }
}
