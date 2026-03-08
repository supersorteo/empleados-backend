package com.gestion.empleados.audit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    long countByActor_Id(Long actorId);

    List<AuditLog> findTop100ByOrderByCreatedAtDesc();
}
