package com.hms.dto.dashboard;

import com.hms.dto.appointment.AppointmentResponse;
import com.hms.dto.billing.BillResponse;
import com.hms.dto.medicalrecord.MedicalRecordResponse;
import com.hms.dto.prescription.PrescriptionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDashboardResponse {
    private List<AppointmentResponse> upcomingAppointments;
    private List<AppointmentResponse> previousAppointments;
    private List<MedicalRecordResponse> medicalRecords;
    private List<PrescriptionResponse> prescriptions;
    private List<BillResponse> bills;
}
