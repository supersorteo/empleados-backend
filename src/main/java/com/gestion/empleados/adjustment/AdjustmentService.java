package com.gestion.empleados.adjustment;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestion.empleados.attendance.AttendanceRecord;
import com.gestion.empleados.attendance.AttendanceRecordRepository;
import com.gestion.empleados.audit.AuditService;
import com.gestion.empleados.common.ApiException;
import com.gestion.empleados.common.EventType;
import com.gestion.empleados.controlpoint.ControlPoint;
import com.gestion.empleados.controlpoint.ControlPointRepository;
import com.gestion.empleados.employee.Employee;
import com.gestion.empleados.employee.EmployeeRepository;

@Service
public class AdjustmentService {
    private final ManualAdjustmentRepository manualAdjustmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ControlPointRepository controlPointRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AuditService auditService;

    public AdjustmentService(
        ManualAdjustmentRepository manualAdjustmentRepository,
        EmployeeRepository employeeRepository,
        ControlPointRepository controlPointRepository,
        AttendanceRecordRepository attendanceRecordRepository,
        AuditService auditService
    ) {
        this.manualAdjustmentRepository = manualAdjustmentRepository;
        this.employeeRepository = employeeRepository;
        this.controlPointRepository = controlPointRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.auditService = auditService;
    }

    @Transactional
    public AdjustmentResponse create(CreateAdjustmentRequest request, Long requesterId) {
        Employee requester = getEmployee(requesterId);
        Employee target = getEmployee(request.getEmployeeId());

        ManualAdjustmentRequest entity = new ManualAdjustmentRequest();
        entity.setRequestedBy(requester);
        entity.setEmployee(target);
        entity.setReason(request.getReason());
        entity.setEvidenceUrl(request.getEvidenceUrl());
        entity.setRequestedTime(request.getRequestedTime());
        entity.setRequestedEventType(parseEventTypeOrNull(request.getRequestedEventType()));
        if (request.getControlPointId() != null) {
            ControlPoint controlPoint = controlPointRepository.findById(request.getControlPointId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Punto de control invalido"));
            entity.setControlPoint(controlPoint);
        }
        ManualAdjustmentRequest saved = manualAdjustmentRepository.save(entity);
        auditService.log(requester, "ADJUSTMENT_CREATED", "ManualAdjustmentRequest", saved.getId().toString(),
            "reason=" + saved.getReason());
        return new AdjustmentResponse(saved);
    }

    public List<AdjustmentResponse> list(String status, Long employeeId) {
        List<ManualAdjustmentRequest> rows;
        if (status != null && !status.isBlank() && employeeId != null) {
            AdjustmentStatus s = parseStatus(status);
            Employee employee = getEmployee(employeeId);
            rows = manualAdjustmentRepository.findByEmployeeAndStatusOrderByCreatedAtDesc(employee, s);
        } else if (status != null && !status.isBlank()) {
            AdjustmentStatus s = parseStatus(status);
            rows = manualAdjustmentRepository.findByStatusOrderByCreatedAtDesc(s);
        } else if (employeeId != null) {
            Employee employee = getEmployee(employeeId);
            rows = manualAdjustmentRepository.findByEmployeeOrderByCreatedAtDesc(employee);
        } else {
            rows = manualAdjustmentRepository.findAllByOrderByCreatedAtDesc();
        }
        return rows.stream().map(AdjustmentResponse::new).toList();
    }

    @Transactional
    public AdjustmentResponse review(Long adjustmentId, ReviewAdjustmentRequest request, Long reviewerId) {
        ManualAdjustmentRequest entity = manualAdjustmentRepository.findById(adjustmentId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ajuste no encontrado"));
        if (entity.getStatus() != AdjustmentStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El ajuste ya fue revisado");
        }

        Employee reviewer = getEmployee(reviewerId);
        AdjustmentStatus targetStatus = parseStatus(request.getStatus());
        entity.setStatus(targetStatus);
        entity.setReviewer(reviewer);
        entity.setReviewerComment(request.getReviewerComment());
        entity.setReviewedAt(Instant.now());

        if (targetStatus == AdjustmentStatus.APPROVED) {
            applyApprovedAdjustment(entity);
        }

        ManualAdjustmentRequest saved = manualAdjustmentRepository.save(entity);
        auditService.log(reviewer, "ADJUSTMENT_REVIEWED", "ManualAdjustmentRequest", saved.getId().toString(),
            "status=" + saved.getStatus().name());
        return new AdjustmentResponse(saved);
    }

    private void applyApprovedAdjustment(ManualAdjustmentRequest entity) {
        if (entity.getRequestedEventType() == null || entity.getRequestedTime() == null || entity.getControlPoint() == null) {
            return;
        }
        AttendanceRecord record = new AttendanceRecord();
        record.setEmployee(entity.getEmployee());
        record.setControlPoint(entity.getControlPoint());
        record.setEventType(entity.getRequestedEventType());
        record.setEventTime(entity.getRequestedTime());
        record.setLate(false);
        record.setExcessPause(false);
        record.setSource("MANUAL_AJUSTE");
        attendanceRecordRepository.save(record);
        entity.setAttendanceRecord(record);
    }

    private Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));
    }

    private AdjustmentStatus parseStatus(String status) {
        try {
            return AdjustmentStatus.valueOf(status.toUpperCase());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Estado de ajuste invalido");
        }
    }

    private EventType parseEventTypeOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return EventType.valueOf(value.toUpperCase());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tipo de evento invalido");
        }
    }
}
