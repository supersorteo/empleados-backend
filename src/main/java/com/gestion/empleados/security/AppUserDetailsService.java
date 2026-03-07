package com.gestion.empleados.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gestion.empleados.employee.Employee;
import com.gestion.empleados.employee.EmployeeRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final EmployeeRepository employeeRepository;

    public AppUserDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return new AppUserPrincipal(employee);
    }
}
