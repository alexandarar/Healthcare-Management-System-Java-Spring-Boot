package com.hms.repository;

import com.hms.entity.Bill;
import com.hms.entity.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    boolean existsByInvoiceNumber(String invoiceNumber);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.status = com.hms.entity.BillStatus.PAID")
    BigDecimal sumPaidRevenue();

    long countByStatus(BillStatus status);
}
