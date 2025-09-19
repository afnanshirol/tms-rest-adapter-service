package com.tms.adapter.service;

import com.tms.adapter.client.ConfigServiceClient;
import com.tms.adapter.client.PartnerApiClient;
import com.tms.adapter.dto.PartnerConfig;
import com.tms.adapter.entity.JobExecution;
import com.tms.adapter.entity.StagingRecord;
import com.tms.adapter.repository.JobExecutionRepository;
import com.tms.adapter.repository.StagingRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerPollingService {

    private final ConfigServiceClient configServiceClient;
    private final PartnerApiClient partnerApiClient;
    private final StagingRecordRepository stagingRecordRepository;
    private final JobExecutionRepository jobExecutionRepository;
    private final DataNormalizationService dataNormalizationService;

    @Scheduled(cron = "0 0 2 * * *")
    public void pollAllPartners() {
        List<PartnerConfig> activePartners = configServiceClient.getActivePartners();

        for (PartnerConfig partner : activePartners) {
            pollPartnerData(partner.getPartnerId());
        }
    }

    private void pollPartnerData(String partnerId) {
        log.info("Starting {} data polling", partnerId);
        LocalDate today = LocalDate.now();

        // Track individual entity type polling
        pollEntityData(partnerId, "THEATRE", today);
        pollEntityData(partnerId, "HALL", today);
        pollEntityData(partnerId, "SHOW", today);

        // Log overall partner summary
        long successCount = jobExecutionRepository.findByPartnerIdAndJobDateOrderByExecutionTimeDesc(partnerId, today)
                .stream().filter(je -> "SUCCESS".equals(je.getStatus())).count();
        long failureCount = jobExecutionRepository.findByPartnerIdAndJobDateOrderByExecutionTimeDesc(partnerId, today)
                .stream().filter(je -> "FAILED".equals(je.getStatus())).count();

        log.info("Partner {} daily summary: {} success, {} failures", partnerId, successCount, failureCount);
    }

    private void pollEntityData(String partnerId, String entityType, LocalDate jobDate) {
        Instant startTime = Instant.now();

        try {
            log.info("Fetching {} data for partner {}", entityType, partnerId);

            String data = switch (entityType) {
                case "THEATRE" -> partnerApiClient.fetchTheatreData(partnerId);
                case "HALL" -> partnerApiClient.fetchHallData(partnerId);
                case "SHOW" -> partnerApiClient.fetchShowData(partnerId);
                default -> throw new IllegalArgumentException("Unknown entity type: " + entityType);
            };

            saveToStaging(partnerId, entityType, data);

            // Record successful execution
            recordJobExecution(partnerId, entityType, jobDate, "SUCCESS", 1, null, startTime);

            log.info("Successfully completed {} polling for {}", entityType, partnerId);

        } catch (Exception e) {
            // Record failed execution
            recordJobExecution(partnerId, entityType, jobDate, "FAILED", 0, e.getMessage(), startTime);

            log.error("{} polling failed for partner {}: {}", entityType, partnerId, e.getMessage(), e);
        }
    }

    private void recordJobExecution(String partnerId, String entityType, LocalDate jobDate,
                                  String status, int recordsProcessed, String errorMessage, Instant startTime) {
        long durationMs = Instant.now().toEpochMilli() - startTime.toEpochMilli();

        JobExecution jobExecution = JobExecution.builder()
                .jobDate(jobDate)
                .partnerId(partnerId)
                .entityType(entityType)
                .status(status)
                .recordsProcessed(recordsProcessed)
                .errorMessage(errorMessage)
                .durationMs(durationMs)
                .build();

        jobExecutionRepository.save(jobExecution);
    }

    private void saveToStaging(String partnerId, String entityType, String rawData) {
        try {
            // Normalize the data (this will throw exception if invalid)
            String normalizedData = dataNormalizationService.normalizeData(rawData, partnerId, entityType);

            String batchId = UUID.randomUUID().toString();
            StagingRecord record = StagingRecord.builder()
                    .partnerId(partnerId)
                    .entityType(entityType)
                    .rawData(rawData)
                    .normalizedData(normalizedData)
                    .status("PENDING")
                    .batchId(batchId)
                    .build();

            stagingRecordRepository.save(record);
            log.info("Saved {} record for partner {} with batch ID {}", entityType, partnerId, batchId);

        } catch (Exception e) {
            // Save failed record with error status
            String batchId = UUID.randomUUID().toString();
            StagingRecord record = StagingRecord.builder()
                    .partnerId(partnerId)
                    .entityType(entityType)
                    .rawData(rawData)
                    .normalizedData(null)
                    .status("FAILED")
                    .batchId(batchId)
                    .build();

            stagingRecordRepository.save(record);
            log.error("Failed to normalize {} data for partner {}: {}", entityType, partnerId, e.getMessage());
            throw e; // Re-throw to trigger job execution failure
        }
    }
}