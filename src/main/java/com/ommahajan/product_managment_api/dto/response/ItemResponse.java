package com.ommahajan.product_managment_api.dto.response;

public record ItemResponse(
        Integer id,
        Integer quantity,
        Integer productId
) { }