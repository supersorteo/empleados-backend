package com.gestion.empleados.audit;

import java.time.Instant;

public class AuditLogResponse {
    private Long id;
    private Long actorId;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private Instant createdAt;

    public AuditLogResponse(AuditLog log) {
        this.id = log.getId();
        this.actorId = log.getActor() == null ? null : log.getActor().getId();
        this.action = log.getAction();
        this.entityType = log.getEntityType();
        this.entityId = log.getEntityId();
        this.details = log.getDetails();
        this.createdAt = log.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getActorId() { return actorId; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}
