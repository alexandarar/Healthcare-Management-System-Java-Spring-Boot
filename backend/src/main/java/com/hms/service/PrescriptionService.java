package com.hms.service;

import com.hms.dto.prescription.PrescriptionRequest;
import com.hms.dto.prescription.PrescriptionResponse;

import java.util.List;

public interface PrescriptionService {
    PrescriptionResponse create(String doctorUserEmail, PrescriptionRequest request);
    List<PrescriptionResponse> getByPatient(Long patientId, String requesterEmail, String requesterRole);
    List<PrescriptionResponse> getByDoctor(Long doctorId);
}
