package com.hms.controller;

import com.hms.dto.billing.BillRequest;
import com.hms.dto.billing.BillResponse;
import com.hms.dto.billing.PaymentRequest;
import com.hms.dto.common.ApiResponse;
import com.hms.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BillResponse>> generateBill(@Valid @RequestBody BillRequest request) {
        BillResponse response = billingService.generateBill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Bill generated", response));
    }

    @PostMapping("/{billId}/payments")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ApiResponse<BillResponse> recordPayment(@PathVariable Long billId, @Valid @RequestBody PaymentRequest request) {
        return ApiResponse.of("Payment recorded", billingService.recordPayment(billId, request));
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<BillResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(billingService.getById(id));
    }

    @GetMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or " +
            "(hasRole('PATIENT') and @patientSecurity.isSelf(#patientId, authentication))")
    public ApiResponse<List<BillResponse>> getByPatient(@PathVariable Long patientId, Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ApiResponse.ok(billingService.getByPatient(patientId, authentication.getName(), role));
    }
}
