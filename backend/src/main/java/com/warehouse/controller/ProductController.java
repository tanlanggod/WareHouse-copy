package com.warehouse.controller;

import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.dto.ImageUploadResponse;
import com.warehouse.entity.Product;
import com.warehouse.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 商品管理接口，覆盖查询、增删改等操作。
 */
@RestController
@RequestMapping("/products")
@CrossOrigin
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public Result<PageResult<Product>> getProducts(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer minStockQty,
            @RequestParam(required = false) Integer maxStockQty,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Product> result = productService.getProducts(
                code, name, categoryId, status, minStockQty, maxStockQty, page, size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Integer id) {
        Product product = productService.getProductById(id);
        return Result.success(product);
    }

    @PostMapping
    public Result<Product> createProduct(@RequestBody Product product) {
        Product created = productService.createProduct(product);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Product> updateProduct(@PathVariable Integer id, @RequestBody Product product) {
        Product updated = productService.updateProduct(id, product);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return Result.success(null);
    }

    @GetMapping("/low-stock")
    public Result<List<Product>> getLowStockProducts() {
        List<Product> products = productService.getLowStockProducts();
        return Result.success(products);
    }

    /**
     * 导出商品数据到Excel
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel() {
        try {
            byte[] data = productService.exportToExcel();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "products.xlsx");

            return ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 从Excel导入商品数据
     */
    @PostMapping("/import")
    public Result<List<String>> importFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            List<String> errors = productService.importFromExcel(file);

            if (errors.isEmpty()) {
                return Result.success("导入成功", errors);
            } else {
                return Result.success("导入完成，部分数据有误", errors);
            }
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 上传商品图片
     */
    @PostMapping("/{id}/image")
    public Result<ImageUploadResponse> uploadProductImage(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("请上传图片文件");
            }

            // 检查文件大小 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("图片大小不能超过5MB");
            }

            // 上传图片并获取更新后的商品信息（包含最新版本号）
            Product updatedProduct = productService.uploadProductImageWithVersion(id, file);

            // 为了保持向后兼容，创建一个包含图片URL和版本号的对象
            ImageUploadResponse response = new ImageUploadResponse();
            response.setImageUrl(updatedProduct.getImageUrl());
            response.setVersion(updatedProduct.getVersion());

            return Result.success("图片上传成功", response);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品图片
     */
    @DeleteMapping("/{id}/image")
    public Result<ImageUploadResponse> deleteProductImage(@PathVariable Integer id) {
        try {
            Product updatedProduct = productService.deleteProductImageWithVersion(id);

            // 为了保持向后兼容，创建一个包含图片URL和版本号的对象
            ImageUploadResponse response = new ImageUploadResponse();
            response.setImageUrl(updatedProduct.getImageUrl());
            response.setVersion(updatedProduct.getVersion());

            return Result.success("图片删除成功", response);
        } catch (Exception e) {
            return Result.error("图片删除失败: " + e.getMessage());
        }
    }
}

