package com.hms.service;

import com.hms.dto.appointment.AppointmentRequest;
import com.hms.entity.Doctor;
import com.hms.entity.Patient;
import com.hms.entity.User;
import com.hms.entity.RoleName;
import com.hms.exception.InvalidAppointmentException;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.PatientRepository;
import com.hms.mapper.AppointmentMapper;
import com.hms.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Verifies the double-booking guard in AppointmentServiceImpl.book(): two overlapping
 * requests for the same doctor must not both succeed. This is the single most
 * important business rule in appointment scheduling (spec: "Prevent double-booking").
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        User doctorUser = User.builder().fullName("Dr. Smith").email("dr@hms.local").role(RoleName.DOCTOR).build();
        doctorUser.setId(1L);
        doctor = Doctor.builder().user(doctorUser).specialization("Cardiology").active(true).build();
        doctor.setId(1L);

        User patientUser = User.builder().fullName("John Doe").email("john@hms.local").role(RoleName.PATIENT).build();
        patientUser.setId(2L);
        patient = Patient.builder().user(patientUser).build();
        patient.setId(1L);
    }

    @Test
    void bookingOverlappingSlotForSameDoctorThrows() {
        AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(1L);
        request.setDoctorId(1L);
        LocalDateTime slot = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        request.setAppointmentDateTime(slot);
        request.setReason("Checkup");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        com.hms.entity.Appointment existing = com.hms.entity.Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDateTime(slot.plusMinutes(10)) // overlaps the 30-min window
                .durationMinutes(30)
                .reason("Existing")
                .status(com.hms.entity.AppointmentStatus.BOOKED)
                .build();
        existing.setId(99L);

        when(appointmentRepository.findCandidatesInRange(eq(1L), any(), any()))
                .thenReturn(List.of(existing));

        assertThrows(InvalidAppointmentException.class, () -> appointmentService.book(request));
    }

    @Test
    void bookingNonOverlappingSlotSucceeds() {
        AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(1L);
        request.setDoctorId(1L);
        LocalDateTime slot = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        request.setAppointmentDateTime(slot);
        request.setReason("Checkup");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findCandidatesInRange(eq(1L), any(), any())).thenReturn(List.of());
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(appointmentMapper.toResponse(any())).thenReturn(null);

        appointmentService.book(request); // should not throw
    }
}
