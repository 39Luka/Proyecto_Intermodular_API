package org.example.bakeryapi.common.exception;

import java.time.Instant;

public record ErrorResponse(
        int status,
        Object message,
        Instant timestamp
) {
    public ErrorResponse(int status, Object message) {
        this(status, message, Instant.now());
    }
}
