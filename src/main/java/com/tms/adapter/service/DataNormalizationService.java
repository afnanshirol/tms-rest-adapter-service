package com.tms.adapter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tms.adapter.client.ConfigServiceClient;
import com.tms.adapter.dto.FieldMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataNormalizationService {

    private final ObjectMapper objectMapper;
    private final ConfigServiceClient configServiceClient;

    public String normalizeData(String rawData, String partnerId, String entityType) {
        try {
            // Step 1: Validate JSON format
            JsonNode rawJson = objectMapper.readTree(rawData);

            // Step 2: Get field mappings from config service
            List<FieldMapping> mappings = getFieldMappings(partnerId, entityType);

            // Step 3: Apply field mappings and transformations
            JsonNode normalizedJson = applyFieldMappings(rawJson, mappings, partnerId, entityType);

            return objectMapper.writeValueAsString(normalizedJson);

        } catch (Exception e) {
            log.error("Failed to normalize data for partner {} entity {}: {}", partnerId, entityType, e.getMessage());
            throw new RuntimeException("Data normalization failed: " + e.getMessage(), e);
        }
    }

    private List<FieldMapping> getFieldMappings(String partnerId, String entityType) {
        try {
            return configServiceClient.getFieldMappings(partnerId, entityType);
        } catch (Exception e) {
            log.warn("Could not fetch field mappings for partner {} entity {}: {}", partnerId, entityType, e.getMessage());
            return List.of(); // Return empty list for basic validation only
        }
    }

    private JsonNode applyFieldMappings(JsonNode rawJson, List<FieldMapping> mappings, String partnerId, String entityType) {
        // Step 1: Basic validation
        if (rawJson == null || rawJson.isEmpty()) {
            throw new IllegalArgumentException("Empty or null data received");
        }

        if (!rawJson.isObject() && !rawJson.isArray()) {
            throw new IllegalArgumentException("Invalid data format - expected JSON object or array");
        }

        // Step 2: If no mappings available, return validated raw data
        if (mappings.isEmpty()) {
            log.debug("No field mappings found for partner {} entity {}, returning raw data after validation", partnerId, entityType);
            return rawJson;
        }

        // Step 3: Apply field mappings
        if (rawJson.isArray()) {
            return normalizeArray((ArrayNode) rawJson, mappings, partnerId, entityType);
        } else {
            return normalizeObject((ObjectNode) rawJson, mappings, partnerId, entityType);
        }
    }

    private JsonNode normalizeArray(ArrayNode rawArray, List<FieldMapping> mappings, String partnerId, String entityType) {
        ArrayNode normalizedArray = objectMapper.createArrayNode();

        for (JsonNode item : rawArray) {
            if (item.isObject()) {
                JsonNode normalizedItem = normalizeObject((ObjectNode) item, mappings, partnerId, entityType);
                normalizedArray.add(normalizedItem);
            } else {
                // Keep non-object items as-is
                normalizedArray.add(item);
            }
        }

        return normalizedArray;
    }

    private JsonNode normalizeObject(ObjectNode rawObject, List<FieldMapping> mappings, String partnerId, String entityType) {
        ObjectNode normalizedObject = objectMapper.createObjectNode();

        // Apply field mappings
        for (FieldMapping mapping : mappings) {
            String sourceField = mapping.getSourceField();
            String targetField = mapping.getTargetField();

            if (rawObject.has(sourceField)) {
                JsonNode value = rawObject.get(sourceField);
                normalizedObject.set(targetField, value);
            } else if (mapping.isRequired()) {
                throw new IllegalArgumentException(
                    String.format("Required field '%s' missing in %s data from partner %s",
                                sourceField, entityType, partnerId));
            }
        }

        // Validate we have some normalized data
        if (normalizedObject.isEmpty()) {
            log.warn("No fields were mapped for partner {} entity {}, keeping original data", partnerId, entityType);
            return rawObject;
        }

        log.debug("Applied {} field mappings for partner {} entity {}", mappings.size(), partnerId, entityType);
        return normalizedObject;
    }
}