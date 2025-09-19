package com.tms.adapter.client;

import com.tms.adapter.dto.FieldMapping;
import com.tms.adapter.dto.PartnerConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "tms-config-service", url = "${config.service.url:http://localhost:8086}")
public interface ConfigServiceClient {

    @GetMapping("/partners/active")
    List<PartnerConfig> getActivePartners();

    @GetMapping("/integration/partners/{partnerId}/mappings/{entityType}")
    List<FieldMapping> getFieldMappings(@PathVariable String partnerId, @PathVariable String entityType);
}