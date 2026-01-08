package com.warehouse.controller;

import com.warehouse.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * 文件访问控制器
 */
@RestController
@RequestMapping("/files")
@CrossOrigin
public class FileController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private static final String AVATAR_UPLOAD_DIR = UPLOAD_DIR + "avatars/";
    private static final String PRODUCT_UPLOAD_DIR = UPLOAD_DIR + "products/";
    private static final String ANNOUNCEMENT_UPLOAD_DIR = UPLOAD_DIR + "announcements/";

    /**
     * 获取头像文件
     */
    @GetMapping("/avatars/{filename}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(AVATAR_UPLOAD_DIR + filename);

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);

            // 根据文件扩展名设置Content-Type
            String contentType = "image/jpeg"; // 默认
            if (filename.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (filename.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            } else if (filename.toLowerCase().endsWith(".webp")) {
                contentType = "image/webp";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取商品图片文件
     */
    @GetMapping("/products/{filename}")
    public ResponseEntity<Resource> getProductImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(PRODUCT_UPLOAD_DIR + filename);

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);

            // 根据文件扩展名设置Content-Type
            String contentType = "image/jpeg"; // 默认
            if (filename.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (filename.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            } else if (filename.toLowerCase().endsWith(".webp")) {
                contentType = "image/webp";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取公告附件文件
     */
    @GetMapping("/announcements/{filename}")
    public ResponseEntity<Resource> getAnnouncementFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(ANNOUNCEMENT_UPLOAD_DIR + filename);

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);

            // 根据文件扩展名设置Content-Type
            String contentType = "application/octet-stream"; // 默认
            String lowerFilename = filename.toLowerCase();
            if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (lowerFilename.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerFilename.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (lowerFilename.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (lowerFilename.endsWith(".doc") || lowerFilename.endsWith(".docx")) {
                contentType = "application/msword";
            } else if (lowerFilename.endsWith(".xls") || lowerFilename.endsWith(".xlsx")) {
                contentType = "application/vnd.ms-excel";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 通用文件上传接口
     */
    @PostMapping("/upload")
    public Result<Object> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 检查文件大小 (10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("文件大小不能超过10MB");
            }

            // 检查文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return Result.error("文件名不能为空");
            }

            String fileExtension = getFileExtension(originalFilename);
            if (!isAllowedFileType(fileExtension)) {
                return Result.error("不支持的文件类型");
            }

            // 创建上传目录
            Path uploadDir = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
            Path filePath = uploadDir.resolve(uniqueFilename);

            // 保存文件
            Files.copy(file.getInputStream(), filePath);

            // 构建返回结果
            String fileUrl = "/files/" + uniqueFilename;
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("url", fileUrl);
            result.put("originalName", originalFilename);
            result.put("filename", uniqueFilename);
            result.put("size", file.getSize());
            result.put("contentType", file.getContentType());

            return Result.success(result);

        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("上传过程中发生错误: " + e.getMessage());
        }
    }

    /**
     * 公告附件上传接口
     */
    @PostMapping("/announcements/upload")
    public Result<Object> uploadAnnouncementFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 检查文件大小 (10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("文件大小不能超过10MB");
            }

            // 检查文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return Result.error("文件名不能为空");
            }

            String fileExtension = getFileExtension(originalFilename);
            if (!isAllowedFileType(fileExtension)) {
                return Result.error("不支持的文件类型，仅支持图片、PDF、Word、Excel文件");
            }

            // 创建公告上传目录
            Path uploadDir = Paths.get(ANNOUNCEMENT_UPLOAD_DIR);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
            Path filePath = uploadDir.resolve(uniqueFilename);

            // 保存文件
            Files.copy(file.getInputStream(), filePath);

            // 构建返回结果
            String fileUrl = "/files/announcements/" + uniqueFilename;
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("url", fileUrl);
            result.put("originalName", originalFilename);
            result.put("filename", uniqueFilename);
            result.put("size", file.getSize());
            result.put("contentType", file.getContentType());

            return Result.success(result);

        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("上传过程中发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 检查是否为允许的文件类型
     */
    private boolean isAllowedFileType(String extension) {
        String[] allowedTypes = {
            "jpg", "jpeg", "png", "gif", "bmp", "webp",  // 图片
            "pdf",  // PDF
            "doc", "docx",  // Word
            "xls", "xlsx",  // Excel
            "txt",  // 文本
            "zip", "rar"  // 压缩文件
        };

        for (String allowedType : allowedTypes) {
            if (allowedType.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}