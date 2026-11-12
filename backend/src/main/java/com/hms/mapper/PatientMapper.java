package com.hms.mapper;

import com.hms.dto.patient.PatientResponse;
import com.hms.entity.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {
    public PatientResponse toResponse(Patient p) {
        return PatientResponse.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .fullName(p.getUser().getFullName())
                .email(p.getUser().getEmail())
                .phoneNumber(p.getUser().getPhoneNumber())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender().name())
                .address(p.getAddress())
                .bloodGroup(p.getBloodGroup())
                .emergencyContactName(p.getEmergencyContactName())
                .emergencyContactPhone(p.getEmergencyContactPhone())
                .allergies(p.getAllergies())
                .active(p.isActive())
                .build();
    }
}
