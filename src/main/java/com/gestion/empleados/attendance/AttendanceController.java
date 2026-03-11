package com.gestion.empleados.attendance;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.gestion.empleados.security.AppUserPrincipal;
import com.gestion.empleados.security.RateLimiterService;
import com.gestion.empleados.common.ApiException;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final RateLimiterService rateLimiterService;

    public AttendanceController(AttendanceService attendanceService, RateLimiterService rateLimiterService) {
        this.attendanceService = attendanceService;
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/qr-token/options")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    public AllowedEventsResponse getOptions(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        if (principal.isForcePasswordChange()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Debes cambiar tu clave antes de fichar");
        }
        return attendanceService.getAllowedEvents(principal.getEmployeeId());
    }

    @PostMapping("/qr-token")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    public QrTokenResponse createToken(Authentication authentication, @Valid @RequestBody CreateQrTokenRequest request) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        if (principal.isForcePasswordChange()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Debes cambiar tu clave antes de fichar");
        }
        return attendanceService.createQrToken(principal.getEmployeeId(), request);
    }

    @PostMapping("/scan")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public ScanResponse scan(Authentication authentication, @Valid @RequestBody ScanRequest request, HttpServletRequest httpRequest) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        if (principal.isForcePasswordChange()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Debes cambiar tu clave antes de usar el escaner");
        }
        String key = principal.getUsername() + "|" + httpRequest.getRemoteAddr();
        if (!rateLimiterService.allowScan(key)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados escaneos en poco tiempo");
        }
        // return attendanceService.scan(request);
        return attendanceService.scan(request, principal.getEmployeeId(), principal.getRole());
    }

    @GetMapping("/attendance")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AttendanceRecordResponse> list(
        Authentication authentication,
        @RequestParam(required = false) Long employeeId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        // return attendanceService.listRecords(from, to, employeeId);
        return attendanceService.listRecords(from, to, employeeId, principal.getEmployeeId());
    }

    @GetMapping(value = "/attendance/export.csv", produces = "text/csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportCsv(
        Authentication authentication,
        @RequestParam(required = false) Long employeeId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        // String csv = attendanceService.exportCsv(from, to, employeeId);
        String csv = attendanceService.exportCsv(from, to, employeeId, principal.getEmployeeId());
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }
}
