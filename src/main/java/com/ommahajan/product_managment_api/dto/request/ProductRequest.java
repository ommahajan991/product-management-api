package com.ommahajan.product_managment_api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProductRequest(
        @NotBlank(message = "Product Name is Required") String productName
) { }