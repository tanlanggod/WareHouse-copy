package com.warehouse.dto;

import lombok.Data;

/**
 * 图片上传响应DTO
 */
@Data
public class ImageUploadResponse {
    private String imageUrl;
    private Long version;
}

