package com.hms.dto.patient;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Fields a receptionist/admin/patient may update after registration. */
@Data
public class PatientUpdateRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    private String address;

    private String bloodGroup;

    @NotBlank(message = "Emergency contact name is required")
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact phone is required")
    private String emergencyContactPhone;

    private String allergies;
}
