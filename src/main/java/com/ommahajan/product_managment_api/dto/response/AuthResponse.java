package com.ommahajan.product_managment_api.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}