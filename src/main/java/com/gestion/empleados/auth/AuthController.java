package com.gestion.empleados.auth;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestion.empleados.common.ApiException;
import com.gestion.empleados.security.RateLimiterService;
import com.gestion.empleados.security.AppUserPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final RateLimiterService rateLimiterService;

    public AuthController(AuthService authService, RateLimiterService rateLimiterService) {
        this.authService = authService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String key = request.getUsername() + "|" + httpRequest.getRemoteAddr();
        if (!rateLimiterService.allowLogin(key)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos de login");
        }
        return authService.login(request);
    }

    @PostMapping("/change-password")
    public Map<String, Object> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        authService.changePassword(principal.getEmployeeId(), request);
        return Map.of("ok", true);
    }

    @GetMapping("/credentials")
    public Map<String, Object> credentials(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return authService.getCredentials(principal.getEmployeeId());
    }

    @PostMapping("/forgot-password")
public Map<String, Object> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    authService.forgotPassword(request);
    return Map.of("ok", true);
}

@PostMapping("/reset-password")
public Map<String, Object> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request);
    return Map.of("ok", true);
}

}
