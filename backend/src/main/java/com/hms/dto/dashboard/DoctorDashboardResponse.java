package com.hms.dto.dashboard;

import com.hms.dto.appointment.AppointmentResponse;
import com.hms.dto.medicalrecord.MedicalRecordResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDashboardResponse {
    private List<AppointmentResponse> todaysAppointments;
    private List<AppointmentResponse> upcomingAppointments;
    private long assignedPatientCount;
    private List<MedicalRecordResponse> recentMedicalRecords;
}
