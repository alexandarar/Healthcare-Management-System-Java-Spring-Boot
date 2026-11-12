package com.hms.dto.dashboard;

import com.hms.dto.appointment.AppointmentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionistDashboardResponse {
    private long totalPatientsRegisteredToday;
    private List<AppointmentResponse> todaysAppointments;
    private long pendingBillsCount;
}
