package com.hms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One drug line item within a Prescription. Many-to-one back to Prescription (not a
 * standalone catalog) because this system tracks what was prescribed, not a shared
 * drug-inventory catalog — that would be a separate concern (pharmacy stock management).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "medicines")
public class Medicine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String dosage; // e.g. "500mg"

    @Column(nullable = false)
    private String frequency; // e.g. "Twice daily"

    @Column(nullable = false)
    private String duration; // e.g. "5 days"

    @Column
    private String instructions; // e.g. "After food"
}
