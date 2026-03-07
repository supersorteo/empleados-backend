package com.gestion.empleados.employee;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestion.empleados.common.ApiException;

@RestController
@RequestMapping("/api/public")
public class PublicEmployeeController {
    private final EmployeeService employeeService;

    public PublicEmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/employee-home/{employeeCode}")
    public Map<String, Object> employeeHome(@PathVariable String employeeCode) {
        Employee employee = employeeService.findByEmployeeCode(employeeCode)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));
        return Map.of(
            "employeeCode", employee.getEmployeeCode(),
            "fullName", employee.getFullName(),
            "active", employee.isActive()
        );
    }
}
