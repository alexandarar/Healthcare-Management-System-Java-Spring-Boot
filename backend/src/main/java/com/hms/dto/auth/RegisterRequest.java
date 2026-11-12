package com.hms.dto.auth;

import com.hms.entity.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Single registration payload used for every role. PATIENT-only fields are validated
 * conditionally in the service layer (Bean Validation can't easily express
 * "required only when role == PATIENT"), since ADMIN/RECEPTIONIST self-registration
 * doesn't need them and DOCTOR accounts are created by an admin via a separate endpoint.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+\\-() ]{7,20}$", message = "Phone number is invalid")
    private String phoneNumber;

    @NotNull(message = "Role is required")
    private RoleName role;

    // Patient-only fields (required when role == PATIENT)
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String bloodGroup;
    private String emergencyContactName;
    private String emergencyContactPhone;
}
