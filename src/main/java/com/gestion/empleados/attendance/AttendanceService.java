package com.gestion.empleados.attendance;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestion.empleados.audit.AuditService;
import com.gestion.empleados.common.ApiException;
import com.gestion.empleados.common.EventType;
import com.gestion.empleados.controlpoint.ControlPoint;
import com.gestion.empleados.controlpoint.ControlPointRepository;
import com.gestion.empleados.employee.Employee;
import com.gestion.empleados.employee.EmployeeRepository;

@Service
public class AttendanceService {
    private final QrTokenRepository qrTokenRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final ControlPointRepository controlPointRepository;
    private final AuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.qr.ttl-seconds}")
    private long qrTtlSeconds;
    @Value("${app.attendance.allow-loose-events:false}")
    private boolean allowLooseEvents;
    @Value("${app.attendance.earliest-entry-minutes-before-shift:120}")
    private long earliestEntryMinutesBeforeShift;

    public AttendanceService(
        QrTokenRepository qrTokenRepository,
        AttendanceRecordRepository attendanceRecordRepository,
        EmployeeRepository employeeRepository,
        ControlPointRepository controlPointRepository,
        AuditService auditService
    ) {
        this.qrTokenRepository = qrTokenRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.employeeRepository = employeeRepository;
        this.controlPointRepository = controlPointRepository;
        this.auditService = auditService;
    }

    public AllowedEventsResponse getAllowedEvents(Long employeeId) {
        Employee employee = getActiveEmployee(employeeId);
        List<EventType> allowed = computeAllowedEvents(employee);
        String status = allowed.isEmpty() ? "JORNADA_CERRADA" : "DISPONIBLE";
        return new AllowedEventsResponse(allowed.stream().map(Enum::name).toList(), status);
    }

    @Transactional
    public QrTokenResponse createQrToken(Long employeeId, CreateQrTokenRequest request) {
        Employee employee = getActiveEmployee(employeeId);
        EventType eventType = parseEventType(request.getTipoEventoSolicitado());
        List<EventType> allowed = computeAllowedEvents(employee);
        if (!allowed.contains(eventType)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tipo de evento no permitido en este estado");
        }

        Instant now = Instant.now();
        QrToken token = new QrToken();
        token.setTokenId(generateTokenValue());
        token.setEmployee(employee);
        token.setRequestedEventType(eventType);
        token.setCreatedAt(now);
        token.setExpiresAt(now.plusSeconds(qrTtlSeconds));
        qrTokenRepository.save(token);

        return new QrTokenResponse(token.getTokenId(), token.getExpiresAt(), eventType.name());
    }

    @Transactional
    public ScanResponse scan(ScanRequest request) {
        Instant now = Instant.now();
        QrToken token = qrTokenRepository.findById(request.getToken())
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Token invalido"));

        if (token.getUsedAt() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Token ya usado");
        }
        if (token.getExpiresAt().isBefore(now)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Token vencido");
        }

        Employee employee = token.getEmployee();
        if (!employee.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Empleado desactivado");
        }

        ControlPoint controlPoint = controlPointRepository.findById(request.getPuntoControlId())
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Punto de control invalido"));
        if (!controlPoint.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Punto de control inactivo");
        }

        EventType eventType = token.getRequestedEventType();
        List<EventType> allowed = computeAllowedEvents(employee);
        if (!allowed.contains(eventType)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Evento no permitido por estado actual");
        }
        validateShiftWindow(employee, eventType, now);

        AttendanceRecord record = new AttendanceRecord();
        record.setEmployee(employee);
        record.setControlPoint(controlPoint);
        record.setEventType(eventType);
        record.setEventTime(now);
        record.setLate(isLate(employee, eventType, now));
        record.setExcessPause(isExcessPause(employee, eventType, now));
        record.setSource("QR_DINAMICO");
        attendanceRecordRepository.save(record);

        token.setUsedAt(now);
        token.setControlPoint(controlPoint);
        qrTokenRepository.save(token);
        auditService.log(employee, "ATTENDANCE_RECORDED", "AttendanceRecord", record.getId().toString(),
            "event=" + record.getEventType().name() + ",source=QR_DINAMICO");

        String status = record.isLate() ? "TARDE" : "OK";
        if (record.isExcessPause()) {
            status = "EXCESO_PAUSA";
        }
        return new ScanResponse(true, status, employee.getFullName(), eventType.name(), now, record.getId());
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> listRecords(Instant from, Instant to, Long employeeId) {
        if (from == null || to == null) {
            LocalDate today = LocalDate.now();
            from = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
            to = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        }

        List<AttendanceRecord> records;
        if (employeeId == null) {
            records = attendanceRecordRepository.findByEventTimeBetweenOrderByEventTimeDesc(from, to);
        } else {
            Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));
            records = attendanceRecordRepository.findByEmployeeAndEventTimeBetweenOrderByEventTimeDesc(employee, from, to);
        }

        return records.stream().map(AttendanceRecordResponse::new).toList();
    }

    public String exportCsv(Instant from, Instant to, Long employeeId) {
        List<AttendanceRecordResponse> rows = listRecords(from, to, employeeId);
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add("id,employee_id,employee_name,control_point_id,control_point_name,event_type,event_time,late,excess_pause,source");
        for (AttendanceRecordResponse r : rows) {
            joiner.add(String.format("%d,%d,%s,%d,%s,%s,%s,%s,%s,%s",
                r.getId(),
                r.getEmployeeId(),
                csv(r.getEmployeeName()),
                r.getControlPointId(),
                csv(r.getControlPointName()),
                r.getEventType(),
                r.getEventTime(),
                r.isLate(),
                r.isExcessPause(),
                r.getSource()
            ));
        }
        return joiner.toString();
    }

    private List<EventType> computeAllowedEvents(Employee employee) {
        LocalDate today = LocalDate.now();
        Instant dayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant dayEnd = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        AttendanceRecord last = attendanceRecordRepository
            .findTopByEmployeeAndEventTimeBetweenOrderByEventTimeDesc(employee, dayStart, dayEnd)
            .orElse(null);
        if (last == null) {
            return allowLooseEvents ? List.of(EventType.ENTRADA, EventType.SALIDA) : List.of(EventType.ENTRADA);
        }
        return switch (last.getEventType()) {
            case ENTRADA -> List.of(EventType.SALIDA, EventType.PAUSA_INICIO);
            case PAUSA_INICIO -> List.of(EventType.PAUSA_FIN);
            case PAUSA_FIN -> List.of(EventType.SALIDA, EventType.PAUSA_INICIO);
            case SALIDA -> List.of();
        };
    }

    private boolean isLate(Employee employee, EventType eventType, Instant eventTime) {
        if (eventType != EventType.ENTRADA || employee.getShiftStart() == null) {
            return false;
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(eventTime, ZoneId.systemDefault());
        LocalTime threshold = employee.getShiftStart().plusMinutes(employee.getToleranceMinutes());
        return ldt.toLocalTime().isAfter(threshold);
    }

    private void validateShiftWindow(Employee employee, EventType eventType, Instant eventTime) {
        if (eventType != EventType.ENTRADA || employee.getShiftStart() == null) {
            return;
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(eventTime, ZoneId.systemDefault());
        LocalTime earliest = employee.getShiftStart().minusMinutes(earliestEntryMinutesBeforeShift);
        if (localDateTime.toLocalTime().isBefore(earliest)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Fichaje de entrada fuera de ventana horaria");
        }
    }

    private boolean isExcessPause(Employee employee, EventType eventType, Instant eventTime) {
        if (eventType != EventType.PAUSA_FIN) {
            return false;
        }
        LocalDate date = LocalDateTime.ofInstant(eventTime, ZoneId.systemDefault()).toLocalDate();
        Instant dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        List<AttendanceRecord> pauses = attendanceRecordRepository
            .findByEmployeeAndEventTimeBetweenAndEventTypeOrderByEventTimeDesc(employee, dayStart, dayEnd, EventType.PAUSA_INICIO);
        if (pauses.isEmpty()) {
            return false;
        }
        AttendanceRecord pauseStart = pauses.get(0);
        long pauseMinutes = java.time.Duration.between(pauseStart.getEventTime(), eventTime).toMinutes();
        return pauseMinutes > employee.getMaxPauseMinutes();
    }

    private Employee getActiveEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));
        if (!employee.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Empleado desactivado");
        }
        return employee;
    }

    private String generateTokenValue() {
        byte[] bytes = new byte[18];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String csv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private EventType parseEventType(String value) {
        try {
            return EventType.valueOf(value.toUpperCase());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tipo de evento invalido");
        }
    }
}
