package com.hms.controller;

import com.hms.dto.common.ApiResponse;
import com.hms.dto.doctor.DoctorCreateRequest;
import com.hms.dto.doctor.DoctorResponse;
import com.hms.dto.doctor.DoctorUpdateRequest;
import com.hms.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> create(@Valid @RequestBody DoctorCreateRequest request) {
        DoctorResponse response = doctorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Doctor created", response));
    }

    @GetMapping
    public ApiResponse<List<DoctorResponse>> getAll(@RequestParam(required = false) String specialization) {
        List<DoctorResponse> doctors = (specialization == null || specialization.isBlank())
                ? doctorService.getAll()
                : doctorService.searchBySpecialization(specialization);
        return ApiResponse.ok(doctors);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public ApiResponse<DoctorResponse> getMyProfile(Authentication authentication) {
        return ApiResponse.ok(doctorService.getByUserEmail(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ApiResponse<DoctorResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(doctorService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @doctorSecurity.isSelf(#id, authentication))")
    public ApiResponse<DoctorResponse> update(@PathVariable Long id, @Valid @RequestBody DoctorUpdateRequest request) {
        return ApiResponse.of("Doctor updated", doctorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        doctorService.deactivate(id);
        return ApiResponse.of("Doctor deactivated", null);
    }
}
