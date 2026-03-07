package com.gestion.empleados.audit;

import java.util.List; 

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLogResponse> list() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc()
            .stream()
            .map(AuditLogResponse::new)
            .toList();
    }
}
