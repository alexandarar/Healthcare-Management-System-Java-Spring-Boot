package com.hms.service;

import com.hms.dto.appointment.AppointmentRequest;
import com.hms.dto.appointment.AppointmentRescheduleRequest;
import com.hms.dto.appointment.AppointmentResponse;
import com.hms.dto.appointment.AppointmentStatusUpdateRequest;

import java.util.List;

public interface AppointmentService {
    AppointmentResponse book(AppointmentRequest request);
    AppointmentResponse reschedule(Long id, AppointmentRescheduleRequest request);
    AppointmentResponse updateStatus(Long id, AppointmentStatusUpdateRequest request);
    void cancel(Long id);
    AppointmentResponse getById(Long id);
    List<AppointmentResponse> getAll();
    List<AppointmentResponse> getByPatient(Long patientId);
    List<AppointmentResponse> getByDoctor(Long doctorId);
}
