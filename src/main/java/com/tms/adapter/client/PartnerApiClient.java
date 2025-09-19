package com.tms.adapter.client;

import com.tms.adapter.dto.PartnerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerApiClient {

    private final RestTemplate restTemplate;
    private final ConfigServiceClient configServiceClient;

    public String fetchTheatreData(String partnerId) {
        return fetchData(partnerId, "theatres", "theatre");
    }

    public String fetchHallData(String partnerId) {
        return fetchData(partnerId, "halls", "hall");
    }

    public String fetchShowData(String partnerId) {
        return fetchData(partnerId, "shows", "show");
    }

    private String fetchData(String partnerId, String endpointKey, String dataType) {
        log.info("Fetching {} data for partner: {}", dataType, partnerId);

        try {
            PartnerConfig partnerConfig = getPartnerConfig(partnerId);
            String endpoint = partnerConfig.getEndpoints().get(endpointKey);

            if (endpoint == null) {
                throw new IllegalArgumentException("Endpoint not configured for " + endpointKey + " in partner " + partnerId);
            }

            String apiUrl = partnerConfig.getBaseUrl() + endpoint;

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            ).getBody();

            log.info("Successfully fetched {} data from {}", dataType, partnerId);
            return response;

        } catch (Exception e) {
            log.error("Failed to fetch {} data from partner {}: {}", dataType, partnerId, e.getMessage());
            throw e;
        }
    }

    private PartnerConfig getPartnerConfig(String partnerId) {
        List<PartnerConfig> partners = configServiceClient.getActivePartners();

        return partners.stream()
                .filter(partner -> partner.getPartnerId().equals(partnerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Partner not found: " + partnerId));
    }
}