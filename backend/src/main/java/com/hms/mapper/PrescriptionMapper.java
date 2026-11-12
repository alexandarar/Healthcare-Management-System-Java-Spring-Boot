package com.hms.mapper;

import com.hms.dto.prescription.MedicineLineRequest;
import com.hms.dto.prescription.PrescriptionResponse;
import com.hms.entity.Medicine;
import com.hms.entity.Prescription;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrescriptionMapper {
    public PrescriptionResponse toResponse(Prescription p) {
        List<MedicineLineRequest> meds = p.getMedicines().stream().map(this::toLine).toList();
        return PrescriptionResponse.builder()
                .id(p.getId())
                .patientId(p.getPatient().getId())
                .patientName(p.getPatient().getUser().getFullName())
                .doctorId(p.getDoctor().getId())
                .doctorName(p.getDoctor().getUser().getFullName())
                .generalInstructions(p.getGeneralInstructions())
                .medicines(meds)
                .createdAt(p.getCreatedAt())
                .build();
    }

    private MedicineLineRequest toLine(Medicine m) {
        MedicineLineRequest line = new MedicineLineRequest();
        line.setName(m.getName());
        line.setDosage(m.getDosage());
        line.setFrequency(m.getFrequency());
        line.setDuration(m.getDuration());
        line.setInstructions(m.getInstructions());
        return line;
    }
}
