package com.gestion.empleados.adjustment;

import java.time.Instant;

import com.gestion.empleados.attendance.AttendanceRecord;
import com.gestion.empleados.common.EventType;
import com.gestion.empleados.controlpoint.ControlPoint;
import com.gestion.empleados.employee.Employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "manual_adjustment_requests")
public class ManualAdjustmentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private Employee requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_record_id")
    private AttendanceRecord attendanceRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_event_type", length = 30)
    private EventType requestedEventType;

    @Column(name = "requested_time")
    private Instant requestedTime;

    @Column(nullable = false, length = 400)
    private String reason;

    @Column(name = "evidence_url", length = 300)
    private String evidenceUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdjustmentStatus status = AdjustmentStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private Employee reviewer;

    @Column(name = "reviewer_comment", length = 400)
    private String reviewerComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "control_point_id")
    private ControlPoint controlPoint;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    public Long getId() { return id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public Employee getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Employee requestedBy) { this.requestedBy = requestedBy; }
    public AttendanceRecord getAttendanceRecord() { return attendanceRecord; }
    public void setAttendanceRecord(AttendanceRecord attendanceRecord) { this.attendanceRecord = attendanceRecord; }
    public EventType getRequestedEventType() { return requestedEventType; }
    public void setRequestedEventType(EventType requestedEventType) { this.requestedEventType = requestedEventType; }
    public Instant getRequestedTime() { return requestedTime; }
    public void setRequestedTime(Instant requestedTime) { this.requestedTime = requestedTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }
    public AdjustmentStatus getStatus() { return status; }
    public void setStatus(AdjustmentStatus status) { this.status = status; }
    public Employee getReviewer() { return reviewer; }
    public void setReviewer(Employee reviewer) { this.reviewer = reviewer; }
    public String getReviewerComment() { return reviewerComment; }
    public void setReviewerComment(String reviewerComment) { this.reviewerComment = reviewerComment; }
    public ControlPoint getControlPoint() { return controlPoint; }
    public void setControlPoint(ControlPoint controlPoint) { this.controlPoint = controlPoint; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
}
