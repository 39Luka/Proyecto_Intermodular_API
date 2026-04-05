package com.bakery.bakeryapi.dto.common;

import jakarta.validation.constraints.NotNull;

public record EnabledUpdateRequest(
        @NotNull(message = "enabled is required") Boolean enabled
) {
}

