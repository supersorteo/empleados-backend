package com.gestion.empleados.attendance;

import java.time.Instant;

public class AttendanceRecordResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long controlPointId;
    private String controlPointName;
    private String eventType;
    private Instant eventTime;
    private boolean late;
    private boolean excessPause;
    private String source;

    public AttendanceRecordResponse(AttendanceRecord r) {
        this.id = r.getId();
        this.employeeId = r.getEmployee().getId();
        this.employeeName = r.getEmployee().getFullName();
        this.controlPointId = r.getControlPoint().getId();
        this.controlPointName = r.getControlPoint().getName();
        this.eventType = r.getEventType().name();
        this.eventTime = r.getEventTime();
        this.late = r.isLate();
        this.excessPause = r.isExcessPause();
        this.source = r.getSource();
    }

    public Long getId() {
        return id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public Long getControlPointId() {
        return controlPointId;
    }

    public String getControlPointName() {
        return controlPointName;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public boolean isLate() {
        return late;
    }

    public boolean isExcessPause() {
        return excessPause;
    }

    public String getSource() {
        return source;
    }
}
