package com.hms.service.impl;

import com.hms.dto.auth.AuthResponse;
import com.hms.dto.auth.LoginRequest;
import com.hms.dto.auth.RegisterRequest;
import com.hms.entity.Patient;
import com.hms.entity.RoleName;
import com.hms.entity.User;
import com.hms.exception.DuplicateResourceException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.repository.PatientRepository;
import com.hms.repository.UserRepository;
import com.hms.security.CustomUserDetails;
import com.hms.security.JwtService;
import com.hms.service.AuthService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles registration and login for every role. Public self-registration
 * (RoleName.PATIENT, and technically ADMIN/RECEPTIONIST if the frontend exposes it)
 * goes through here; DOCTOR accounts are intentionally NOT registerable through this
 * public endpoint — they're created by an ADMIN via DoctorService.create, so a random
 * visitor can't grant themselves clinical privileges by picking "DOCTOR" at signup.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }
        if (request.getRole() == RoleName.DOCTOR) {
            throw new IllegalArgumentException("Doctor accounts must be created by an administrator");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .enabled(true)
                .build();
        user = userRepository.save(user);

        if (request.getRole() == RoleName.PATIENT) {
            validatePatientFields(request);
            Patient patient = Patient.builder()
                    .user(user)
                    .dateOfBirth(request.getDateOfBirth())
                    .gender(parseGender(request.getGender()))
                    .address(request.getAddress())
                    .bloodGroup(request.getBloodGroup())
                    .emergencyContactName(request.getEmergencyContactName())
                    .emergencyContactPhone(request.getEmergencyContactPhone())
                    .active(true)
                    .build();
            patientRepository.save(patient);
        }

        return buildAuthResponse(user);
    }

    private void validatePatientFields(RegisterRequest r) {
        if (r.getDateOfBirth() == null || r.getGender() == null || r.getAddress() == null
                || r.getEmergencyContactName() == null || r.getEmergencyContactPhone() == null) {
            throw new IllegalArgumentException(
                    "dateOfBirth, gender, address, emergencyContactName and emergencyContactPhone are required for patient registration");
        }
        // @Past on the DTO already rejects future dates; this catches the other end of
        // the same logical bug — an implausible birth date (e.g. 300 years ago from a
        // typo'd year) that is technically "in the past" but not a real human age.
        if (r.getDateOfBirth().isBefore(java.time.LocalDate.now().minusYears(120))) {
            throw new IllegalArgumentException("Date of birth is not valid — please check the year");
        }
    }

    private Patient.Gender parseGender(String gender) {
        try {
            return Patient.Gender.valueOf(gender.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Gender must be one of MALE, FEMALE, OTHER");
        }
    }

    @Override
    public AuthResponse login(@NotNull LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return buildAuthResponse(userDetails.getUser());
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new IllegalArgumentException("Refresh token is invalid or expired");
        }
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails, user.getId(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
