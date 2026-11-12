package com.hms.service.impl;

import com.hms.dto.billing.BillItemRequest;
import com.hms.dto.billing.BillRequest;
import com.hms.dto.billing.BillResponse;
import com.hms.dto.billing.PaymentRequest;
import com.hms.entity.Appointment;
import com.hms.entity.Bill;
import com.hms.entity.BillItem;
import com.hms.entity.BillStatus;
import com.hms.entity.Patient;
import com.hms.entity.Payment;
import com.hms.entity.RoleName;
import com.hms.exception.InvalidAppointmentException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.exception.UnauthorizedAccessException;
import com.hms.mapper.BillMapper;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.BillRepository;
import com.hms.repository.PatientRepository;
import com.hms.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Generates invoices from itemized charges and records payments against them. Totals
 * are always derived by summing BillItem amounts (never a free-typed total), and status
 * transitions to PAID/PARTIALLY_PAID are computed from the sum of Payment rows — this
 * keeps the bill's state an honest reflection of its line items and payment history
 * rather than two numbers that could drift apart.
 */
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final BillRepository billRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillMapper billMapper;

    @Override
    @Transactional
    public BillResponse generateBill(BillRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + request.getAppointmentId()));
            // Prevents billing a patient for someone else's appointment.
            if (!appointment.getPatient().getId().equals(patient.getId())) {
                throw new IllegalArgumentException("That appointment does not belong to the specified patient");
            }
        }

        Bill bill = Bill.builder()
                .patient(patient)
                .appointment(appointment)
                .invoiceNumber(generateInvoiceNumber())
                .status(BillStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (BillItemRequest itemReq : request.getItems()) {
            BillItem item = BillItem.builder()
                    .bill(bill)
                    .chargeType(itemReq.getChargeType())
                    .description(itemReq.getDescription())
                    .amount(itemReq.getAmount())
                    .build();
            bill.getItems().add(item);
            total = total.add(itemReq.getAmount());
        }
        bill.setTotalAmount(total);

        return billMapper.toResponse(billRepository.save(bill));
    }

    @Override
    @Transactional
    public BillResponse recordPayment(Long billId, PaymentRequest request) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + billId));

        if (bill.getStatus() == BillStatus.CANCELLED) {
            throw new InvalidAppointmentException("Cannot record a payment against a cancelled bill");
        }

        Payment payment = Payment.builder()
                .bill(bill)
                .amount(request.getAmount())
                .method(request.getMethod())
                .transactionReference(request.getTransactionReference())
                .build();
        bill.getPayments().add(payment);

        BigDecimal totalPaid = bill.getPayments().stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(bill.getTotalAmount()) >= 0) {
            bill.setStatus(BillStatus.PAID);
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            bill.setStatus(BillStatus.PARTIALLY_PAID);
        }

        return billMapper.toResponse(billRepository.save(bill));
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
        return billMapper.toResponse(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillResponse> getByPatient(Long patientId, String requesterEmail, String requesterRole) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        if (RoleName.PATIENT.name().equals(requesterRole) && !patient.getUser().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new UnauthorizedAccessException("You may only view your own bills");
        }

        return billRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(billMapper::toResponse).toList();
    }

    private String generateInvoiceNumber() {
        String candidate;
        do {
            candidate = "INV-" + Instant.now().toEpochMilli();
        } while (billRepository.existsByInvoiceNumber(candidate));
        return candidate;
    }
}
