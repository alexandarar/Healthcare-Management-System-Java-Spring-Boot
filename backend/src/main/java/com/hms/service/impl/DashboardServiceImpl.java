package com.hms.service.impl;

import com.hms.dto.appointment.AppointmentResponse;
import com.hms.dto.dashboard.AdminDashboardResponse;
import com.hms.dto.dashboard.DoctorDashboardResponse;
import com.hms.dto.dashboard.PatientDashboardResponse;
import com.hms.dto.dashboard.ReceptionistDashboardResponse;
import com.hms.entity.Appointment;
import com.hms.entity.AppointmentStatus;
import com.hms.entity.BillStatus;
import com.hms.entity.Doctor;
import com.hms.entity.Patient;
import com.hms.exception.ResourceNotFoundException;
import com.hms.mapper.AppointmentMapper;
import com.hms.mapper.MedicalRecordMapper;
import com.hms.mapper.PrescriptionMapper;
import com.hms.mapper.BillMapper;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.BillRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.MedicalRecordRepository;
import com.hms.repository.PatientRepository;
import com.hms.repository.PrescriptionRepository;
import com.hms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final BillRepository billRepository;

    private final AppointmentMapper appointmentMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final BillMapper billMapper;

    @Override
    public AdminDashboardResponse getAdminDashboard() {
        List<AppointmentResponse> recent = appointmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Appointment::getCreatedAt).reversed())
                .limit(10)
                .map(appointmentMapper::toResponse)
                .toList();

        BigDecimal revenue = billRepository.sumPaidRevenue();

        return AdminDashboardResponse.builder()
                .totalPatients(patientRepository.count())
                .totalDoctors(doctorRepository.count())
                .totalAppointments(appointmentRepository.count())
                .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
                .recentAppointments(recent)
                .build();
    }

    @Override
    public DoctorDashboardResponse getDoctorDashboard(String doctorEmail) {
        Doctor doctor = doctorRepository.findByUserEmail(doctorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        List<Appointment> all = appointmentRepository.findByDoctorIdOrderByAppointmentDateTimeDesc(doctor.getId());
        LocalDate today = LocalDate.now();

        List<AppointmentResponse> todays = all.stream()
                .filter(a -> a.getAppointmentDateTime().toLocalDate().isEqual(today))
                .map(appointmentMapper::toResponse)
                .toList();

        List<AppointmentResponse> upcoming = all.stream()
                .filter(a -> a.getAppointmentDateTime().isAfter(LocalDateTime.now())
                        && a.getStatus() != com.hms.entity.AppointmentStatus.CANCELLED)
                .sorted(Comparator.comparing(Appointment::getAppointmentDateTime))
                .limit(10)
                .map(appointmentMapper::toResponse)
                .toList();

        long assignedPatients = all.stream().map(a -> a.getPatient().getId()).distinct().count();

        var recentRecords = medicalRecordRepository.findByDoctorIdOrderByCreatedAtDesc(doctor.getId())
                .stream().limit(10).map(medicalRecordMapper::toResponse).toList();

        return DoctorDashboardResponse.builder()
                .todaysAppointments(todays)
                .upcomingAppointments(upcoming)
                .assignedPatientCount(assignedPatients)
                .recentMedicalRecords(recentRecords)
                .build();
    }

    @Override
    public PatientDashboardResponse getPatientDashboard(String patientEmail) {
        Patient patient = patientRepository.findByUserEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        List<Appointment> all = appointmentRepository.findByPatientIdOrderByAppointmentDateTimeDesc(patient.getId());
        LocalDateTime now = LocalDateTime.now();

        List<AppointmentResponse> upcoming = all.stream()
                .filter(a -> a.getAppointmentDateTime().isAfter(now) && a.getStatus() != AppointmentStatus.CANCELLED)
                .sorted(Comparator.comparing(Appointment::getAppointmentDateTime))
                .map(appointmentMapper::toResponse)
                .toList();

        List<AppointmentResponse> previous = all.stream()
                .filter(a -> !a.getAppointmentDateTime().isAfter(now) || a.getStatus() == AppointmentStatus.COMPLETED)
                .map(appointmentMapper::toResponse)
                .toList();

        var records = medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId())
                .stream().map(medicalRecordMapper::toResponse).toList();

        var prescriptions = prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId())
                .stream().map(prescriptionMapper::toResponse).toList();

        var bills = billRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId())
                .stream().map(billMapper::toResponse).toList();

        return PatientDashboardResponse.builder()
                .upcomingAppointments(upcoming)
                .previousAppointments(previous)
                .medicalRecords(records)
                .prescriptions(prescriptions)
                .bills(bills)
                .build();
    }

    @Override
    public ReceptionistDashboardResponse getReceptionistDashboard() {
        LocalDate today = LocalDate.now();

        List<AppointmentResponse> todays = appointmentRepository.findAll().stream()
                .filter(a -> a.getAppointmentDateTime().toLocalDate().isEqual(today))
                .map(appointmentMapper::toResponse)
                .toList();

        long registeredToday = patientRepository.findAll().stream()
                .filter(p -> p.getCreatedAt().toLocalDate().isEqual(today))
                .count();

        long pendingBills = billRepository.countByStatus(BillStatus.PENDING);

        return ReceptionistDashboardResponse.builder()
                .totalPatientsRegisteredToday(registeredToday)
                .todaysAppointments(todays)
                .pendingBillsCount(pendingBills)
                .build();
    }
}
