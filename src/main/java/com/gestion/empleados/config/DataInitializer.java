package com.gestion.empleados.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gestion.empleados.common.Role;
import com.gestion.empleados.controlpoint.ControlPoint;
import com.gestion.empleados.controlpoint.ControlPointRepository;
import com.gestion.empleados.employee.Employee;
import com.gestion.empleados.employee.EmployeeRepository;

@Configuration
public class DataInitializer {

    @Value("${APP_ADMIN_PASSWORD:Admin123*}")
    private String adminPassword;

    @Bean
    CommandLineRunner seedData(EmployeeRepository employeeRepository, ControlPointRepository controlPointRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (employeeRepository.count() == 0) {
                Employee admin = new Employee();
                admin.setFullName("Administrador General");
                admin.setUsername("admin");
                admin.setEmail("admin@empresa.com");
               // admin.setPasswordHash(passwordEncoder.encode("Admin123*"));
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));

                admin.setRole(Role.ADMIN);
                admin.setEmployeeCode("admin-root");
                admin.setForcePasswordChange(false);
                employeeRepository.save(admin);
            }

            if (controlPointRepository.count() == 0) {
                ControlPoint cp = new ControlPoint();
                cp.setName("Entrada Principal");
                cp.setActive(true);
                controlPointRepository.save(cp);
            }
        };
    }
}
