package com.tms.adapter.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PartnerConfig {
    private String partnerId;
    private String baseUrl;
    private String oauthUrl;
    private String clientId;
    private String clientSecret;
    private boolean active;
    private Map<String, String> endpoints; // e.g., {"theatres": "/v1/theatres", "halls": "/v1/halls", "shows": "/v1/shows"}
}