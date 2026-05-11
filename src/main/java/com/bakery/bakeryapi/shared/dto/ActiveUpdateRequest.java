package com.bakery.bakeryapi.shared.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for toggling active/enabled state.
 */
public record ActiveUpdateRequest(
        @NotNull(message = "active is required") Boolean active
) {
}

