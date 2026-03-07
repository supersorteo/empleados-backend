package com.gestion.empleados.auth;

public class LoginResponse {
    private String token;
    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private String role;
    private boolean forcePasswordChange;

    public LoginResponse(String token, Long employeeId, String employeeCode, String fullName, String role, boolean forcePasswordChange) {
        this.token = token;
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.role = role;
        this.forcePasswordChange = forcePasswordChange;
    }

    public String getToken() {
        return token;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }
}
