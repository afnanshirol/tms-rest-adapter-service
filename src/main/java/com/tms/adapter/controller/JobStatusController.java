package com.tms.adapter.controller;

import com.tms.adapter.dto.JobStatusResponse;
import com.tms.adapter.entity.JobExecution;
import com.tms.adapter.repository.JobExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/job-status")
@RequiredArgsConstructor
@Slf4j
public class JobStatusController {

    private final JobExecutionRepository jobExecutionRepository;

    @GetMapping("/latest")
    public JobStatusResponse getLatestJobStatus() {
        List<JobExecution> latestExecutions = jobExecutionRepository.findLatestJobExecutions();

        if (latestExecutions.isEmpty()) {
            return JobStatusResponse.builder()
                    .jobDate(LocalDate.now())
                    .totalPartners(0)
                    .successfulPartners(0)
                    .failedPartners(0)
                    .partnerStatuses(List.of())
                    .build();
        }

        LocalDate latestDate = latestExecutions.get(0).getJobDate();
        return buildJobStatusResponse(latestDate, latestExecutions);
    }

    @GetMapping("/{date}")
    public JobStatusResponse getJobStatusByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<JobExecution> executions = jobExecutionRepository.findByJobDateOrderByExecutionTimeDesc(date);
        return buildJobStatusResponse(date, executions);
    }

    @GetMapping("/failed/{date}")
    public List<JobExecution> getFailedExecutions(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return jobExecutionRepository.findFailedExecutionsByDate(date);
    }

    private JobStatusResponse buildJobStatusResponse(LocalDate jobDate, List<JobExecution> executions) {
        Map<String, List<JobExecution>> partnerExecutions = executions.stream()
                .collect(Collectors.groupingBy(JobExecution::getPartnerId));

        List<JobStatusResponse.PartnerJobStatus> partnerStatuses = partnerExecutions.entrySet().stream()
                .map(entry -> buildPartnerStatus(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        long successfulPartners = partnerStatuses.stream()
                .filter(status -> "SUCCESS".equals(status.getOverallStatus()))
                .count();

        long failedPartners = partnerStatuses.stream()
                .filter(status -> "FAILED".equals(status.getOverallStatus()))
                .count();

        return JobStatusResponse.builder()
                .jobDate(jobDate)
                .totalPartners(partnerStatuses.size())
                .successfulPartners((int) successfulPartners)
                .failedPartners((int) failedPartners)
                .partnerStatuses(partnerStatuses)
                .build();
    }

    private JobStatusResponse.PartnerJobStatus buildPartnerStatus(String partnerId, List<JobExecution> executions) {
        long successful = executions.stream().filter(e -> "SUCCESS".equals(e.getStatus())).count();
        long failed = executions.stream().filter(e -> "FAILED".equals(e.getStatus())).count();

        List<String> failedEntityTypes = executions.stream()
                .filter(e -> "FAILED".equals(e.getStatus()))
                .map(JobExecution::getEntityType)
                .collect(Collectors.toList());

        String overallStatus;
        if (failed == 0) {
            overallStatus = "SUCCESS";
        } else if (successful == 0) {
            overallStatus = "FAILED";
        } else {
            overallStatus = "PARTIAL";
        }

        return JobStatusResponse.PartnerJobStatus.builder()
                .partnerId(partnerId)
                .successfulEntities((int) successful)
                .failedEntities((int) failed)
                .failedEntityTypes(failedEntityTypes)
                .overallStatus(overallStatus)
                .build();
    }
}