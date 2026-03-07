package com.gestion.empleados.employee;

import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateEmployeeRequest {
    @NotBlank
    private String fullName;
    @NotBlank
    private String username;
    private String email;
    private String dni;
    @NotBlank
    private String password;
    @NotNull
    private String role;
    private LocalTime shiftStart;
    private Integer toleranceMinutes = 10;
    private Integer maxPauseMinutes = 45;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalTime getShiftStart() {
        return shiftStart;
    }

    public void setShiftStart(LocalTime shiftStart) {
        this.shiftStart = shiftStart;
    }

    public Integer getToleranceMinutes() {
        return toleranceMinutes;
    }

    public void setToleranceMinutes(Integer toleranceMinutes) {
        this.toleranceMinutes = toleranceMinutes;
    }

    public Integer getMaxPauseMinutes() {
        return maxPauseMinutes;
    }

    public void setMaxPauseMinutes(Integer maxPauseMinutes) {
        this.maxPauseMinutes = maxPauseMinutes;
    }
}
