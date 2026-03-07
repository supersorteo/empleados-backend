package com.gestion.empleados.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QrTokenRepository extends JpaRepository<QrToken, String> {
}
