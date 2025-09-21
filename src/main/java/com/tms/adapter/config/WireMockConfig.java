package com.tms.adapter.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Configuration
@ConditionalOnProperty(name = "wiremock.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class WireMockConfig {

    @Value("${wiremock.port:8089}")
    private int wireMockPort;

    private WireMockServer wireMockServer;

    @PostConstruct
    public void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(wireMockPort));
        wireMockServer.start();

        log.info("WireMock server started on port {}", wireMockPort);

        setupMockResponses();
    }

    @PreDestroy
    public void stopWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            log.info("WireMock server stopped");
        }
    }


    private void setupMockResponses() {
        setupInoxMockResponses();
        setupPvrMockResponses();
    }

    private void setupInoxMockResponses() {
        // INOX Theatre Data - Only mapped fields from Config Service
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/theatres"))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "theatres": [
                            {
                                "theater_id": "INOX_BLR_FORUM",
                                "theater_name": "INOX Forum Mall",
                                "theater_city": "Bangalore",
                                "theater_address": "Forum Mall, Koramangala"
                            },
                            {
                                "theater_id": "INOX_BLR_GARUDA",
                                "theater_name": "INOX Garuda Mall",
                                "theater_city": "Bangalore",
                                "theater_address": "Garuda Mall, Magrath Road"
                            }
                        ]
                    }
                    """)));

        // INOX Hall Data - Only mapped fields from Config Service
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/halls"))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "halls": [
                            {
                                "hall_id": "INOX_BLR_FORUM_HALL_1",
                                "hall_name": "LUXE Screen 1",
                                "capacity": 180
                            },
                            {
                                "hall_id": "INOX_BLR_GARUDA_HALL_1",
                                "hall_name": "Premium Screen 1",
                                "capacity": 220
                            }
                        ]
                    }
                    """)));

        // INOX Show Data - Only mapped fields from Config Service
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/shows"))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "shows": [
                            {
                                "show_id": "INOX_SHOW_12345",
                                "movie_name": "Avengers: Endgame",
                                "show_time": "2024-01-15T14:30:00"
                            },
                            {
                                "show_id": "INOX_SHOW_67890",
                                "movie_name": "Spider-Man: No Way Home",
                                "show_time": "2024-01-15T18:00:00"
                            }
                        ]
                    }
                    """)));
    }

    private void setupPvrMockResponses() {
        // PVR Theatre Data - Only mapped fields from Config Service
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v2/theatres"))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "status": "success",
                        "data": {
                            "cinema_locations": [
                                {
                                    "cinema_id": "PVR_DEL_SELECT",
                                    "cinema_name": "PVR Select City Walk",
                                    "location": "New Delhi"
                                },
                                {
                                    "cinema_id": "PVR_MUM_PHOENIX",
                                    "cinema_name": "PVR Phoenix Mills",
                                    "location": "Mumbai"
                                }
                            ]
                        }
                    }
                    """)));

        // PVR Hall Data - Only mapped fields from Config Service
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v2/shows"))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "screens": [
                            {
                                "screen_id": "PVR_DEL_SELECT_SCREEN_1",
                                "screen_name": "Gold Class Screen 1",
                                "seats": 120
                            },
                            {
                                "screen_id": "PVR_MUM_PHOENIX_SCREEN_1",
                                "screen_name": "Premium Screen 1",
                                "seats": 180
                            }
                        ]
                    }
                    """)));

        // INOX Price Data - Based on Config Service PRICE entity mappings
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/prices"))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "prices": [
                            {
                                "price_id": "INOX_PRICE_001",
                                "show_id": "INOX_SHOW_12345",
                                "seat_category": "PLATINUM",
                                "price_amount": 350.00
                            },
                            {
                                "price_id": "INOX_PRICE_002",
                                "show_id": "INOX_SHOW_12345",
                                "seat_category": "GOLD",
                                "price_amount": 250.00
                            },
                            {
                                "price_id": "INOX_PRICE_003",
                                "show_id": "INOX_SHOW_67890",
                                "seat_category": "PLATINUM",
                                "price_amount": 450.00
                            }
                        ]
                    }
                    """)));
    }

}