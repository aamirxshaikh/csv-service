package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ValidationErrorResponseDto(
        String apiPath,
        Integer httpStatusCode,
        String errorMessage,
        LocalDateTime errorTimestamp,
        Map<String, String> errors
) {
}
