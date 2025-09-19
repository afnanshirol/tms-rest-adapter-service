package com.tms.adapter.repository;

import com.tms.adapter.entity.StagingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StagingRecordRepository extends JpaRepository<StagingRecord, Long> {
}