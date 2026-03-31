package com.hyundai.dms.controller;

import com.hyundai.dms.dto.AuditLogDto;
import com.hyundai.dms.entity.AuditLog;
import com.hyundai.dms.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final com.hyundai.dms.repository.UserRepository userRepository;

    @jakarta.annotation.PostConstruct
    public void seedAuditLogs() {
        // Disabled for safety as it can interfere with other startup tasks
        System.out.println("Audit log seeding disabled.");
    }
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<AuditLogDto>> getLogs(
            @RequestParam(required = false) Long dealerId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        String filteredAction = (actionType != null && !actionType.trim().isEmpty()) ? actionType : null;
        String keyword = (search != null && !search.trim().isEmpty()) ? search : null;
        
        Page<AuditLog> logs = auditLogRepository.findWithFilters(dealerId, filteredAction, keyword, pageable);
        
        Page<AuditLogDto> dtos = logs.map(a -> AuditLogDto.builder()
                .id(a.getId())
                .username(a.getUser() != null ? a.getUser().getUsername() : "System")
                .dealerId(a.getUser() != null ? a.getUser().getDealerId() : null)
                .action(a.getAction())
                .entityType(a.getEntityType())
                .entityId(a.getEntityId())
                .description(a.getDescription())
                .createdAt(a.getCreatedAt())
                .build());
                
        return ResponseEntity.ok(dtos);
    }
}
