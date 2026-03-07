package com.gestion.empleados.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.gestion.empleados.common.Role;
import com.gestion.empleados.employee.Employee;

public class AppUserPrincipal implements UserDetails {
    private final Employee employee;

    public AppUserPrincipal(Employee employee) {
        this.employee = employee;
    }

    public Long getEmployeeId() {
        return employee.getId();
    }

    public Role getRole() {
        return employee.getRole();
    }

    public String getEmployeeCode() {
        return employee.getEmployeeCode();
    }

    public boolean isForcePasswordChange() {
        return employee.isForcePasswordChange();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name()));
    }

    @Override
    public String getPassword() {
        return employee.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return employee.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return employee.isActive();
    }
}
