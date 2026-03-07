package com.gestion.empleados.employee;

public class EmployeeResponse {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String employeeCode;
    private String role;
    private boolean active;

    public EmployeeResponse(Employee employee) {
        this.id = employee.getId();
        this.fullName = employee.getFullName();
        this.username = employee.getUsername();
         this.email = employee.getEmail();
        this.employeeCode = employee.getEmployeeCode();
        this.role = employee.getRole().name();
        this.active = employee.isActive();
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

      public String getEmail() {
        return email;
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
}
