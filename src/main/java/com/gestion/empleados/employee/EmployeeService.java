package com.gestion.empleados.employee;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestion.empleados.adjustment.ManualAdjustmentRepository;
import com.gestion.empleados.attendance.AttendanceRecordRepository;
import com.gestion.empleados.attendance.QrTokenRepository;
import com.gestion.empleados.audit.AuditLogRepository;
import com.gestion.empleados.audit.AuditService;
import com.gestion.empleados.common.ApiException;
import com.gestion.empleados.common.Role;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final QrTokenRepository qrTokenRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ManualAdjustmentRepository manualAdjustmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmployeeService(
        EmployeeRepository employeeRepository,
        PasswordEncoder passwordEncoder,
        AuditService auditService,
        QrTokenRepository qrTokenRepository,
        AttendanceRecordRepository attendanceRecordRepository,
        ManualAdjustmentRepository manualAdjustmentRepository,
        AuditLogRepository auditLogRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.qrTokenRepository = qrTokenRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.manualAdjustmentRepository = manualAdjustmentRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public EmployeeResponse createEmployee(CreateEmployeeRequest request, Long actorId) {
        if (employeeRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El username ya existe");
        }

        Employee employee = new Employee();
        employee.setFullName(request.getFullName());
        employee.setUsername(request.getUsername());
        employee.setEmail(request.getEmail());
        employee.setDni(request.getDni());
        employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        employee.setCreatedByAdminId(actorId);

        try {
            employee.setRole(Role.valueOf(request.getRole().toUpperCase()));
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Rol invalido");
        }
        employee.setEmployeeCode(generateEmployeeCode());
        employee.setShiftStart(request.getShiftStart());
        employee.setToleranceMinutes(request.getToleranceMinutes() == null ? 10 : request.getToleranceMinutes());
        employee.setMaxPauseMinutes(request.getMaxPauseMinutes() == null ? 45 : request.getMaxPauseMinutes());
        employee.setForcePasswordChange(true);

        Employee saved = employeeRepository.save(employee);
        if (actorId != null) {
            Employee actor = findById(actorId);
            auditService.log(actor, "EMPLOYEE_CREATED", "Employee", saved.getId().toString(), "username=" + saved.getUsername());
        }
        return new EmployeeResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> listUsers(Pageable pageable) {
        return employeeRepository.findAllByOrderByIdDesc(pageable).map(EmployeeResponse::new);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getUserById(Long userId) {
        Employee employee = employeeRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return new EmployeeResponse(employee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> listEmployees(Pageable pageable, Long actorId) {
        // return employeeRepository.findAllByOrderByIdDesc(pageable).map(EmployeeResponse::new);
        // return employeeRepository.findByActiveTrueOrderByIdDesc(pageable).map(EmployeeResponse::new);
        return employeeRepository.findByCreatedByAdminIdAndActiveTrueOrderByIdDesc(actorId, pageable)
            .map(EmployeeResponse::new);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));
        return new EmployeeResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long employeeId, UpdateEmployeeRequest request, Long actorId) {
        Employee employee = findById(employeeId);
        if (employeeRepository.existsByUsernameAndIdNot(request.getUsername(), employeeId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El username ya existe");
        }

        employee.setFullName(request.getFullName());
        employee.setUsername(request.getUsername());
        employee.setEmail(request.getEmail());
        employee.setDni(request.getDni());
        employee.setShiftStart(request.getShiftStart());
        employee.setToleranceMinutes(request.getToleranceMinutes() == null ? 10 : request.getToleranceMinutes());
        employee.setMaxPauseMinutes(request.getMaxPauseMinutes() == null ? 45 : request.getMaxPauseMinutes());

        try {
            employee.setRole(Role.valueOf(request.getRole().toUpperCase()));
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Rol invalido");
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            employee.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            employee.setForcePasswordChange(true);
        }

        Employee saved = employeeRepository.save(employee);
        if (actorId != null) {
            Employee actor = findById(actorId);
            auditService.log(actor, "EMPLOYEE_UPDATED", "Employee", saved.getId().toString(), "username=" + saved.getUsername());
        }
        return new EmployeeResponse(saved);
    }

    /*  @Transactional
    public void deleteEmployee(Long employeeId, Long actorId) {
        Employee employee = findById(employeeId);
        boolean hardDeleted = false;
        // employee.setActive(false);
        // employeeRepository.save(employee);
        try {
            employeeRepository.delete(employee);
            hardDeleted = true;
        } catch (DataIntegrityViolationException ex) {
            // throw new ApiException(HttpStatus.CONFLICT, "No se puede eliminar: el empleado tiene registros relacionados");
            employee.setActive(false);
            employeeRepository.save(employee);
        }

        if (actorId != null) {
            Employee actor = findById(actorId);
            // auditService.log(actor, "EMPLOYEE_DELETED", "Employee", employee.getId().toString(), "soft_delete=true");
            auditService.log(
                actor,
                "EMPLOYEE_DELETED",
                "Employee",
                employee.getId().toString(),
                hardDeleted ? "hard_delete=true" : "soft_delete=true"
            );
        }
    }*/

    @Transactional
    public void deleteEmployee(Long employeeId, Long actorId, boolean forceDelete) {
        Employee employee = findById(employeeId);

        long qrTokens = qrTokenRepository.countByEmployee_Id(employeeId);
        long attendanceRecords = attendanceRecordRepository.countByEmployee_Id(employeeId);
        long adjustments = manualAdjustmentRepository.countByEmployee_IdOrRequestedBy_IdOrReviewer_Id(employeeId, employeeId, employeeId);
        long auditLogs = auditLogRepository.countByActor_Id(employeeId);
        boolean hasRelatedData = (qrTokens + attendanceRecords + adjustments + auditLogs) > 0;

        if (hasRelatedData && !forceDelete) {
            throw new ApiException(
                HttpStatus.CONFLICT,
                "El empleado tiene datos relacionados. Confirma eliminacion forzada para borrar todo su historial."
            );
        }

        // employee.setActive(false);
        // employeeRepository.save(employee);
        employeeRepository.delete(employee);

        if (actorId != null) {
            Employee actor = findById(actorId);
            String details = hasRelatedData
                ? "hard_delete=true, cascade=true, related_data_deleted=true"
                : "hard_delete=true, cascade=true, related_data_deleted=false";
            auditService.log(actor, "EMPLOYEE_DELETED", "Employee", employee.getId().toString(), details);
        }
    }


    public EmployeeResponse setActive(Long employeeId, boolean active, Long actorId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));
        employee.setActive(active);
        Employee actor = actorId == null ? null : findById(actorId);
        auditService.log(actor, "EMPLOYEE_ACTIVE_CHANGED", "Employee", employee.getId().toString(), "active=" + active);
        return new EmployeeResponse(employeeRepository.save(employee));
    }

    public Employee findById(Long employeeId) {
        return employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));
    }

    public Optional<Employee> findByEmployeeCode(String employeeCode) {
        return employeeRepository.findByEmployeeCode(employeeCode);
    }

    private String generateEmployeeCode() {
        byte[] bytes = new byte[12];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


    @Transactional(readOnly = true)
    public List<EmployeeResponse> listAdmins() {
        return employeeRepository.findByRoleOrderByIdDesc(Role.ADMIN).stream().map(EmployeeResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> listEmployeesCreatedByAdmin(Long adminId) {
        return employeeRepository.findByCreatedByAdminIdOrderByIdDesc(adminId).stream().map(EmployeeResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> listByRoleCreatedByAdmin(Long adminId, Role role) {
        return employeeRepository.findByCreatedByAdminIdAndRoleOrderByIdDesc(adminId, role)
            .stream().map(EmployeeResponse::new).toList();
    }
    
}
