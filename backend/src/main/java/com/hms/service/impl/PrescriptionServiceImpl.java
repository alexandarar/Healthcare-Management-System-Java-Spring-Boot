package com.hms.service.impl;

import com.hms.dto.prescription.MedicineLineRequest;
import com.hms.dto.prescription.PrescriptionRequest;
import com.hms.dto.prescription.PrescriptionResponse;
import com.hms.entity.Doctor;
import com.hms.entity.MedicalRecord;
import com.hms.entity.Medicine;
import com.hms.entity.Patient;
import com.hms.entity.Prescription;
import com.hms.entity.RoleName;
import com.hms.exception.ResourceNotFoundException;
import com.hms.exception.UnauthorizedAccessException;
import com.hms.mapper.PrescriptionMapper;
import com.hms.repository.DoctorRepository;
import com.hms.repository.MedicalRecordRepository;
import com.hms.repository.PatientRepository;
import com.hms.repository.PrescriptionRepository;
import com.hms.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionMapper prescriptionMapper;

    @Override
    @Transactional
    public PrescriptionResponse create(String doctorUserEmail, PrescriptionRequest request) {
        Doctor doctor = doctorRepository.findByUserEmail(doctorUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        MedicalRecord medicalRecord = null;
        if (request.getMedicalRecordId() != null) {
            medicalRecord = medicalRecordRepository.findById(request.getMedicalRecordId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + request.getMedicalRecordId()));
            // Prevents attaching a prescription to a medical record that belongs to a
            // different patient (or a different doctor) than the one issuing it.
            if (!medicalRecord.getPatient().getId().equals(patient.getId())) {
                throw new IllegalArgumentException("That medical record does not belong to the specified patient");
            }
            if (!medicalRecord.getDoctor().getId().equals(doctor.getId())) {
                throw new IllegalArgumentException("That medical record does not belong to you");
            }
        }

        Prescription prescription = Prescription.builder()
                .patient(patient)
                .doctor(doctor)
                .medicalRecord(medicalRecord)
                .generalInstructions(request.getGeneralInstructions())
                .build();

        for (MedicineLineRequest line : request.getMedicines()) {
            Medicine medicine = Medicine.builder()
                    .prescription(prescription)
                    .name(line.getName())
                    .dosage(line.getDosage())
                    .frequency(line.getFrequency())
                    .duration(line.getDuration())
                    .instructions(line.getInstructions())
                    .build();
            prescription.getMedicines().add(medicine);
        }

        return prescriptionMapper.toResponse(prescriptionRepository.save(prescription));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getByPatient(Long patientId, String requesterEmail, String requesterRole) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        if (RoleName.PATIENT.name().equals(requesterRole) && !patient.getUser().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new UnauthorizedAccessException("You may only view your own prescriptions");
        }

        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(prescriptionMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getByDoctor(Long doctorId) {
        return prescriptionRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId)
                .stream().map(prescriptionMapper::toResponse).toList();
    }
}
