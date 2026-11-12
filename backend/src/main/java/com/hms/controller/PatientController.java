package com.hms.controller;

import com.hms.dto.common.ApiResponse;
import com.hms.dto.patient.PatientResponse;
import com.hms.dto.patient.PatientUpdateRequest;
import com.hms.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public ApiResponse<List<PatientResponse>> getAll(@RequestParam(required = false) String search) {
        List<PatientResponse> patients = (search == null || search.isBlank())
                ? patientService.getAll()
                : patientService.search(search);
        return ApiResponse.ok(patients);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<PatientResponse> getMyProfile(Authentication authentication) {
        return ApiResponse.ok(patientService.getByUserEmail(authentication.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST') or " +
            "(hasRole('PATIENT') and @patientSecurity.isSelf(#id, authentication))")
    public ApiResponse<PatientResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(patientService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or " +
            "(hasRole('PATIENT') and @patientSecurity.isSelf(#id, authentication))")
    public ApiResponse<PatientResponse> update(@PathVariable Long id, @Valid @RequestBody PatientUpdateRequest request) {
        return ApiResponse.of("Patient updated", patientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        patientService.deactivate(id);
        return ApiResponse.of("Patient deactivated", null);
    }
}
