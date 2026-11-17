package com.burim.mcp_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReviewDto(
        Long id,
        Long productId,
        Integer rating,
        String title,
        String description,
        String advantages,
        String disadvantages,
        OffsetDateTime createdAt
) {
    public ReviewDto {
        title = truncate(title);
        description = truncate(description);
        advantages = truncate(advantages);
        disadvantages = truncate(disadvantages);
    }

    private static String truncate(String text) {
        if (text != null && text.length() > 500) {
            return text.substring(0, 500) + "...";
        }
        return text;
    }
}
