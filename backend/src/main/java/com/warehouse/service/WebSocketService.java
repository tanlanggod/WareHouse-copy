package com.warehouse.service;

import com.warehouse.entity.ApprovalRecord;
import com.warehouse.entity.User;
import com.warehouse.enums.UserRole;
import com.warehouse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.stream.Collectors;

/**
 * WebSocket服务类，用于实时推送审批通知
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final UserRepository userRepository;

    // 存储用户会话信息：userId -> WebSocketSession
    private static final java.util.Map<Integer, WebSocketSession> userSessions = new java.util.concurrent.ConcurrentHashMap<>();

    // 存储会话用户信息：sessionId -> userId
    private static final java.util.Map<String, Integer> sessionUsers = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 添加用户会话
     */
    public void addUserSession(Integer userId, WebSocketSession session) {
        userSessions.put(userId, session);
        sessionUsers.put(session.getId(), userId);
        log.info("用户 {} 建立WebSocket连接", userId);
    }

    /**
     * 移除用户会话
     */
    public void removeUserSession(WebSocketSession session) {
        Integer userId = sessionUsers.remove(session.getId());
        if (userId != null) {
            userSessions.remove(userId);
        }
        log.info("用户 {} 断开WebSocket连接", userId);
    }

    /**
     * 通知审批提交
     */
    public void notifyApprovalSubmission(ApprovalRecord approvalRecord) {
        String message = String.format(
                "{\"type\":\"approval_submission\",\"businessId\":%d,\"businessType\":\"%s\",\"submitter\":\"%s\",\"submitTime\":\"%s\"}",
                approvalRecord.getBusinessId(),
                approvalRecord.getBusinessType().getDescription(),
                approvalRecord.getSubmitter() != null ? approvalRecord.getSubmitter().getRealName() : "未知用户",
                approvalRecord.getSubmitTime()
        );

        // 发送给所有有审批权限的用户
        sendToApprovers(message);
    }

    /**
     * 通知审批结果
     */
    public void notifyApprovalResult(ApprovalRecord approvalRecord) {
        String message = String.format(
                "{\"type\":\"approval_result\",\"businessId\":%d,\"businessType\":\"%s\",\"approvalStatus\":\"%s\",\"approver\":\"%s\",\"approvalTime\":\"%s\",\"approvalRemark\":\"%s\"}",
                approvalRecord.getBusinessId(),
                approvalRecord.getBusinessType().getDescription(),
                approvalRecord.getApprovalStatus().getDescription(),
                approvalRecord.getApprover() != null ? approvalRecord.getApprover().getRealName() : "未知用户",
                approvalRecord.getApprovalTime(),
                approvalRecord.getApprovalRemark() != null ? approvalRecord.getApprovalRemark() : ""
        );

        // 发送给提交人
        sendToUser(approvalRecord.getSubmitterId(), message);
    }

    /**
     * 通知待审批数量变化
     */
    public void notifyPendingCountChange(Integer userId, Long pendingCount) {
        String message = String.format(
                "{\"type\":\"pending_count_change\",\"userId\":%d,\"pendingCount\":%d\"}",
                userId, pendingCount
        );
        sendToUser(userId, message);
    }

    /**
     * 发送给所有有审批权限的用户
     */
    private void sendToApprovers(String message) {
        List<User> approvers = userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.WAREHOUSE_KEEPER)
                .collect(Collectors.toList());


        for (User approver : approvers) {
            sendToUser(approver.getId(), message);
        }
    }

    /**
     * 发送给指定用户
     */
    private void sendToUser(Integer userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.debug("向用户 {} 发送WebSocket消息: {}", userId, message);
            } catch (Exception e) {
                log.error("向用户 {} 发送WebSocket消息失败: {}", userId, e.getMessage());
                // 清理失效的会话
                removeUserSession(session);
            }
        }
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Integer userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取用户会话
     */
    public WebSocketSession getUserSession(Integer userId) {
        return userSessions.get(userId);
    }

    /**
     * 清理所有会话
     */
    public void cleanup() {
        userSessions.clear();
        sessionUsers.clear();
        log.info("清理所有WebSocket会话");
    }
}