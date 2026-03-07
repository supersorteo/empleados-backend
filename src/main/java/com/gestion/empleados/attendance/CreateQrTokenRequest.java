package com.gestion.empleados.attendance;

import jakarta.validation.constraints.NotBlank;

public class CreateQrTokenRequest {
    @NotBlank
    private String tipoEventoSolicitado;

    public String getTipoEventoSolicitado() {
        return tipoEventoSolicitado;
    }

    public void setTipoEventoSolicitado(String tipoEventoSolicitado) {
        this.tipoEventoSolicitado = tipoEventoSolicitado;
    }
}
