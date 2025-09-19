package com.tms.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobStatusResponse {
    private LocalDate jobDate;
    private int totalPartners;
    private int successfulPartners;
    private int failedPartners;
    private List<PartnerJobStatus> partnerStatuses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PartnerJobStatus {
        private String partnerId;
        private int successfulEntities;
        private int failedEntities;
        private List<String> failedEntityTypes;
        private String overallStatus; // SUCCESS, PARTIAL, FAILED
    }
}