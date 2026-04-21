package com.hyundai.dms.service;

import com.hyundai.dms.entity.AuditLog;
import com.hyundai.dms.entity.User;
import com.hyundai.dms.repository.AuditLogRepository;
import com.hyundai.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void logAction(String action, String entityType, Long entityId, String description) {
        try {
            String username = "System";
            org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                username = auth.getName();
            }
            User user = userRepository.findByUsername(username).orElse(null);

            AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit logged: action={}, entity={}, id={}, user={}", action, entityType, entityId, username);
        } catch (Exception e) {
            // Audit failure must never break the main transaction
            log.error("Failed to save audit log: action={}, entity={}, id={} — {}", action, entityType, entityId, e.getMessage());
        }
    }
}
