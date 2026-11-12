package com.hms.repository;

import com.hms.entity.Appointment;
import com.hms.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByAppointmentDateTimeDesc(Long patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateTimeDesc(Long doctorId);

    /**
     * Returns every non-cancelled appointment for this doctor that could possibly
     * overlap [windowStart, windowEnd) — a coarse date-range fetch. The exact overlap
     * test (accounting for each appointment's own durationMinutes) is done in Java by
     * AppointmentServiceImpl, since expressing "start + variable duration" portably in
     * JPQL across MySQL/H2 dialects is unreliable.
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.status <> com.hms.entity.AppointmentStatus.CANCELLED " +
           "AND a.appointmentDateTime BETWEEN :rangeStart AND :rangeEnd")
    List<Appointment> findCandidatesInRange(@Param("doctorId") Long doctorId,
                                             @Param("rangeStart") LocalDateTime rangeStart,
                                             @Param("rangeEnd") LocalDateTime rangeEnd);

    List<Appointment> findByDoctorIdAndAppointmentDateTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByStatus(AppointmentStatus status);

    long countByStatus(AppointmentStatus status);
}
