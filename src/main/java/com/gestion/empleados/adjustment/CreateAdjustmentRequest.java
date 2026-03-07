package com.gestion.empleados.adjustment;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateAdjustmentRequest {
    @NotNull
    private Long employeeId;
    private String requestedEventType;
    private Instant requestedTime;
    @NotBlank
    private String reason;
    private String evidenceUrl;
    private Long controlPointId;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getRequestedEventType() { return requestedEventType; }
    public void setRequestedEventType(String requestedEventType) { this.requestedEventType = requestedEventType; }
    public Instant getRequestedTime() { return requestedTime; }
    public void setRequestedTime(Instant requestedTime) { this.requestedTime = requestedTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }
    public Long getControlPointId() { return controlPointId; }
    public void setControlPointId(Long controlPointId) { this.controlPointId = controlPointId; }
}
