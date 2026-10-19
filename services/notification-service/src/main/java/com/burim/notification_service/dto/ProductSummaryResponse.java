package com.burim.notification_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductSummaryResponse(
        Long id,
        String name
) {}