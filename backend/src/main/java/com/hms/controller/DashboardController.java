package com.hms.controller;

import com.hms.dto.common.ApiResponse;
import com.hms.dto.dashboard.AdminDashboardResponse;
import com.hms.dto.dashboard.DoctorDashboardResponse;
import com.hms.dto.dashboard.PatientDashboardResponse;
import com.hms.dto.dashboard.ReceptionistDashboardResponse;
import com.hms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminDashboardResponse> admin() {
        return ApiResponse.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public ApiResponse<DoctorDashboardResponse> doctor(Authentication authentication) {
        return ApiResponse.ok(dashboardService.getDoctorDashboard(authentication.getName()));
    }

    @GetMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<PatientDashboardResponse> patient(Authentication authentication) {
        return ApiResponse.ok(dashboardService.getPatientDashboard(authentication.getName()));
    }

    @GetMapping("/receptionist")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ApiResponse<ReceptionistDashboardResponse> receptionist() {
        return ApiResponse.ok(dashboardService.getReceptionistDashboard());
    }
}
