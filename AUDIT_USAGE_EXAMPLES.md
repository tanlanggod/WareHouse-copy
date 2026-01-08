# 审计功能使用示例

## 概述

数据库优化完成后，所有实体类都支持完整的审计功能，包括：
- 自动填充创建人、创建时间、修改人、修改时间
- 逻辑删除功能
- 数据变更追踪
- 乐观锁版本控制（部分实体）

## 基本使用方法

### 1. Repository使用示例

```java
@Service
public class ProductService extends BaseService<Product, Integer> {

    @Autowired
    private ProductRepository productRepository;

    @Override
    protected BaseRepository<Product, Integer> getRepository() {
        return productRepository;
    }

    // 创建商品（自动填充审计字段）
    public Product createProduct(Product product) {
        // 自动设置 creatorId, createdAt, updaterId, updatedAt
        return create(product);
    }

    // 更新商品（自动更新审计字段）
    public Product updateProduct(Product product) {
        // 自动设置 updaterId, updatedAt
        return update(product);
    }

    // 逻辑删除商品
    public void deleteProduct(Integer productId) {
        // 自动设置 isDeleted=1, deletedAt, deleterId
        softDelete(productId);
    }

    // 查找未删除的商品
    public List<Product> findActiveProducts() {
        return findAll();
    }

    // 查找已删除的商品
    public List<Product> findDeletedProducts() {
        return findAllDeleted();
    }

    // 恢复删除的商品
    public void restoreProduct(Integer productId) {
        restore(productId);
    }

    // 根据创建人查找商品
    public List<Product> findByCreator(Integer creatorId) {
        return productRepository.findByCreatorId(creatorId);
    }
}
```

### 2. 自定义查询示例

```java
@Repository
public interface ProductRepository extends BaseRepository<Product, Integer> {

    // 自定义查询需要添加逻辑删除条件
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isDeleted = 0")
    List<Product> findByCategoryIdAndActive(@Param("categoryId") Integer categoryId);

    // 根据状态查找未删除的商品
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.isDeleted = 0")
    List<Product> findByStatusAndActive(@Param("status") Integer status);

    // 根据库存范围查找商品
    @Query("SELECT p FROM Product p WHERE p.stockQty BETWEEN :minQty AND :maxQty AND p.isDeleted = 0")
    List<Product> findByStockQuantityRange(@Param("minQty") Integer minQty,
                                          @Param("maxQty") Integer maxQty);

    // 复杂查询示例：根据供应商、类别、状态查找
    @Query("SELECT p FROM Product p WHERE p.supplier.id = :supplierId " +
           "AND p.category.id = :categoryId AND p.status = :status AND p.isDeleted = 0")
    List<Product> findBySupplierAndCategoryAndStatus(@Param("supplierId") Integer supplierId,
                                                   @Param("categoryId") Integer categoryId,
                                                   @Param("status") Integer status);
}
```

### 3. Controller使用示例

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        // 在Controller层设置当前用户上下文
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            Product savedProduct = productService.createProduct(product);
            return ResponseEntity.ok(savedProduct);
        } finally {
            UserContext.clear();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer id,
                                               @RequestBody Product product) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            product.setId(id);
            Product updatedProduct = productService.update(product);
            return ResponseEntity.ok(updatedProduct);
        } finally {
            UserContext.clear();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            productService.softDelete(id);
            return ResponseEntity.ok().build();
        } finally {
            UserContext.clear();
        }
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restoreProduct(@PathVariable Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            productService.restore(id);
            return ResponseEntity.ok().build();
        } finally {
            UserContext.clear();
        }
    }

    @GetMapping
    public ResponseEntity<List<Product>> getActiveProducts() {
        List<Product> products = productService.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<Product>> getDeletedProducts() {
        List<Product> products = productService.findAllDeleted();
        return ResponseEntity.ok(products);
    }

    private Integer getCurrentUserId() {
        // 从Spring Security获取当前用户ID
        // 这里需要根据你的具体认证实现来调整
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // 假设用户ID存储在principal中
            return ((User) authentication.getPrincipal()).getId();
        }
        return 1; // 默认系统用户
    }
}
```

### 4. 乐观锁使用示例

```java
@Service
public class ProductService extends BaseService<Product, Integer> {

    @Transactional
    public Product updateProductWithVersionCheck(Product product) {
        Product existingProduct = findById(product.getId())
            .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 检查版本号
        if (!existingProduct.getVersion().equals(product.getVersion())) {
            throw new OptimisticLockingFailureException(
                "商品已被其他用户修改，请刷新后重试");
        }

        // 更新商品信息
        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        // ... 其他字段

        // 保存时会自动检查版本号
        return update(existingProduct);
    }
}
```

### 5. 批量操作示例

```java
@Service
public class ProductService extends BaseService<Product, Integer> {

    // 批量创建商品
    @Transactional
    public List<Product> batchCreateProducts(List<Product> products) {
        UserContext.setCurrentUserId(getCurrentUserId());
        try {
            return products.stream()
                .map(this::create)
                .collect(Collectors.toList());
        } finally {
            UserContext.clear();
        }
    }

    // 批量删除商品
    @Transactional
    public void batchDeleteProducts(List<Integer> productIds) {
        UserContext.setCurrentUserId(getCurrentUserId());
        try {
            softDeleteAll(productIds);
        } finally {
            UserContext.clear();
        }
    }

    // 批量恢复商品
    @Transactional
    public void batchRestoreProducts(List<Integer> productIds) {
        UserContext.setCurrentUserId(getCurrentUserId());
        try {
            restoreAll(productIds);
        } finally {
            UserContext.clear();
        }
    }
}
```

## 数据库查询示例

### 1. 基本查询

```sql
-- 查找未删除的商品
SELECT * FROM product WHERE is_deleted = 0;

-- 查找已删除的商品
SELECT * FROM product WHERE is_deleted = 1;

-- 根据创建人查找商品
SELECT * FROM product WHERE creator_id = 1 AND is_deleted = 0;

-- 根据修改时间范围查找商品
SELECT * FROM product
WHERE updated_at BETWEEN '2024-01-01' AND '2024-12-31'
AND is_deleted = 0;
```

### 2. 复杂查询

```sql
-- 查找特定供应商的所有有效商品
SELECT p.*, s.name as supplier_name
FROM product p
JOIN supplier s ON p.supplier_id = s.id
WHERE p.is_deleted = 0 AND s.is_deleted = 0 AND p.supplier_id = 1;

-- 统计各分类的商品数量（只统计未删除的）
SELECT c.name, COUNT(p.id) as product_count
FROM category c
LEFT JOIN product p ON c.id = p.category_id AND p.is_deleted = 0
WHERE c.is_deleted = 0
GROUP BY c.id, c.name;

-- 查找库存低于安全水平的商品
SELECT p.name, p.stock_qty, p.min_stock
FROM product p
WHERE p.is_deleted = 0
AND p.stock_qty <= p.min_stock
AND p.status = 1;
```

## 测试用例示例

### 1. 单元测试

```java
@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Test
    void testCreateProduct() {
        // 设置当前用户
        UserContext.setCurrentUserId(1);

        Product product = new Product();
        product.setCode("TEST001");
        product.setName("测试商品");
        product.setPrice(new BigDecimal("100.00"));

        Product savedProduct = productService.create(product);

        assertNotNull(savedProduct.getId());
        assertEquals(1, savedProduct.getCreatorId());
        assertEquals(1, savedProduct.getUpdaterId());
        assertNotNull(savedProduct.getCreatedAt());
        assertNotNull(savedProduct.getUpdatedAt());
        assertEquals(0, savedProduct.getIsDeleted());

        UserContext.clear();
    }

    @Test
    void testSoftDelete() {
        UserContext.setCurrentUserId(1);

        // 先创建商品
        Product product = createTestProduct();
        Product savedProduct = productService.create(product);

        // 逻辑删除
        productService.softDelete(savedProduct.getId());

        // 验证删除状态
        Optional<Product> deletedProduct = productService.findById(savedProduct.getId());
        assertFalse(deletedProduct.isPresent());

        // 验证可以通过查找已删除记录找到
        List<Product> deletedProducts = productService.findAllDeleted();
        assertEquals(1, deletedProducts.size());

        UserContext.clear();
    }

    @Test
    void testRestore() {
        UserContext.setCurrentUserId(1);

        // 创建并删除商品
        Product product = createTestProduct();
        Product savedProduct = productService.create(product);
        productService.softDelete(savedProduct.getId());

        // 恢复商品
        productService.restore(savedProduct.getId());

        // 验证恢复成功
        Optional<Product> restoredProduct = productService.findById(savedProduct.getId());
        assertTrue(restoredProduct.isPresent());
        assertEquals(0, restoredProduct.get().getIsDeleted());

        UserContext.clear();
    }

    private Product createTestProduct() {
        Product product = new Product();
        product.setCode("TEST001");
        product.setName("测试商品");
        product.setPrice(new BigDecimal("100.00"));
        return product;
    }
}
```

## 注意事项

### 1. 用户上下文管理

- 确保在每次请求开始时设置当前用户ID
- 在请求结束时清除用户上下文（可以使用拦截器或过滤器）
- 对于异步任务，需要在任务开始时设置用户上下文

### 2. 查询优化

- 所有查询都应该包含 `is_deleted = 0` 条件
- 使用复合索引优化常用查询
- 考虑使用视图简化复杂查询

### 3. 数据迁移

- 现有数据的 `is_deleted` 字段默认为 0（未删除）
- 现有数据的 `created_at` 和 `updated_at` 需要设置合适的值
- 考虑为历史数据设置合理的创建人ID

### 4. 性能考虑

- 定期清理过期的已删除数据
- 监控索引使用情况
- 考虑对大表进行分区

通过以上示例和说明，您可以充分利用新的审计功能来提升系统的数据完整性和可追溯性。