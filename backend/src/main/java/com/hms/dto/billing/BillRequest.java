package com.hms.dto.billing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BillRequest {
    @NotNull(message = "Patient id is required")
    private Long patientId;

    private Long appointmentId; // optional

    @NotEmpty(message = "At least one bill item is required")
    @Valid
    private List<BillItemRequest> items;
}
