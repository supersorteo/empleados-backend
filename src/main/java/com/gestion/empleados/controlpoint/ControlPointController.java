package com.gestion.empleados.controlpoint;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/control-points")
public class ControlPointController {
    private final ControlPointRepository controlPointRepository;

    public ControlPointController(ControlPointRepository controlPointRepository) {
        this.controlPointRepository = controlPointRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public List<ControlPoint> list() {
        return controlPointRepository.findAll();
    }
}
