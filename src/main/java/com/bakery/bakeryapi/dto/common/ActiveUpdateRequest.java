package com.bakery.bakeryapi.dto.common;

import jakarta.validation.constraints.NotNull;

public record ActiveUpdateRequest(
        @NotNull(message = "active is required") Boolean active
) {
}

