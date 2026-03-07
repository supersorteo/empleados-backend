package com.gestion.empleados.attendance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ScanRequest {
    @NotBlank
    private String token;
    @NotNull
    private Long puntoControlId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getPuntoControlId() {
        return puntoControlId;
    }

    public void setPuntoControlId(Long puntoControlId) {
        this.puntoControlId = puntoControlId;
    }
}
