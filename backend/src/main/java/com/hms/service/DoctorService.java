package com.hms.service;

import com.hms.dto.doctor.DoctorCreateRequest;
import com.hms.dto.doctor.DoctorResponse;
import com.hms.dto.doctor.DoctorUpdateRequest;

import java.util.List;

public interface DoctorService {
    DoctorResponse create(DoctorCreateRequest request);
    List<DoctorResponse> getAll();
    DoctorResponse getById(Long id);
    DoctorResponse getByUserEmail(String email);
    List<DoctorResponse> searchBySpecialization(String specialization);
    DoctorResponse update(Long id, DoctorUpdateRequest request);
    void deactivate(Long id);
}
