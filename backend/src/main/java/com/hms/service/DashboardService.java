package com.hms.service;

import com.hms.dto.dashboard.AdminDashboardResponse;
import com.hms.dto.dashboard.DoctorDashboardResponse;
import com.hms.dto.dashboard.PatientDashboardResponse;
import com.hms.dto.dashboard.ReceptionistDashboardResponse;

public interface DashboardService {
    AdminDashboardResponse getAdminDashboard();
    DoctorDashboardResponse getDoctorDashboard(String doctorEmail);
    PatientDashboardResponse getPatientDashboard(String patientEmail);
    ReceptionistDashboardResponse getReceptionistDashboard();
}
