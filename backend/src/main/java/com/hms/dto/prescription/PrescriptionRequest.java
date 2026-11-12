package com.hms.dto.prescription;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PrescriptionRequest {
    @NotNull(message = "Patient id is required")
    private Long patientId;

    private Long medicalRecordId; // optional

    @NotEmpty(message = "At least one medicine is required")
    @Valid
    private List<MedicineLineRequest> medicines;

    private String generalInstructions;
}
