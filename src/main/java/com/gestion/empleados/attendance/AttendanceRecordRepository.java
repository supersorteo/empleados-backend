package com.gestion.empleados.attendance;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestion.empleados.employee.Employee;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    long countByEmployee_Id(Long employeeId);
    List<AttendanceRecord> findByEmployee_CreatedByAdminIdAndEventTimeBetweenOrderByEventTimeDesc(Long createdByAdminId, Instant from, Instant to);

    Optional<AttendanceRecord> findTopByEmployeeOrderByEventTimeDesc(Employee employee);
    Optional<AttendanceRecord> findTopByEmployeeAndEventTimeBetweenOrderByEventTimeDesc(Employee employee, Instant from, Instant to);
    List<AttendanceRecord> findByEventTimeBetweenOrderByEventTimeDesc(Instant from, Instant to);
    List<AttendanceRecord> findByEmployeeAndEventTimeBetweenOrderByEventTimeDesc(Employee employee, Instant from, Instant to);
    List<AttendanceRecord> findByEmployeeAndEventTimeBetweenAndEventTypeOrderByEventTimeDesc(
        Employee employee, Instant from, Instant to, com.gestion.empleados.common.EventType eventType
    );
}
