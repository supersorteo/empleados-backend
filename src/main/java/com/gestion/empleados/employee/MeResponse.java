package com.gestion.empleados.employee;

public class MeResponse {
    private Long id;
    private String fullName;
    private String employeeCode;
    private String role;
    private boolean active;
    private boolean forcePasswordChange;

    public MeResponse(Employee employee) {
        this.id = employee.getId();
        this.fullName = employee.getFullName();
        this.employeeCode = employee.getEmployeeCode();
        this.role = employee.getRole().name();
        this.active = employee.isActive();
        this.forcePasswordChange = employee.isForcePasswordChange();
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }
}
