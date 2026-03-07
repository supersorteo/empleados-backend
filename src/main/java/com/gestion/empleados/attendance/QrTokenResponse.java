package com.gestion.empleados.attendance;

import java.time.Instant;

public class QrTokenResponse {
    private String token;
    private Instant expiraEn;
    private String tipoEvento;

    public QrTokenResponse(String token, Instant expiraEn, String tipoEvento) {
        this.token = token;
        this.expiraEn = expiraEn;
        this.tipoEvento = tipoEvento;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiraEn() {
        return expiraEn;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }
}
