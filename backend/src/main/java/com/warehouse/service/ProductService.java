package com.warehouse.service;

import com.warehouse.common.PageResult;
import com.warehouse.entity.Category;
import com.warehouse.entity.Product;
import com.warehouse.entity.Supplier;
import com.warehouse.repository.BaseRepository;
import com.warehouse.repository.CategoryRepository;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.SupplierRepository;
import com.warehouse.util.UserContext;
import com.warehouse.util.ExcelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 商品业务服务，封装商品的增删改查及库存告警逻辑。
 */
@Service
public class ProductService extends BaseService<Product, Integer> {

    private static final String PRODUCT_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/products/";

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    protected BaseRepository<Product, Integer> getRepository() {
        return productRepository;
    }

    protected Integer getCurrentUserId() {
        try {
            return UserContext.getCurrentUserId();
        } catch (Exception e) {
            return 1; // 系统用户ID
        }
    }

    @Cacheable(value = "products", keyGenerator = "cacheKeyGenerator", unless = "#result.records.size() > 1000")
    public PageResult<Product> getProducts(String code, String name, Integer categoryId, Integer status,
                                           Integer minStockQty, Integer maxStockQty,
                                           Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> productPage = productRepository.findActiveByConditions(
                code, name, categoryId, status, pageable);
        return new PageResult<>(productPage.getTotalElements(), productPage.getContent());
    }

    public Product getProductById(Integer id) {
        return productRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
    }

    @Transactional
    @CacheEvict(value = {"products", "lowStockProducts"}, allEntries = true)
    public Product createProduct(Product product) {
        // 设置用户上下文
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            if (productRepository.existsActiveByCode(product.getCode())) {
                throw new RuntimeException("商品编号已存在");
            }
            if (product.getCategory() != null && product.getCategory().getId() != null) {
                product.setCategory(categoryRepository.findActiveById(product.getCategory().getId())
                        .orElseThrow(() -> new RuntimeException("商品类别不存在")));
            }
            if (product.getSupplier() != null && product.getSupplier().getId() != null) {
                product.setSupplier(supplierRepository.findActiveById(product.getSupplier().getId())
                        .orElseThrow(() -> new RuntimeException("供应商不存在")));
            }
            return create(product); // 使用BaseService的create方法，自动设置审计字段
        } finally {
            UserContext.clear();
        }
    }

    @Transactional
    @CacheEvict(value = {"products", "lowStockProducts"}, allEntries = true)
    public Product updateProduct(Integer id, Product product) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            Product existingProduct = productRepository.findActiveById(id)
                    .orElseThrow(() -> new RuntimeException("商品不存在"));

            if (!existingProduct.getCode().equals(product.getCode()) &&
                productRepository.existsActiveByCode(product.getCode())) {
                throw new RuntimeException("商品编号已存在");
            }

            // 检查版本号（乐观锁）- 仅当前端提供了版本号时才检查
            if (product.getVersion() != null && !existingProduct.getVersion().equals(product.getVersion())) {
                logger.warn("乐观锁冲突：商品ID={}, 数据库版本={}, 前端版本={}",
                    id, existingProduct.getVersion(), product.getVersion());
                throw new RuntimeException("商品已被其他用户修改，请刷新页面后重试");
            } else if (product.getVersion() == null) {
                // 如果前端没有传递版本号，使用数据库版本号
                logger.info("前端未传递版本号，使用数据库版本号：商品ID={}, 版本={}",
                    id, existingProduct.getVersion());
            }

            existingProduct.setName(product.getName());
            existingProduct.setCode(product.getCode());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setMinStock(product.getMinStock());
            existingProduct.setUnit(product.getUnit());
            existingProduct.setBarcode(product.getBarcode());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setStatus(product.getStatus());

            if (product.getCategory() != null && product.getCategory().getId() != null) {
                existingProduct.setCategory(categoryRepository.findActiveById(product.getCategory().getId())
                        .orElseThrow(() -> new RuntimeException("商品类别不存在")));
            }
            if (product.getSupplier() != null && product.getSupplier().getId() != null) {
                existingProduct.setSupplier(supplierRepository.findActiveById(product.getSupplier().getId())
                        .orElseThrow(() -> new RuntimeException("供应商不存在")));
            }

            return update(existingProduct); // 使用BaseService的update方法
        } finally {
            UserContext.clear();
        }
    }

    @Transactional
    @CacheEvict(value = {"products", "lowStockProducts"}, allEntries = true)
    public void deleteProduct(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            if (!productRepository.existsActiveById(id)) {
                throw new RuntimeException("商品不存在");
            }
            softDelete(id); // 使用逻辑删除
        } finally {
            UserContext.clear();
        }
    }

    @Transactional
    @CacheEvict(value = {"products", "lowStockProducts"}, allEntries = true)
    public void restoreProduct(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            restore(id); // 恢复删除的商品
        } finally {
            UserContext.clear();
        }
    }

    @Cacheable(value = "lowStockProducts", unless = "#result.size() > 500")
    public List<Product> getLowStockProducts() {
        return productRepository.findActiveLowStockProducts();
    }

    /**
     * 导出商品数据到Excel
     */
    public byte[] exportToExcel() throws IOException {
        List<Product> products = productRepository.findAll();

        String[] headers = {"商品ID", "商品编号", "商品名称", "分类", "供应商", "单价", "库存数量", "最低库存", "单位", "条形码", "状态"};

        return ExcelUtil.exportExcel(products, headers,
                Product::getId,
                Product::getCode,
                Product::getName,
                p -> p.getCategory() != null ? p.getCategory().getName() : "",
                p -> p.getSupplier() != null ? p.getSupplier().getName() : "",
                Product::getPrice,
                Product::getStockQty,
                Product::getMinStock,
                Product::getUnit,
                p -> p.getBarcode() != null ? p.getBarcode() : "",
                p -> p.getStatus() == 1 ? "启用" : "禁用"
        );
    }

    /**
     * 从Excel导入商品数据
     */
    @Transactional
    @CacheEvict(value = {"products", "lowStockProducts"}, allEntries = true)
    public List<String> importFromExcel(MultipartFile file) throws IOException {
        List<String> errors = new ArrayList<>();

        List<Product> products = ExcelUtil.importExcel(file.getInputStream(), 1, (row, rowNum) -> {
            try {
                Product product = new Product();

                // 商品编号（必填）
                String code = ExcelUtil.getCellValue(row.getCell(0));
                if (code == null || code.trim().isEmpty()) {
                    errors.add("第" + (rowNum + 1) + "行：商品编号不能为空");
                    return null;
                }
                product.setCode(code.trim());

                // 商品名称（必填）
                String name = ExcelUtil.getCellValue(row.getCell(1));
                if (name == null || name.trim().isEmpty()) {
                    errors.add("第" + (rowNum + 1) + "行：商品名称不能为空");
                    return null;
                }
                product.setName(name.trim());

                // 分类名称
                String categoryName = ExcelUtil.getCellValue(row.getCell(2));
                if (categoryName != null && !categoryName.trim().isEmpty()) {
                    Category category = categoryRepository.findByName(categoryName.trim());
                    if (category != null) {
                        product.setCategory(category);
                    }
                }

                // 供应商名称
                String supplierName = ExcelUtil.getCellValue(row.getCell(3));
                if (supplierName != null && !supplierName.trim().isEmpty()) {
                    Optional<Supplier> supplierOpt = supplierRepository.findByName(supplierName.trim());
                    if (supplierOpt.isPresent()) {
                        product.setSupplier(supplierOpt.get());
                    }
                }

                // 单价
                String priceStr = ExcelUtil.getCellValue(row.getCell(4));
                if (priceStr != null && !priceStr.trim().isEmpty()) {
                    try {
                        product.setPrice(new BigDecimal(priceStr.trim()));
                    } catch (NumberFormatException e) {
                        product.setPrice(BigDecimal.ZERO);
                    }
                }

                // 最低库存
                String minStockStr = ExcelUtil.getCellValue(row.getCell(5));
                if (minStockStr != null && !minStockStr.trim().isEmpty()) {
                    try {
                        product.setMinStock(Integer.parseInt(minStockStr.trim().split("\\.")[0]));
                    } catch (NumberFormatException e) {
                        product.setMinStock(0);
                    }
                }

                // 单位
                String unit = ExcelUtil.getCellValue(row.getCell(6));
                if (unit != null && !unit.trim().isEmpty()) {
                    product.setUnit(unit.trim());
                }

                // 条形码
                String barcode = ExcelUtil.getCellValue(row.getCell(7));
                if (barcode != null && !barcode.trim().isEmpty()) {
                    product.setBarcode(barcode.trim());
                }

                // 说明
                String description = ExcelUtil.getCellValue(row.getCell(8));
                if (description != null && !description.trim().isEmpty()) {
                    product.setDescription(description.trim());
                }

                return product;
            } catch (Exception e) {
                errors.add("第" + (rowNum + 1) + "行：数据格式错误 - " + e.getMessage());
                return null;
            }
        });

        // 保存商品数据
        for (Product product : products) {
            try {
                if (productRepository.existsByCode(product.getCode())) {
                    errors.add("商品编号 " + product.getCode() + " 已存在，跳过");
                } else {
                    productRepository.save(product);
                }
            } catch (Exception e) {
                errors.add("商品 " + product.getCode() + " 保存失败：" + e.getMessage());
            }
        }

        return errors;
    }

    /**
     * 上传商品图片
     */
    @Transactional
    @CacheEvict(value = {"products", "lowStockProducts"}, allEntries = true)
    public String uploadProductImage(Integer productId, MultipartFile file) throws IOException {
        // 检查商品是否存在
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 创建上传目录
        Path uploadPath = Paths.get(PRODUCT_UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = "product_" + productId + "_" + UUID.randomUUID().toString() + extension;

        // 保存文件
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 删除旧图片文件（如果存在）
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            String oldFilename = product.getImageUrl().substring(product.getImageUrl().lastIndexOf("/") + 1);
            Path oldFilePath = uploadPath.resolve(oldFilename);
            try {
                Files.deleteIfExists(oldFilePath);
            } catch (IOException e) {
                // 记录日志但不影响新图片上传
                System.err.println("删除旧图片失败: " + e.getMessage());
            }
        }

        // 使用原生SQL更新图片URL，避免乐观锁版本号变化
        String imageUrl = "/api/files/products/" + filename;
        productRepository.updateImageUrl(productId, imageUrl);

        logger.info("商品图片更新成功：商品ID={}, 图片URL={}", productId, imageUrl);

        return imageUrl;
    }

    /**
     * 上传商品图片并返回更新后的商品信息（包含最新版本号）
     */
    @Transactional
    @CacheEvict(value = {"products", "lowStockProducts"}, allEntries = true)
    public Product uploadProductImageWithVersion(Integer productId, MultipartFile file) throws IOException {
        String imageUrl = uploadProductImage(productId, file);

        // 获取商品基本信息（包含版本号）
        Product product = productRepository.findActiveById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 手动设置imageUrl，确保返回正确的值
        product.setImageUrl(imageUrl);

        logger.info("商品图片上传并查询完成：商品ID={}, 图片URL={}, 版本号={}",
            productId, imageUrl, product.getVersion());

        return product;
    }

    /**
     * 删除商品图片
     */
    @Transactional
    @CacheEvict(value = {"products", "lowStockProducts"}, allEntries = true)
    public void deleteProductImage(Integer productId) throws IOException {
        // 检查商品是否存在并获取图片URL
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在"));

        String imageUrl = product.getImageUrl();

        // 删除图片文件
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(PRODUCT_UPLOAD_DIR + filename);
            Files.deleteIfExists(filePath);
        }

        // 使用原生SQL清空图片URL，避免乐观锁版本号变化
        productRepository.updateImageUrl(productId, null);

        logger.info("商品图片删除成功：商品ID={}", productId);
    }

    /**
     * 删除商品图片并返回更新后的商品信息（包含最新版本号）
     */
    @Transactional
    @CacheEvict(value = {"products", "lowStockProducts"}, allEntries = true)
    public Product deleteProductImageWithVersion(Integer productId) throws IOException {
        deleteProductImage(productId);

        // 获取商品基本信息（包含版本号）
        Product product = productRepository.findActiveById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 手动设置imageUrl为null，确保返回正确的值
        product.setImageUrl(null);

        logger.info("商品图片删除并查询完成：商品ID={}, 版本号={}", productId, product.getVersion());

        return product;
    }
}

