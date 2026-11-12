package com.hms.config;

import com.hms.entity.Doctor;
import com.hms.entity.RoleName;
import com.hms.entity.User;
import com.hms.repository.DoctorRepository;
import com.hms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds two accounts on first startup: one ADMIN and one DOCTOR.
 *
 * The admin account is the only way into the system without manual SQL. The doctor
 * account exists for a concrete, previously-missing reason: DOCTOR accounts are
 * intentionally NOT self-registerable (see AuthServiceImpl — only an ADMIN can create
 * one, via POST /api/doctors). Without a seeded doctor, a fresh clone of this project
 * has no doctor account reachable until someone logs in as admin and manually creates
 * one first — a dead end that reads as "doctor login doesn't work" even though the
 * login endpoint itself is role-agnostic and works fine once a doctor account exists.
 * Seeding one demo doctor removes that dead end while keeping every other user
 * (patients, receptionists, additional doctors) created through the app itself, not
 * fake data standing in for real functionality.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedDemoDoctor();
    }

    private void seedAdmin() {
        String adminEmail = "admin@hms.local";
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .fullName("System Administrator")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin@123"))
                    .phoneNumber("0000000000")
                    .role(RoleName.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }
    }

    private void seedDemoDoctor() {
        String doctorEmail = "doctor@hms.local";
        if (!userRepository.existsByEmail(doctorEmail)) {
            User doctorUser = User.builder()
                    .fullName("Dr. Sarah Chen")
                    .email(doctorEmail)
                    .password(passwordEncoder.encode("Doctor@123"))
                    .phoneNumber("0000000001")
                    .role(RoleName.DOCTOR)
                    .enabled(true)
                    .build();
            doctorUser = userRepository.save(doctorUser);

            Doctor doctor = Doctor.builder()
                    .user(doctorUser)
                    .specialization("General Medicine")
                    .qualification("MBBS, MD")
                    .yearsOfExperience(10)
                    .consultationFee(new BigDecimal("100.00"))
                    .availability("MON-FRI 09:00-17:00")
                    .active(true)
                    .build();
            doctorRepository.save(doctor);
        }
    }
}
