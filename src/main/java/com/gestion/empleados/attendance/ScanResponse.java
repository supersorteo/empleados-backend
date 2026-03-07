package com.gestion.empleados.attendance;

import java.time.Instant;

public class ScanResponse {
    private boolean ok;
    private String estado;
    private String empleado;
    private String tipoEvento;
    private Instant hora;
    private Long registroId;

    public ScanResponse(boolean ok, String estado, String empleado, String tipoEvento, Instant hora, Long registroId) {
        this.ok = ok;
        this.estado = estado;
        this.empleado = empleado;
        this.tipoEvento = tipoEvento;
        this.hora = hora;
        this.registroId = registroId;
    }

    public boolean isOk() {
        return ok;
    }

    public String getEstado() {
        return estado;
    }

    public String getEmpleado() {
        return empleado;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public Instant getHora() {
        return hora;
    }

    public Long getRegistroId() {
        return registroId;
    }
}
