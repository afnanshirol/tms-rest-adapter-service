package com.tms.adapter.dto;

import lombok.Data;

@Data
public class FieldMapping {
    private String partnerId;
    private String entityType;
    private String sourceField;
    private String targetField;
    private boolean isRequired;
}