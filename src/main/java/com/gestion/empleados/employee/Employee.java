package com.gestion.empleados.employee;

import java.time.Instant;
import java.time.LocalTime;

import com.gestion.empleados.common.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "employee_code", nullable = false, unique = true, length = 64)
    private String employeeCode;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(length = 120)
    private String email;

    @Column(length = 32)
    private String dni;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "shift_start")
    private LocalTime shiftStart;

    @Column(name = "tolerance_minutes", nullable = false)
    private Integer toleranceMinutes = 10;

    @Column(name = "max_pause_minutes", nullable = false)
    private Integer maxPauseMinutes = 45;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "force_password_change", nullable = false)
    private boolean forcePasswordChange = true;

    @Column(name = "created_by_admin_id")
    private Long createdByAdminId;

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }

    public void setForcePasswordChange(boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }

    public Long getCreatedByAdminId() { return createdByAdminId; }
public void setCreatedByAdminId(Long createdByAdminId) { this.createdByAdminId = createdByAdminId; }
}
