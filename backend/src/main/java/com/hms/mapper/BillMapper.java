package com.hms.mapper;

import com.hms.dto.billing.BillItemRequest;
import com.hms.dto.billing.BillResponse;
import com.hms.entity.Bill;
import com.hms.entity.BillItem;
import com.hms.entity.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BillMapper {
    public BillResponse toResponse(Bill b) {
        List<BillItemRequest> items = b.getItems().stream().map(this::toItem).toList();
        BigDecimal paid = b.getPayments().stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return BillResponse.builder()
                .id(b.getId())
                .invoiceNumber(b.getInvoiceNumber())
                .patientId(b.getPatient().getId())
                .patientName(b.getPatient().getUser().getFullName())
                .appointmentId(b.getAppointment() != null ? b.getAppointment().getId() : null)
                .totalAmount(b.getTotalAmount())
                .amountPaid(paid)
                .balanceDue(b.getTotalAmount().subtract(paid))
                .status(b.getStatus())
                .items(items)
                .createdAt(b.getCreatedAt())
                .build();
    }

    private BillItemRequest toItem(BillItem i) {
        BillItemRequest item = new BillItemRequest();
        item.setChargeType(i.getChargeType());
        item.setDescription(i.getDescription());
        item.setAmount(i.getAmount());
        return item;
    }
}
