package com.bakery.api.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record ErrorResponse(
        @Schema(description = "HTTP status code", example = "400")
        int status,
        @Schema(description = "Error message. May be a string or a structured object for validation errors.")
        Object message,
        @Schema(description = "Server timestamp", example = "2026-03-08T10:26:00Z")
        Instant timestamp
) {
    public ErrorResponse(int status, Object message) {
        this(status, message, Instant.now());
    }
}


