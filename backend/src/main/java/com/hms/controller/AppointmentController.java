package com.hms.controller;

import com.hms.dto.appointment.AppointmentRequest;
import com.hms.dto.appointment.AppointmentRescheduleRequest;
import com.hms.dto.appointment.AppointmentResponse;
import com.hms.dto.appointment.AppointmentStatusUpdateRequest;
import com.hms.dto.common.ApiResponse;
import com.hms.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> book(@Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.book(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Appointment booked", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ApiResponse<List<AppointmentResponse>> getAll() {
        return ApiResponse.ok(appointmentService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<AppointmentResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(appointmentService.getById(id));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST') or " +
            "(hasRole('PATIENT') and @patientSecurity.isSelf(#patientId, authentication))")
    public ApiResponse<List<AppointmentResponse>> getByPatient(@PathVariable Long patientId) {
        return ApiResponse.ok(appointmentService.getByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or " +
            "(hasRole('DOCTOR') and @doctorSecurity.isSelf(#doctorId, authentication))")
    public ApiResponse<List<AppointmentResponse>> getByDoctor(@PathVariable Long doctorId) {
        return ApiResponse.ok(appointmentService.getByDoctor(doctorId));
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    public ApiResponse<AppointmentResponse> reschedule(@PathVariable Long id, @Valid @RequestBody AppointmentRescheduleRequest request) {
        return ApiResponse.of("Appointment rescheduled", appointmentService.reschedule(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public ApiResponse<AppointmentResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody AppointmentStatusUpdateRequest request) {
        return ApiResponse.of("Appointment status updated", appointmentService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        appointmentService.cancel(id);
        return ApiResponse.of("Appointment cancelled", null);
    }
}
