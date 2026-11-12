package com.hms.dto.appointment;

import com.hms.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private AppointmentStatus status;

    private String notes;
}
