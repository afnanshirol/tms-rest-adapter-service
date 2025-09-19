package com.tms.adapter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "job_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_date", nullable = false)
    private LocalDate jobDate;

    @Column(name = "partner_id", nullable = false)
    private String partnerId;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "status", nullable = false)
    private String status; // SUCCESS, FAILED

    @Column(name = "records_processed")
    @Builder.Default
    private Integer recordsProcessed = 0;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "execution_time", nullable = false)
    @Builder.Default
    private Instant executionTime = Instant.now();

    @Column(name = "duration_ms")
    private Long durationMs;
}