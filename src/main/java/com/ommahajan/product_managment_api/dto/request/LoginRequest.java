package com.ommahajan.product_managment_api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "username is required")
        String username,

        @NotBlank(message = "password is required")
        String password
) {}