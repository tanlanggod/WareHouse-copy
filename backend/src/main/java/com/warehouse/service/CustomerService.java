package com.warehouse.service;

import com.warehouse.common.Result;
import com.warehouse.entity.Customer;
import com.warehouse.repository.BaseRepository;
import com.warehouse.repository.CustomerRepository;
import com.warehouse.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 客户服务
 */
@Service
@Transactional
public class CustomerService extends BaseService<Customer, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    protected BaseRepository<Customer, Integer> getRepository() {
        return customerRepository;
    }

    protected Integer getCurrentUserId() {
        try {
            return UserContext.getCurrentUserId();
        } catch (Exception e) {
            return 1; // 系统用户ID
        }
    }

    /**
     * 获取所有客户
     */
    @Cacheable(value = "customers", key = "'all'")
    public Result<List<Customer>> getAllCustomers() {
        try {
            List<Customer> customers = findAll();
            return Result.success("获取客户列表成功", customers);
        } catch (Exception e) {
            logger.error("获取客户列表失败: {}", e.getMessage(), e);
            return Result.error("获取客户列表失败：" + e.getMessage());
        }
    }

    /**
     * 分页获取客户
     */
    public Result<Page<Customer>> getCustomers(Pageable pageable) {
        try {
            Page<Customer> customers = findAll(pageable);
            return Result.success("获取客户列表成功", customers);
        } catch (Exception e) {
            logger.error("获取客户列表失败: {}", e.getMessage(), e);
            return Result.error("获取客户列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取客户
     */
    @Cacheable(value = "customer", key = "#id")
    public Result<Customer> getCustomerById(Integer id) {
        try {
            Optional<Customer> customer = findActiveById(id);
            if (customer.isPresent()) {
                return Result.success("获取客户信息成功", customer.get());
            } else {
                return Result.error("客户不存在");
            }
        } catch (Exception e) {
            logger.error("获取客户信息失败: {}", e.getMessage(), e);
            return Result.error("获取客户信息失败：" + e.getMessage());
        }
    }

    /**
     * 创建客户
     */
    @CacheEvict(value = {"customers", "customer"}, allEntries = true)
    public Result<Customer> createCustomer(Customer customer) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            // 验证客户名称唯一性
            if (customerRepository.findActiveByName(customer.getName()).isPresent()) {
                return Result.error("客户名称已存在");
            }

            // 验证必填字段
            if (!StringUtils.hasText(customer.getName())) {
                return Result.error("客户名称不能为空");
            }

            // 设置默认状态
            if (customer.getStatus() == null) {
                customer.setStatus(1);
            }

            Customer savedCustomer = create(customer);
            logger.info("创建客户成功，ID: {}, 名称: {}", savedCustomer.getId(), savedCustomer.getName());

            return Result.success("创建客户成功", savedCustomer);
        } catch (Exception e) {
            logger.error("创建客户失败: {}", e.getMessage(), e);
            return Result.error("创建客户失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 更新客户
     */
    @CacheEvict(value = {"customers", "customer"}, allEntries = true)
    public Result<Customer> updateCustomer(Integer id, Customer customer) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            Optional<Customer> existingCustomer = findActiveById(id);
            if (!existingCustomer.isPresent()) {
                return Result.error("客户不存在");
            }

            Customer customerToUpdate = existingCustomer.get();

            // 验证客户名称唯一性（排除当前客户）
            if (!customer.getName().equals(customerToUpdate.getName()) &&
                customerRepository.findActiveByName(customer.getName()).isPresent()) {
                return Result.error("客户名称已存在");
            }

            // 验证必填字段
            if (!StringUtils.hasText(customer.getName())) {
                return Result.error("客户名称不能为空");
            }

            // 更新字段
            customerToUpdate.setName(customer.getName());
            customerToUpdate.setContactPerson(customer.getContactPerson());
            customerToUpdate.setPhone(customer.getPhone());
            customerToUpdate.setAddress(customer.getAddress());
            customerToUpdate.setEmail(customer.getEmail());
            customerToUpdate.setStatus(customer.getStatus());

            Customer savedCustomer = update(customerToUpdate);
            logger.info("更新客户成功，ID: {}, 名称: {}", savedCustomer.getId(), savedCustomer.getName());

            return Result.success("更新客户成功", savedCustomer);
        } catch (Exception e) {
            logger.error("更新客户失败: {}", e.getMessage(), e);
            return Result.error("更新客户失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 删除客户
     */
    @CacheEvict(value = {"customers", "customer"}, allEntries = true)
    public Result<String> deleteCustomer(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            Optional<Customer> customer = findActiveById(id);
            if (!customer.isPresent()) {
                return Result.error("客户不存在");
            }

            // TODO: 检查是否有关联的出库记录
            // 这里可以添加业务逻辑，检查客户是否有关联的出库记录

            softDelete(id);
            logger.info("删除客户成功，ID: {}", id);

            return Result.success("删除客户成功", "删除成功");
        } catch (Exception e) {
            logger.error("删除客户失败: {}", e.getMessage(), e);
            return Result.error("删除客户失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 恢复删除的客户
     */
    @CacheEvict(value = {"customers", "customer"}, allEntries = true)
    public Result<String> restoreCustomer(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            restore(id);
            logger.info("恢复客户成功，ID: {}", id);

            return Result.success("恢复客户成功", "恢复成功");
        } catch (Exception e) {
            logger.error("恢复客户失败: {}", e.getMessage(), e);
            return Result.error("恢复客户失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 获取已删除的客户
     */
    public Result<List<Customer>> getDeletedCustomers() {
        try {
            List<Customer> deletedCustomers = findAllDeleted();
            return Result.success("获取已删除客户列表成功", deletedCustomers);
        } catch (Exception e) {
            logger.error("获取已删除客户列表失败: {}", e.getMessage(), e);
            return Result.error("获取已删除客户列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据状态获取客户
     */
    public Result<List<Customer>> getCustomersByStatus(Integer status) {
        try {
            List<Customer> customers = customerRepository.findActiveByStatus(status);
            return Result.success("获取客户列表成功", customers);
        } catch (Exception e) {
            logger.error("获取客户列表失败: {}", e.getMessage(), e);
            return Result.error("获取客户列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据名称搜索客户
     */
    public Result<List<Customer>> searchCustomers(String keyword) {
        try {
            List<Customer> customers = customerRepository.findActiveByNameContaining(keyword);
            return Result.success("搜索客户成功", customers);
        } catch (Exception e) {
            logger.error("搜索客户失败: {}", e.getMessage(), e);
            return Result.error("搜索客户失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除客户
     */
    @Transactional
    public Result<String> batchDeleteCustomers(List<Integer> ids) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            // TODO: 检查是否有关联的出库记录
            softDeleteAll(ids);
            logger.info("批量删除客户成功，ID数量: {}", ids.size());

            return Result.success("批量删除客户成功", "删除成功");
        } catch (Exception e) {
            logger.error("批量删除客户失败: {}", e.getMessage(), e);
            return Result.error("批量删除客户失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 批量恢复客户
     */
    @Transactional
    public Result<String> batchRestoreCustomers(List<Integer> ids) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            restoreAll(ids);
            logger.info("批量恢复客户成功，ID数量: {}", ids.size());

            return Result.success("批量恢复客户成功", "恢复成功");
        } catch (Exception e) {
            logger.error("批量恢复客户失败: {}", e.getMessage(), e);
            return Result.error("批量恢复客户失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }
}