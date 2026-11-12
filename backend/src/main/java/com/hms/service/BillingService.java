package com.hms.service;

import com.hms.dto.billing.BillRequest;
import com.hms.dto.billing.BillResponse;
import com.hms.dto.billing.PaymentRequest;

import java.util.List;

public interface BillingService {
    BillResponse generateBill(BillRequest request);
    BillResponse recordPayment(Long billId, PaymentRequest request);
    BillResponse getById(Long id);
    List<BillResponse> getByPatient(Long patientId, String requesterEmail, String requesterRole);
}
