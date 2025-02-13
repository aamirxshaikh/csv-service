package com.example.demo.dto;

import java.time.LocalDateTime;

public record ErrorResponseDto(
        String apiPath,
        Integer httpStatusCode,
        String errorMessage,
        LocalDateTime errorTimestamp
) {
}
