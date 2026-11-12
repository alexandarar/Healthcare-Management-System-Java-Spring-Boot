package com.hms.mapper;

import com.hms.dto.appointment.AppointmentResponse;
import com.hms.entity.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {
    public AppointmentResponse toResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getUser().getFullName())
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getUser().getFullName())
                .doctorSpecialization(a.getDoctor().getSpecialization())
                .appointmentDateTime(a.getAppointmentDateTime())
                .durationMinutes(a.getDurationMinutes())
                .reason(a.getReason())
                .status(a.getStatus())
                .notes(a.getNotes())
                .build();
    }
}
