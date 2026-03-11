package com.gestion.empleados.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BootstrapAdminRequest {
    @NotBlank
    @Size(min = 3, max = 120)
    private String fullName;

    @NotBlank
    @Size(min = 3, max = 80)
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$")
    private String username;

    @Email
    @Size(max = 120)
    private String email;

    @NotBlank
    @Size(min = 8, max = 120)
    private String password;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
