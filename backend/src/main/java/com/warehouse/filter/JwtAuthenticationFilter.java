package com.warehouse.filter;

import com.warehouse.service.UserDetailsServiceImpl;
import com.warehouse.util.JwtUtil;
import com.warehouse.util.UserContext;
import com.warehouse.entity.User;
import com.warehouse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 认证过滤器，从请求头解析 Token 并注入安全上下文。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        logger.debug("JWT认证过滤器 - 请求: {} {}", method, requestURI);

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            logger.debug("发现JWT token，长度: {}", token.length());

            try {
                username = jwtUtil.getUsernameFromToken(token);
                logger.debug("从token中提取用户名: {}", username);
            } catch (Exception e) {
                logger.error("无法从token中获取用户名: {}", e.getMessage());
                logger.debug("Token内容前20个字符: {}", token.length() > 20 ? token.substring(0, 20) : token);
            }
        } else {
            logger.debug("未发现有效的Authorization header");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                logger.debug("加载用户详情: {}", username);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                boolean tokenValid = jwtUtil.validateToken(token);
                logger.debug("Token验证结果: {}, 用户状态: {}, 权限: {}",
                    tokenValid,
                    userDetails.isEnabled(),
                    userDetails.getAuthorities());

                if (tokenValid && userDetails.isEnabled()) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    // 设置UserContext
                    User user = userRepository.findByUsername(username)
                            .orElse(null);
                    if (user != null) {
                        UserContext.setCurrentUserId(user.getId());
                        logger.debug("已设置用户上下文，用户ID: {}", user.getId());
                    }

                    logger.info("用户认证成功: {} - {} {}", username, method, requestURI);
                } else if (!tokenValid) {
                    logger.warn("Token无效或已过期: {} - {} {}", username, method, requestURI);
                } else if (!userDetails.isEnabled()) {
                    logger.warn("用户已被禁用: {} - {} {}", username, method, requestURI);
                }
            } catch (Exception e) {
                logger.error("用户认证过程中发生错误: {}", e.getMessage(), e);
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // 仅对认证相关的API请求清除UserContext，避免影响业务API调用
            if (requestURI.startsWith("/api/") &&
                (requestURI.contains("/users/current") ||
                 requestURI.contains("/approvals/"))) {
                UserContext.clear();
            }
        }
    }
}

