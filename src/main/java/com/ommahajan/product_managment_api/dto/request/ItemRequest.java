package com.ommahajan.product_managment_api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemRequest(
        @NotNull(message = "quantity is required")
        @Min(value = 0, message = "quantity cannot be negative") Integer quantity
) {
}
