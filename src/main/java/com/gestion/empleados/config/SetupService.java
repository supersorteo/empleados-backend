package com.gestion.empleados.config;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestion.empleados.common.ApiException;
import com.gestion.empleados.common.Role;
import com.gestion.empleados.employee.Employee;
import com.gestion.empleados.employee.EmployeeRepository;

@Service
public class SetupService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public SetupService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public SetupStatusResponse getStatus() {
        return new SetupStatusResponse(employeeRepository.existsByRole(Role.ADMIN));
    }

    @Transactional
    public void bootstrapAdmin(BootstrapAdminRequest request) {
        if (employeeRepository.existsByRole(Role.ADMIN)) {
            throw new ApiException(HttpStatus.CONFLICT, "La aplicacion ya fue inicializada");
        }
        if (employeeRepository.existsByUsername(request.getUsername().trim())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El username ya existe");
        }

        Employee admin = new Employee();
        admin.setFullName(request.getFullName().trim());
        admin.setUsername(request.getUsername().trim());
        admin.setEmail(request.getEmail() == null ? null : request.getEmail().trim());
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        admin.setRole(Role.ADMIN);
        admin.setEmployeeCode(generateEmployeeCode());
        admin.setForcePasswordChange(false);

        Employee saved = employeeRepository.save(admin);
        saved.setCreatedByAdminId(saved.getId());
        employeeRepository.save(saved);
    }

    private String generateEmployeeCode() {
        byte[] bytes = new byte[12];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
