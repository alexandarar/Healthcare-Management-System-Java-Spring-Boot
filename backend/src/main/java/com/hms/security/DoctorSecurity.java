package com.hms.security;

import com.hms.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("doctorSecurity")
@RequiredArgsConstructor
public class DoctorSecurity {

    private final DoctorRepository doctorRepository;

    public boolean isSelf(Long doctorId, Authentication authentication) {
        if (authentication == null || doctorId == null) return false;
        return doctorRepository.findById(doctorId)
                .map(d -> d.getUser().getEmail().equalsIgnoreCase(authentication.getName()))
                .orElse(false);
    }
}
