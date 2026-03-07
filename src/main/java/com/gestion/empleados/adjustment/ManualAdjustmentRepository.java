package com.gestion.empleados.adjustment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestion.empleados.employee.Employee;

public interface ManualAdjustmentRepository extends JpaRepository<ManualAdjustmentRequest, Long> {
    List<ManualAdjustmentRequest> findByStatusOrderByCreatedAtDesc(AdjustmentStatus status);
    List<ManualAdjustmentRequest> findByEmployeeOrderByCreatedAtDesc(Employee employee);
    List<ManualAdjustmentRequest> findByEmployeeAndStatusOrderByCreatedAtDesc(Employee employee, AdjustmentStatus status);
    List<ManualAdjustmentRequest> findAllByOrderByCreatedAtDesc();
}
