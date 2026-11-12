package com.hms.controller;

import com.hms.dto.common.ApiResponse;
import com.hms.dto.medicalrecord.MedicalRecordRequest;
import com.hms.dto.medicalrecord.MedicalRecordResponse;
import com.hms.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> create(@Valid @RequestBody MedicalRecordRequest request,
                                                                       Authentication authentication) {
        MedicalRecordResponse response = medicalRecordService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Medical record created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ApiResponse<MedicalRecordResponse> update(@PathVariable Long id, @Valid @RequestBody MedicalRecordRequest request,
                                                       Authentication authentication) {
        return ApiResponse.of("Medical record updated", medicalRecordService.update(id, authentication.getName(), request));
    }

    @GetMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or " +
            "(hasRole('PATIENT') and @patientSecurity.isSelf(#patientId, authentication))")
    public ApiResponse<List<MedicalRecordResponse>> getByPatient(@PathVariable Long patientId, Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ApiResponse.ok(medicalRecordService.getByPatient(patientId, authentication.getName(), role));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN') or (hasRole('DOCTOR') and @doctorSecurity.isSelf(#doctorId, authentication))")
    public ApiResponse<List<MedicalRecordResponse>> getByDoctor(@PathVariable Long doctorId) {
        return ApiResponse.ok(medicalRecordService.getByDoctor(doctorId));
    }
}
