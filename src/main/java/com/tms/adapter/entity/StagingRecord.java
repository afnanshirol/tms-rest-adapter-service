package com.tms.adapter.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.Instant;

@Entity
@Table(name = "staging_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StagingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partner_id", nullable = false)
    private String partnerId;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "normalized_data", columnDefinition = "TEXT")
    private String normalizedData;

    @Column(name = "received_at", nullable = false)
    @Builder.Default
    private Instant receivedAt = Instant.now();
}