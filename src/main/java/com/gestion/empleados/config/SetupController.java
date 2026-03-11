package com.gestion.empleados.config;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/setup")
public class SetupController {
    private final SetupService setupService;

    public SetupController(SetupService setupService) {
        this.setupService = setupService;
    }

    @GetMapping("/status")
    public SetupStatusResponse status() {
        return setupService.getStatus();
    }

    @PostMapping("/bootstrap-admin")
    public Map<String, Object> bootstrapAdmin(@Valid @RequestBody BootstrapAdminRequest request) {
        setupService.bootstrapAdmin(request);
        return Map.of("ok", true);
    }
}
