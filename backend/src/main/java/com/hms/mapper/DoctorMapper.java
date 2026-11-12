package com.hms.mapper;

import com.hms.dto.doctor.DoctorResponse;
import com.hms.entity.Doctor;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {
    public DoctorResponse toResponse(Doctor d) {
        return DoctorResponse.builder()
                .id(d.getId())
                .userId(d.getUser().getId())
                .fullName(d.getUser().getFullName())
                .email(d.getUser().getEmail())
                .phoneNumber(d.getUser().getPhoneNumber())
                .specialization(d.getSpecialization())
                .qualification(d.getQualification())
                .yearsOfExperience(d.getYearsOfExperience())
                .consultationFee(d.getConsultationFee())
                .availability(d.getAvailability())
                .active(d.isActive())
                .build();
    }
}
