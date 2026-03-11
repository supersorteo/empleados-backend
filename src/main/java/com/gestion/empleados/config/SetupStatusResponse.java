package com.gestion.empleados.config;

public class SetupStatusResponse {
    private final boolean initialized;

    public SetupStatusResponse(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
