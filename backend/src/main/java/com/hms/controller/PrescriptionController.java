package com.hms.controller;

import com.hms.dto.common.ApiResponse;
import com.hms.dto.prescription.PrescriptionRequest;
import com.hms.dto.prescription.PrescriptionResponse;
import com.hms.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> create(@Valid @RequestBody PrescriptionRequest request,
                                                                      Authentication authentication) {
        PrescriptionResponse response = prescriptionService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Prescription created", response));
    }

    @GetMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or " +
            "(hasRole('PATIENT') and @patientSecurity.isSelf(#patientId, authentication))")
    public ApiResponse<List<PrescriptionResponse>> getByPatient(@PathVariable Long patientId, Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ApiResponse.ok(prescriptionService.getByPatient(patientId, authentication.getName(), role));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN') or (hasRole('DOCTOR') and @doctorSecurity.isSelf(#doctorId, authentication))")
    public ApiResponse<List<PrescriptionResponse>> getByDoctor(@PathVariable Long doctorId) {
        return ApiResponse.ok(prescriptionService.getByDoctor(doctorId));
    }
}
