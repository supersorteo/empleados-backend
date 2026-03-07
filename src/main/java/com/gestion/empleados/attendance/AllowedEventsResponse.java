package com.gestion.empleados.attendance;

import java.util.List;

public class AllowedEventsResponse {
    private List<String> allowed;
    private String status;

    public AllowedEventsResponse(List<String> allowed, String status) {
        this.allowed = allowed;
        this.status = status;
    }

    public List<String> getAllowed() {
        return allowed;
    }

    public String getStatus() {
        return status;
    }
}
