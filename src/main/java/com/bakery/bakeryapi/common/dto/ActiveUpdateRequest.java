package com.bakery.bakeryapi.common.dto;

import jakarta.validation.constraints.NotNull;

public record ActiveUpdateRequest(
        @NotNull(message = "active is required") Boolean active
) {
}

