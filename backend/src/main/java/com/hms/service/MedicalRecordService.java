package com.hms.service;

import com.hms.dto.medicalrecord.MedicalRecordRequest;
import com.hms.dto.medicalrecord.MedicalRecordResponse;

import java.util.List;

public interface MedicalRecordService {
    MedicalRecordResponse create(Long doctorUserEmail, MedicalRecordRequest request);
    MedicalRecordResponse update(Long id, String requesterEmail, MedicalRecordRequest request);
    List<MedicalRecordResponse> getByPatient(Long patientId, String requesterEmail, String requesterRole);
    List<MedicalRecordResponse> getByDoctor(Long doctorId);
}
