package com.company.crm_backend.lead.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table (name = "duplicate_leads")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "duplicate_id")
    private Long duplicateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_lead_id", nullable = false)
    private Lead originalLead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duplicate_lead_id", nullable = false)
    private Lead duplicateLead;

    @Column(name = "similarity_score", precision = 5, scale = 2)
    private BigDecimal similarityScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}