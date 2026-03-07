package com.gestion.empleados.adjustment;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestion.empleados.common.ApiException;
import com.gestion.empleados.common.Role;
import com.gestion.empleados.security.AppUserPrincipal;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/adjustments")
public class AdjustmentController {
    private final AdjustmentService adjustmentService;

    public AdjustmentController(AdjustmentService adjustmentService) {
        this.adjustmentService = adjustmentService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public AdjustmentResponse create(Authentication authentication, @Valid @RequestBody CreateAdjustmentRequest request) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        if (principal.getRole() == Role.EMPLOYEE && !principal.getEmployeeId().equals(request.getEmployeeId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No puedes crear ajustes para otro empleado");
        }
        return adjustmentService.create(request, principal.getEmployeeId());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<AdjustmentResponse> list(Authentication authentication,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) Long employeeId) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        if (principal.getRole() == Role.EMPLOYEE) {
            return adjustmentService.list(status, principal.getEmployeeId());
        }
        return adjustmentService.list(status, employeeId);
    }

    @PatchMapping("/{adjustmentId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public AdjustmentResponse review(Authentication authentication, @PathVariable Long adjustmentId,
                                     @Valid @RequestBody ReviewAdjustmentRequest request) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return adjustmentService.review(adjustmentId, request, principal.getEmployeeId());
    }
}
