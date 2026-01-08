# Service类更新指南

以下是如何更新Service类以支持审计功能的详细示例。

## SupplierService 更新示例

### 更新前（原始版本）
```java
@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier createSupplier(Supplier supplier) {
        if (supplierRepository.existsByName(supplier.getName())) {
            throw new RuntimeException("供应商名称已存在");
        }
        return supplierRepository.save(supplier);
    }

    public Supplier updateSupplier(Integer id, Supplier supplier) {
        Supplier existing = supplierRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("供应商不存在"));

        existing.setName(supplier.getName());
        existing.setContactPerson(supplier.getContactPerson());
        existing.setPhone(supplier.getPhone());
        existing.setAddress(supplier.getAddress());
        existing.setEmail(supplier.getEmail());

        return supplierRepository.save(existing);
    }

    public void deleteSupplier(Integer id) {
        supplierRepository.deleteById(id);
    }
}
```

### 更新后（支持审计功能）
```java
@Service
public class SupplierService extends BaseService<Supplier, Integer> {

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    protected BaseRepository<Supplier, Integer> getRepository() {
        return supplierRepository;
    }

    private Integer getCurrentUserId() {
        try {
            return UserContext.getCurrentUserId();
        } catch (Exception e) {
            return 1; // 系统用户ID
        }
    }

    // 获取所有有效供应商
    public List<Supplier> getAllSuppliers() {
        return findAll(); // 使用BaseService的findAll方法，自动过滤逻辑删除
    }

    // 分页获取有效供应商
    public Page<Supplier> getSuppliers(Pageable pageable) {
        return findAll(pageable); // 使用BaseService的分页方法
    }

    // 创建供应商（自动设置审计字段）
    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            if (supplierRepository.existsActiveByName(supplier.getName())) {
                throw new RuntimeException("供应商名称已存在");
            }
            return create(supplier); // 使用BaseService的create方法，自动填充审计字段
        } finally {
            UserContext.clear();
        }
    }

    // 更新供应商（自动设置审计字段）
    @Transactional
    public Supplier updateSupplier(Integer id, Supplier supplier) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            Supplier existing = findActiveById(id) // 使用BaseService的findActiveById方法
                .orElseThrow(() -> new RuntimeException("供应商不存在"));

            if (!existing.getName().equals(supplier.getName()) &&
                supplierRepository.existsActiveByName(supplier.getName())) {
                throw new RuntimeException("供应商名称已存在");
            }

            existing.setName(supplier.getName());
            existing.setContactPerson(supplier.getContactPerson());
            existing.setPhone(supplier.getPhone());
            existing.setAddress(supplier.getAddress());
            existing.setEmail(supplier.getEmail());
            existing.setStatus(supplier.getStatus());

            return update(existing); // 使用BaseService的update方法，自动更新审计字段
        } finally {
            UserContext.clear();
        }
    }

    // 逻辑删除供应商
    @Transactional
    public void deleteSupplier(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            softDelete(id); // 使用BaseService的逻辑删除方法
        } finally {
            UserContext.clear();
        }
    }

    // 恢复删除的供应商
    @Transactional
    public void restoreSupplier(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            restore(id); // 使用BaseService的恢复方法
        } finally {
            UserContext.clear();
        }
    }

    // 获取已删除的供应商
    public List<Supplier> getDeletedSuppliers() {
        return findAllDeleted(); // 使用BaseService的findAllDeleted方法
    }

    // 根据状态查找供应商
    public List<Supplier> getSuppliersByStatus(Integer status) {
        return supplierRepository.findActiveByStatus(status);
    }

    // 搜索供应商
    public List<Supplier> searchSuppliers(String keyword) {
        return supplierRepository.findActiveByKeyword(keyword);
    }

    // 批量删除供应商
    @Transactional
    public void batchDeleteSuppliers(List<Integer> ids) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            softDeleteAll(ids); // 使用BaseService的批量逻辑删除
        } finally {
            UserContext.clear();
        }
    }

    // 批量恢复供应商
    @Transactional
    public void batchRestoreSuppliers(List<Integer> ids) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            restoreAll(ids); // 使用BaseService的批量恢复
        } finally {
            UserContext.clear();
        }
    }

    // 根据创建人查找供应商
    public List<Supplier> getSuppliersByCreator(Integer creatorId) {
        return supplierRepository.findByCreatorId(creatorId);
    }
}
```

## 关键变更点说明

### 1. 继承BaseService
```java
// 从
public class SupplierService {

// 改为
public class SupplierService extends BaseService<Supplier, Integer> {
```

### 2. 实现getRepository方法
```java
@Override
protected BaseRepository<Supplier, Integer> getRepository() {
    return supplierRepository;
}
```

### 3. 用户上下文管理
```java
private Integer getCurrentUserId() {
    try {
        return UserContext.getCurrentUserId();
    } catch (Exception e) {
        return 1; // 系统用户ID
    }
}
```

### 4. 审计功能集成
```java
// 创建时自动填充审计字段
UserContext.setCurrentUserId(getCurrentUserId());
try {
    return create(supplier); // 自动设置creatorId, createdAt等
} finally {
    UserContext.clear();
}
```

### 5. 逻辑删除
```java
// 原来的物理删除
supplierRepository.deleteById(id);

// 改为逻辑删除
softDelete(id); // 自动设置isDeleted=1, deletedAt, deleterId
```

### 6. 查询方法更新
```java
// 原来
supplierRepository.findAll();

// 改为
findAll(); // 自动过滤逻辑删除的记录
```

## 需要更新的Service类列表

1. **CategoryService** - 继承BaseService<Category, Integer>
2. **SupplierService** - 继承BaseService<Supplier, Integer>
3. **CustomerService** - 继承BaseService<Customer, Integer>
4. **WarehouseService** - 继承BaseService<Warehouse, Integer>
5. **WarehouseLocationService** - 继承BaseService<WarehouseLocation, Integer>
6. **StockAdjustmentService** - 继承BaseService<StockAdjustment, Integer>
7. **SystemConfigService** - 继承BaseService<SystemConfig, Integer>

## 不需要更新的Service类

1. **ProductService** - 已经更新完成
2. **UserService** - 用户相关服务，可能需要特殊处理
3. **AuthService** - 认证服务，不涉及审计
4. **OperationLogService** - 日志服务，本身用于审计
5. **ReportService** - 报表服务，只读操作
6. **SmsService** - 短信服务，工具类
7. **WeChatService** - 微信服务，工具类
8. **CaptchaService** - 验证码服务，工具类

## 更新模式

对于每个需要更新的Service类，按照以下模式进行：

1. 继承对应的BaseService
2. 实现getRepository方法
3. 添加getCurrentUserId辅助方法
4. 在业务方法中设置用户上下文
5. 使用BaseService提供的CRUD方法
6. 更新查询方法使用Repository的Active方法
7. 添加逻辑删除和恢复功能

通过这种方式，您可以系统地更新所有Service类，使整个应用都支持完整的审计功能。