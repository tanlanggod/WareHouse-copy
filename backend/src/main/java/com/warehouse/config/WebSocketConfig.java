package com.warehouse.config;

import com.warehouse.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.io.IOException;
import java.net.URI;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketService webSocketService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ApprovalWebSocketHandler(), "/ws/approval")
                .setAllowedOrigins("*");
    }

    /**
     * 审批WebSocket处理器
     */
    public class ApprovalWebSocketHandler implements WebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            String query = session.getUri().getQuery();
            Integer userId = extractUserIdFromQuery(query);

            if (userId != null) {
                webSocketService.addUserSession(userId, session);
                log.info("WebSocket连接建立：用户ID={}, SessionID={}", userId, session.getId());
            } else {
                log.warn("WebSocket连接缺少用户ID参数：{}", session.getId());
                session.close();
            }
        }

        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
            // 处理客户端发送的消息（如心跳包）
            log.debug("收到WebSocket消息：SessionID={}, Message={}", session.getId(), message.getPayload());
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            log.error("WebSocket传输错误：SessionID={}, Error={}", session.getId(), exception.getMessage());
            webSocketService.removeUserSession(session);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
            log.info("WebSocket连接关闭：SessionID={}, Status={}", session.getId(), closeStatus);
            webSocketService.removeUserSession(session);
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }

        /**
         * 从查询参数中提取用户ID
         */
        private Integer extractUserIdFromQuery(String query) {
            if (query == null || query.isEmpty()) {
                return null;
            }

            for (String param : query.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && "userId".equals(keyValue[0])) {
                    try {
                        return Integer.parseInt(keyValue[1]);
                    } catch (NumberFormatException e) {
                        log.error("解析用户ID失败：{}", keyValue[1]);
                        return null;
                    }
                }
            }
            return null;
        }
    }
}