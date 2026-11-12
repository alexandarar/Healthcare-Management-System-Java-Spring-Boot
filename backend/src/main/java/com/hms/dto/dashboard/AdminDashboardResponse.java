package com.hms.dto.dashboard;

import com.hms.dto.appointment.AppointmentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;
    private BigDecimal totalRevenue;
    private List<AppointmentResponse> recentAppointments;
}
