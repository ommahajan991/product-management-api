package com.ommahajan.product_managment_api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "refreshToken is required")
        String refreshToken
) {}