package com.gestion.empleados.adjustment;

import java.time.Instant;

public class AdjustmentResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String requestedEventType;
    private Instant requestedTime;
    private String reason;
    private String evidenceUrl;
    private String status;
    private String reviewerComment;
    private Instant createdAt;
    private Instant reviewedAt;

    public AdjustmentResponse(ManualAdjustmentRequest r) {
        this.id = r.getId();
        this.employeeId = r.getEmployee().getId();
        this.employeeName = r.getEmployee().getFullName();
        this.requestedEventType = r.getRequestedEventType() == null ? null : r.getRequestedEventType().name();
        this.requestedTime = r.getRequestedTime();
        this.reason = r.getReason();
        this.evidenceUrl = r.getEvidenceUrl();
        this.status = r.getStatus().name();
        this.reviewerComment = r.getReviewerComment();
        this.createdAt = r.getCreatedAt();
        this.reviewedAt = r.getReviewedAt();
    }

    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getRequestedEventType() { return requestedEventType; }
    public Instant getRequestedTime() { return requestedTime; }
    public String getReason() { return reason; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public String getStatus() { return status; }
    public String getReviewerComment() { return reviewerComment; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReviewedAt() { return reviewedAt; }
}
