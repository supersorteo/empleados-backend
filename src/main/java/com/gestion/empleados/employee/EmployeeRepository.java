package com.gestion.empleados.employee;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gestion.empleados.common.Role;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByEmployeeCode(String employeeCode);
    boolean existsByRole(Role role);
    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, Long id);
    boolean existsByIdAndCreatedByAdminId(Long id, Long createdByAdminId);
    Page<Employee> findAllByOrderByIdDesc(Pageable pageable);
    Page<Employee> findByActiveTrueOrderByIdDesc(Pageable pageable);
    Page<Employee> findByCreatedByAdminIdAndActiveTrueOrderByIdDesc(Long createdByAdminId, Pageable pageable);

    Optional<Employee> findByEmailIgnoreCase(String email);


    List<Employee> findByRoleOrderByIdDesc(Role role);
    List<Employee> findByCreatedByAdminIdOrderByIdDesc(Long createdByAdminId);
    List<Employee> findByCreatedByAdminIdAndRoleOrderByIdDesc(Long createdByAdminId, Role role);
}
