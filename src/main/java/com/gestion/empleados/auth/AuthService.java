package com.gestion.empleados.auth;

import java.util.Map;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gestion.empleados.common.ApiException;
import com.gestion.empleados.employee.Employee;
import com.gestion.empleados.employee.EmployeeRepository;
import com.gestion.empleados.security.AppUserPrincipal;
import com.gestion.empleados.security.JwtService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    private final PasswordResetTokenRepository passwordResetTokenRepository;
private final JavaMailSender mailSender;
private final SecureRandom secureRandom = new SecureRandom();

@Value("${app.auth.reset.expiration-minutes:15}")
private long resetExpirationMinutes;

@Value("${app.frontend.reset-password-url:http://localhost:4200/reset-password}")
private String resetPasswordUrl;

@Value("${app.mail.from:no-reply@gestion.local}")
private String mailFrom;


    /*public AuthService(
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        EmployeeRepository employeeRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }*/

        public AuthService(
    AuthenticationManager authenticationManager,
    JwtService jwtService,
    EmployeeRepository employeeRepository,
    PasswordEncoder passwordEncoder,
    PasswordResetTokenRepository passwordResetTokenRepository,
    JavaMailSender mailSender
) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.employeeRepository = employeeRepository;
    this.passwordEncoder = passwordEncoder;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.mailSender = mailSender;
}


    public LoginResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }

        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        Employee employee = employeeRepository.findById(principal.getEmployeeId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (!employee.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Empleado desactivado");
        }

        String token = jwtService.generateToken(principal, Map.of("role", employee.getRole().name(), "eid", employee.getId()));
        return new LoginResponse(
            token,
            employee.getId(),
            employee.getEmployeeCode(),
            employee.getFullName(),
            employee.getRole().name(),
            employee.isForcePasswordChange()
        );
    }

    public void changePassword(Long employeeId, ChangePasswordRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), employee.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Clave actual incorrecta");
        }
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La nueva clave debe ser distinta");
        }
        employee.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        employee.setForcePasswordChange(false);
        employeeRepository.save(employee);
    }

    public Map<String, Object> getCredentials(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        return Map.of(
            "id", employee.getId(),
            "username", employee.getUsername(),
            "role", employee.getRole().name(),
            "active", employee.isActive(),
            "forcePasswordChange", employee.isForcePasswordChange()
        );
    }


    @Transactional
public void forgotPassword(ForgotPasswordRequest request) {
    employeeRepository.findByEmailIgnoreCase(request.getEmail()).ifPresent(employee -> {
        if (!employee.isActive()) return;

        passwordResetTokenRepository.deleteByEmployeeId(employee.getId());

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(generateResetToken());
        token.setEmployee(employee);
        token.setExpiresAt(Instant.now().plusSeconds(resetExpirationMinutes * 60));
        passwordResetTokenRepository.save(token);

        sendResetEmail(employee.getEmail(), employee.getFullName(), token.getToken());
    });
}

@Transactional
public void resetPassword(ResetPasswordRequest request) {
    PasswordResetToken token = passwordResetTokenRepository.findByTokenAndUsedAtIsNull(request.getToken())
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Token de recuperacion invalido"));

    if (token.getExpiresAt().isBefore(Instant.now())) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Token de recuperacion vencido");
    }

    Employee employee = token.getEmployee();
    employee.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    employee.setForcePasswordChange(false);
    employeeRepository.save(employee);

    token.setUsedAt(Instant.now());
    passwordResetTokenRepository.save(token);
}

private String generateResetToken() {
    byte[] bytes = new byte[24];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
}

private void sendResetEmail(String to, String fullName, String token) {
    String link = resetPasswordUrl + "?token=" + token;

    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(mailFrom);
    msg.setTo(to);
    msg.setSubject("Recuperacion de contrasena");
    msg.setText(
        "Hola " + fullName + ",\n\n" +
        "Recibimos una solicitud para restablecer tu contrasena.\n" +
        "Usa este enlace (valido por " + resetExpirationMinutes + " minutos):\n" +
        link + "\n\n" +
        "Si no solicitaste este cambio, ignora este correo."
    );
    mailSender.send(msg);
}

}
