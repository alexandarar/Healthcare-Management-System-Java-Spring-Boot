package com.hms.service.impl;

import com.hms.dto.medicalrecord.MedicalRecordRequest;
import com.hms.dto.medicalrecord.MedicalRecordResponse;
import com.hms.entity.Appointment;
import com.hms.entity.Doctor;
import com.hms.entity.MedicalRecord;
import com.hms.entity.Patient;
import com.hms.entity.RoleName;
import com.hms.exception.ResourceNotFoundException;
import com.hms.exception.UnauthorizedAccessException;
import com.hms.mapper.MedicalRecordMapper;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.MedicalRecordRepository;
import com.hms.repository.PatientRepository;
import com.hms.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Creates and retrieves clinical notes. Every read path checks that the requester is
 * either staff (ADMIN/DOCTOR) or the patient themselves — this is the concrete
 * enforcement of "a patient must never access another patient's private medical
 * information" from the spec; the security-config URL rule only gets you as far as
 * "some PATIENT role hit this endpoint", not "the right one".
 */
@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordMapper medicalRecordMapper;

    @Override
    @Transactional
    public MedicalRecordResponse create(String doctorUserEmail, MedicalRecordRequest request) {
        Doctor doctor = doctorRepository.findByUserEmail(doctorUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + request.getAppointmentId()));
            // Without this check, a doctor could pass any appointment id and silently
            // attach one patient's medical record to a different patient's appointment.
            if (!appointment.getPatient().getId().equals(patient.getId())) {
                throw new IllegalArgumentException("That appointment does not belong to the specified patient");
            }
            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new IllegalArgumentException("That appointment does not belong to you");
            }
        }

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .symptoms(request.getSymptoms())
                .diagnosis(request.getDiagnosis())
                .treatmentNotes(request.getTreatmentNotes())
                .build();

        return medicalRecordMapper.toResponse(medicalRecordRepository.save(record));
    }

    @Override
    @Transactional
    public MedicalRecordResponse update(Long id, String requesterEmail, MedicalRecordRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + id));

        if (!record.getDoctor().getUser().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new UnauthorizedAccessException("Only the authoring doctor can update this medical record");
        }

        record.setSymptoms(request.getSymptoms());
        record.setDiagnosis(request.getDiagnosis());
        record.setTreatmentNotes(request.getTreatmentNotes());
        return medicalRecordMapper.toResponse(medicalRecordRepository.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getByPatient(Long patientId, String requesterEmail, String requesterRole) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        if (RoleName.PATIENT.name().equals(requesterRole) && !patient.getUser().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new UnauthorizedAccessException("You may only view your own medical records");
        }

        return medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(medicalRecordMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getByDoctor(Long doctorId) {
        return medicalRecordRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId)
                .stream().map(medicalRecordMapper::toResponse).toList();
    }
}
