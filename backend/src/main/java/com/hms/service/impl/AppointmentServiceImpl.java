package com.hms.service.impl;

import com.hms.dto.appointment.AppointmentRequest;
import com.hms.dto.appointment.AppointmentRescheduleRequest;
import com.hms.dto.appointment.AppointmentResponse;
import com.hms.dto.appointment.AppointmentStatusUpdateRequest;
import com.hms.entity.Appointment;
import com.hms.entity.AppointmentStatus;
import com.hms.entity.Doctor;
import com.hms.entity.Patient;
import com.hms.exception.InvalidAppointmentException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.mapper.AppointmentMapper;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.PatientRepository;
import com.hms.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Owns the appointment lifecycle (BOOKED -> CONFIRMED -> COMPLETED, or -> CANCELLED at
 * any point) and the double-booking guard. The overlap check is done here rather than
 * as a DB constraint because "overlap" depends on variable slot duration, which a simple
 * unique index on (doctor_id, datetime) can't express.
 */
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    @Transactional
    public AppointmentResponse book(AppointmentRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));

        if (!doctor.isActive()) {
            throw new InvalidAppointmentException("This doctor is not currently accepting appointments");
        }

        int durationMinutes = 30;
        assertSlotAvailable(doctor.getId(), request.getAppointmentDateTime(), durationMinutes, null);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDateTime(request.getAppointmentDateTime())
                .durationMinutes(durationMinutes)
                .reason(request.getReason())
                .status(AppointmentStatus.BOOKED)
                .build();

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponse reschedule(Long id, AppointmentRescheduleRequest request) {
        Appointment appointment = findAppointment(id);

        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentException("Cannot reschedule an appointment that is " + appointment.getStatus());
        }

        assertSlotAvailable(appointment.getDoctor().getId(), request.getAppointmentDateTime(),
                appointment.getDurationMinutes(), appointment.getId());

        appointment.setAppointmentDateTime(request.getAppointmentDateTime());
        appointment.setStatus(AppointmentStatus.BOOKED);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponse updateStatus(Long id, AppointmentStatusUpdateRequest request) {
        Appointment appointment = findAppointment(id);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentException("Cannot change the status of a cancelled appointment");
        }

        appointment.setStatus(request.getStatus());
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Appointment appointment = findAppointment(id);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentException("Cannot cancel an appointment that is already completed");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long id) {
        return appointmentMapper.toResponse(findAppointment(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAll() {
        return appointmentRepository.findAll().stream().map(appointmentMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getByPatient(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateTimeDesc(patientId)
                .stream().map(appointmentMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateTimeDesc(doctorId)
                .stream().map(appointmentMapper::toResponse).toList();
    }

    /**
     * Guards against double-booking: any existing, non-cancelled appointment for this
     * doctor whose [start, start+duration) window intersects the requested window is
     * rejected. excludeAppointmentId lets reschedule() check without colliding with
     * the appointment being moved.
     */
    private void assertSlotAvailable(Long doctorId, LocalDateTime start, int durationMinutes, Long excludeAppointmentId) {
        LocalDateTime newEnd = start.plusMinutes(durationMinutes);
        // Widen the candidate fetch window by a day on each side so any appointment whose
        // own [start, start+duration) could touch ours is included, then narrow in Java.
        List<Appointment> candidates = appointmentRepository.findCandidatesInRange(
                doctorId, start.minusDays(1), newEnd.plusDays(1));

        boolean conflict = candidates.stream()
                .filter(a -> excludeAppointmentId == null || !a.getId().equals(excludeAppointmentId))
                .anyMatch(a -> {
                    LocalDateTime existingStart = a.getAppointmentDateTime();
                    LocalDateTime existingEnd = existingStart.plusMinutes(a.getDurationMinutes());
                    return start.isBefore(existingEnd) && existingStart.isBefore(newEnd);
                });

        if (conflict) {
            throw new InvalidAppointmentException("This doctor already has an appointment overlapping that time slot");
        }
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }
}
