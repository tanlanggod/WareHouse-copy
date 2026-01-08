package com.warehouse.service;

import com.warehouse.common.PageResult;
import com.warehouse.controller.UserController;
import com.warehouse.dto.PasswordResetRequest;
import com.warehouse.dto.UserCreateRequest;
import com.warehouse.dto.UserListRequest;
import com.warehouse.dto.UserQueryDTO;
import com.warehouse.entity.User;
import com.warehouse.enums.UserRole;
import com.warehouse.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户服务类
 */
@Service
public class UserService {

    // 常量定义
    private static final List<String> ALLOWED_SORT_FIELDS = Arrays.asList(
        "id", "username", "realName", "role", "status", "createdAt", "updatedAt"
    );
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String DEFAULT_SORT_FIELD = "createdAt";
    private static final String DEFAULT_SORT_DIRECTION = "desc";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String AVATAR_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/avatars/";

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            String username = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    /**
     * 更新当前用户信息
     */
    @Transactional
    public User updateCurrentUser(User userInfo) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        // 更新允许修改的字段
        if (userInfo.getRealName() != null) {
            currentUser.setRealName(userInfo.getRealName());
        }
        if (userInfo.getEmail() != null) {
            currentUser.setEmail(userInfo.getEmail());
        }
        if (userInfo.getPhone() != null && !userInfo.getPhone().isEmpty()) {
            // 检查新手机号是否已被其他用户使用
            Optional<User> existingUser = userRepository.findByPhone(userInfo.getPhone());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(currentUser.getId())) {
                throw new RuntimeException("手机号已被其他用户使用");
            }
            currentUser.setPhone(userInfo.getPhone());
        }

        return userRepository.save(currentUser);
    }

    /**
     * 上传用户头像
     */
    public String uploadAvatar(MultipartFile file) throws IOException {
        // 创建上传目录
        Path uploadPath = Paths.get(AVATAR_UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;

        // 保存文件
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 更新用户头像URL
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            currentUser.setAvatar("/api/files/avatars/" + filename);
            userRepository.save(currentUser);
        }

        return currentUser.getAvatar();
    }

    
    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 更新新密码
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);
    }

    /**
     * 根据ID获取用户信息
     */
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 更新用户信息 (管理员功能)
     */
    @Transactional
    public User updateUser(Integer id, User userInfo) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 更新允许修改的字段
        if (userInfo.getRealName() != null) {
            user.setRealName(userInfo.getRealName());
        }
        if (userInfo.getEmail() != null) {
            user.setEmail(userInfo.getEmail());
        }
        if (userInfo.getPhone() != null && !userInfo.getPhone().isEmpty()) {
            // 检查新手机号是否已被其他用户使用
            Optional<User> existingUser = userRepository.findByPhone(userInfo.getPhone());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw new RuntimeException("手机号已被其他用户使用");
            }
            user.setPhone(userInfo.getPhone());
        }
        if (userInfo.getRole() != null) {
            user.setRole(userInfo.getRole());
        }
        if (userInfo.getStatus() != null) {
            user.setStatus(userInfo.getStatus());
        }

        return userRepository.save(user);
    }

    /**
     * 分页查询用户列表（管理员功能）
     */
    @Transactional(readOnly = true)
    public PageResult<UserQueryDTO> getUsers(UserListRequest request) {
        logger.info("开始查询用户列表，参数: {}", request);

        // 1. 权限验证
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            throw new SecurityException("无权限访问用户列表");
        }

        // 2. 参数校验和默认值设置
        if (request == null) {
            request = new UserListRequest();
        }
        if (request.getPage() == null || request.getPage() < 1) {
            request.setPage(1);
        }
        if (request.getSize() == null || request.getSize() < 1) {
            request.setSize(DEFAULT_PAGE_SIZE);
        }
        if (request.getSize() > MAX_PAGE_SIZE) {
            request.setSize(MAX_PAGE_SIZE);
            logger.warn("分页大小超过限制，已调整为最大值: {}", MAX_PAGE_SIZE);
        }

        // 3. 构建查询条件
        Specification<User> specification = buildUserSpecification(request);

        // 4. 验证和设置排序参数
        String sortBy = validateSortField(request.getSortBy());
        String sortDirection = validateSortDirection(request.getSortDirection());

        // 5. 构建排序
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);

        // 6. 创建分页请求
        Pageable pageable = PageRequest.of(
            request.getPage() - 1, // Spring Data JPA页码从0开始
            request.getSize(),
            sort
        );

        try {
            // 7. 执行查询
            Page<User> userPage = userRepository.findAll(specification, pageable);

            // 8. 转换为DTO，防止敏感信息泄露
            List<UserQueryDTO> userDTOs = userPage.getContent().stream()
                .map(UserQueryDTO::fromEntity)
                .collect(Collectors.toList());

            // 9. 构建返回结果
            PageResult<UserQueryDTO> result = new PageResult<>(
                userDTOs,
                userPage.getTotalElements(),
                request.getPage(),
                request.getSize(),
                userPage.getTotalPages()
            );

            logger.info("用户列表查询完成，共{}条记录，当前第{}页",
                result.getTotal(), result.getCurrentPage());

            return result;

        } catch (Exception e) {
            logger.error("查询用户列表失败", e);
            throw new RuntimeException("查询用户列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建用户查询条件
     */
    private Specification<User> buildUserSpecification(UserListRequest request) {
        return (root, query, cb) -> {
            List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // 关键词搜索（用户名、真实姓名）
            if (StringUtils.hasText(request.getKeyword())) {
                String keyword = "%" + request.getKeyword().trim() + "%";
                javax.persistence.criteria.Predicate usernameLike = cb.like(root.get("username"), keyword);
                javax.persistence.criteria.Predicate realNameLike = cb.like(root.get("realName"), keyword);
                predicates.add(cb.or(usernameLike, realNameLike));
            }

            // 角色筛选
            if (request.getRole() != null) {
                predicates.add(cb.equal(root.get("role"), request.getRole()));
            }

            // 状态筛选
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            return cb.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
    }

    /**
     * 验证排序字段
     */
    private String validateSortField(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return DEFAULT_SORT_FIELD;
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            logger.warn("非法的排序字段: {}, 使用默认字段: {}", sortBy, DEFAULT_SORT_FIELD);
            return DEFAULT_SORT_FIELD;
        }

        return sortBy;
    }

    /**
     * 验证排序方向
     */
    private String validateSortDirection(String sortDirection) {
        if (!StringUtils.hasText(sortDirection)) {
            return DEFAULT_SORT_DIRECTION;
        }

        if (!sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc")) {
            logger.warn("非法的排序方向: {}, 使用默认方向: {}", sortDirection, DEFAULT_SORT_DIRECTION);
            return DEFAULT_SORT_DIRECTION;
        }

        return sortDirection.toLowerCase();
    }

    /**
     * 创建用户（管理员功能）
     */
    @Transactional
    public User createUser(UserCreateRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在（如果提供了邮箱）
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("邮箱已被使用");
            }
        }

        // 检查手机号是否已存在（如果提供了手机号）
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("手机号已被使用");
            }
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * 重置用户密码
     */
    @Transactional
    public String resetUserPassword(Integer userId, PasswordResetRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 生成随机密码（如果没有提供新密码）
        String newPassword;
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            newPassword = request.getNewPassword();
        } else {
            newPassword = generateRandomPassword();
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return newPassword;
    }

    /**
     * 切换用户状态
     */
    @Transactional
    public User toggleUserStatus(Integer userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 切换状态
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * 删除用户（软删除）
     */
    @Transactional
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 软删除：设置状态为禁用
        user.setStatus(0);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();

        // 确保至少包含一个大写字母、小写字母和数字
        password.append(chars.charAt((int) (Math.random() * 26))); // 大写字母
        password.append(chars.charAt((int) (Math.random() * 26) + 26)); // 小写字母
        password.append(chars.charAt((int) (Math.random() * 10) + 52)); // 数字

        // 剩余字符随机选择
        for (int i = 3; i < 8; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }

        // 打乱字符顺序
        return shuffleString(password.toString());
    }

    /**
     * 打乱字符串
     */
    private String shuffleString(String str) {
        char[] chars = str.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    /**
     * 获取当前用户的角色
     */
    public String getCurrentUserRole() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName(); // 或者从用户信息中获取角色
            }
            return null;
        } catch (Exception e) {
            logger.error("获取当前用户角色失败", e);
            return null;
        }
    }
}