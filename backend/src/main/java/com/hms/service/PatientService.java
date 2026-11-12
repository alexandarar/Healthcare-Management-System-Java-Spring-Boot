package com.hms.service;

import com.hms.dto.patient.PatientResponse;
import com.hms.dto.patient.PatientUpdateRequest;

import java.util.List;

public interface PatientService {
    List<PatientResponse> getAll();
    PatientResponse getById(Long id);
    PatientResponse getByUserEmail(String email);
    List<PatientResponse> search(String term);
    PatientResponse update(Long id, PatientUpdateRequest request);
    void deactivate(Long id);
}
