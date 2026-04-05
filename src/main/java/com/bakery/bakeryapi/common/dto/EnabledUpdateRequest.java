package com.bakery.bakeryapi.common.dto;

import jakarta.validation.constraints.NotNull;

public record EnabledUpdateRequest(
        @NotNull(message = "enabled is required") Boolean enabled
) {
}

