package com.tms.adapter.repository;

import com.tms.adapter.entity.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {

    List<JobExecution> findByJobDateOrderByExecutionTimeDesc(LocalDate jobDate);

    List<JobExecution> findByPartnerIdAndJobDateOrderByExecutionTimeDesc(String partnerId, LocalDate jobDate);

    @Query("SELECT je FROM JobExecution je WHERE je.jobDate = :date AND je.status = 'FAILED'")
    List<JobExecution> findFailedExecutionsByDate(@Param("date") LocalDate date);

    @Query("SELECT je FROM JobExecution je WHERE je.jobDate = (SELECT MAX(je2.jobDate) FROM JobExecution je2)")
    List<JobExecution> findLatestJobExecutions();
}