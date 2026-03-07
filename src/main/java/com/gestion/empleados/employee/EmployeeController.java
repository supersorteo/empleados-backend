package com.gestion.empleados.employee;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestion.empleados.common.Role;
import com.gestion.empleados.security.AppUserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeResponse create(Authentication authentication, @Valid @RequestBody CreateEmployeeRequest request) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return employeeService.createEmployee(request, principal.getEmployeeId());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<EmployeeResponse> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeService.listEmployees(pageable);
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeResponse getById(@PathVariable Long employeeId) {
        return employeeService.getEmployeeById(employeeId);
    }

    @PatchMapping("/{employeeId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeResponse setActive(Authentication authentication, @PathVariable Long employeeId, @RequestBody Map<String, Boolean> body) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return employeeService.setActive(employeeId, body.getOrDefault("active", true), principal.getEmployeeId());
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeResponse update(
        Authentication authentication,
        @PathVariable Long employeeId,
        @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return employeeService.updateEmployee(employeeId, request, principal.getEmployeeId());
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> delete(Authentication authentication, @PathVariable Long employeeId) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        employeeService.deleteEmployee(employeeId, principal.getEmployeeId());
        return Map.of("ok", true);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public MeResponse me(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        Employee employee = employeeService.findById(principal.getEmployeeId());
        return new MeResponse(employee);
    }


    @GetMapping("/admins")
@PreAuthorize("hasRole('ADMIN')")
public List<EmployeeResponse> listAdmins() {
    return employeeService.listAdmins();
}

@GetMapping("/mine")
@PreAuthorize("hasRole('ADMIN')")
public List<EmployeeResponse> myRegistered(Authentication authentication) {
    AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
    return employeeService.listEmployeesCreatedByAdmin(principal.getEmployeeId());
}

@GetMapping("/mine/by-role")
@PreAuthorize("hasRole('ADMIN')")
public List<EmployeeResponse> myRegisteredByRole(
    Authentication authentication,
    @RequestParam String role
) {
    AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
    Role parsed = Role.valueOf(role.toUpperCase());
    return employeeService.listByRoleCreatedByAdmin(principal.getEmployeeId(), parsed);
}

}
