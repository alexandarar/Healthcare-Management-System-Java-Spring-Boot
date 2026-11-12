package com.hms.security;

import com.hms.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Backs the @patientSecurity.isSelf(...) SpEL check used in @PreAuthorize. Lets a
 * PATIENT-role request through only when the {id} path variable actually resolves to
 * their own Patient row — the concrete enforcement of "a patient can only access their
 * own data" at the URL-path level (service-layer checks reinforce this for nested reads).
 */
@Component("patientSecurity")
@RequiredArgsConstructor
public class PatientSecurity {

    private final PatientRepository patientRepository;

    public boolean isSelf(Long patientId, Authentication authentication) {
        if (authentication == null || patientId == null) return false;
        return patientRepository.findById(patientId)
                .map(p -> p.getUser().getEmail().equalsIgnoreCase(authentication.getName()))
                .orElse(false);
    }
}
