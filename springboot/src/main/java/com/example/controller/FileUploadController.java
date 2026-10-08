package com.example.controller;

import com.example.common.Result;
import com.example.utils.UploadPathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private static final Set<String> FORUM_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp",
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
            ".txt", ".md", ".zip", ".rar", ".7z"
    ));

    @Value("${server.port:9001}")
    private String serverPort;

    @Value("${app.upload-dir:../upload/files}")
    private String uploadDir;

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    @PostMapping("/avatar")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            StoredFile stored = store(file, "", null, null);
            return Result.success(stored.url);
        } catch (IllegalArgumentException e) {
            return Result.error("400", e.getMessage());
        } catch (IOException e) {
            return Result.error("500", "文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/paper")
    public Result uploadPaper(@RequestParam("file") MultipartFile file) {
        try {
            StoredFile stored = store(file, "paper", ".pdf",
                    new HashSet<>(Arrays.asList(".pdf")));
            return Result.success(stored.url);
        } catch (IllegalArgumentException e) {
            return Result.error("400", e.getMessage());
        } catch (IOException e) {
            return Result.error("500", "文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/file")
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        return uploadAvatar(file);
    }

    @PostMapping("/forum")
    public Result uploadForumFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.getSize() > 80L * 1024L * 1024L) {
                return Result.error("400", "单个文件不能超过80MB");
            }
            String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            StoredFile stored = store(file, "forum/" + month, null, FORUM_EXTENSIONS);
            Map<String, Object> data = new HashMap<>();
            data.put("fileName", stored.originalName);
            data.put("fileUrl", stored.url);
            data.put("fileType", file.getContentType());
            data.put("fileSize", file.getSize());
            data.put("url", stored.url);
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            return Result.error("400", e.getMessage());
        } catch (IOException e) {
            return Result.error("500", "文件上传失败: " + e.getMessage());
        }
    }

    private StoredFile store(MultipartFile file,
                             String subdirectory,
                             String forcedExtension,
                             Set<String> allowedExtensions) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String originalName = file.getOriginalFilename() == null
                ? "file"
                : file.getOriginalFilename();
        String extension = extensionOf(originalName);
        if (forcedExtension != null) {
            if (!forcedExtension.equalsIgnoreCase(extension)) {
                throw new IllegalArgumentException("只支持" + forcedExtension.substring(1).toUpperCase() + "文件");
            }
            extension = forcedExtension;
        }
        if (allowedExtensions != null && !allowedExtensions.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("不支持该文件格式");
        }
        Path root = UploadPathUtils.resolve(uploadDir);
        Path directory = subdirectory == null || subdirectory.isEmpty()
                ? root
                : root.resolve(subdirectory).normalize();
        if (!directory.startsWith(root)) {
            throw new IllegalArgumentException("上传目录不合法");
        }
        Files.createDirectories(directory);
        String fileName = UUID.randomUUID().toString() + extension;
        Path destination = directory.resolve(fileName).normalize();
        file.transferTo(destination.toFile());
        String relative = subdirectory == null || subdirectory.isEmpty()
                ? fileName
                : subdirectory.replace('\\', '/') + "/" + fileName;
        return new StoredFile(originalName, buildUrl(relative));
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot).toLowerCase();
    }

    private String buildUrl(String relativePath) {
        String base = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        if (base.isEmpty()) {
            base = "http://localhost:" + serverPort;
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/files/" + relativePath;
    }

    private static class StoredFile {
        private final String originalName;
        private final String url;

        private StoredFile(String originalName, String url) {
            this.originalName = originalName;
            this.url = url;
        }
    }
}
