package com.ommahajan.product_managment_api.dto.response;

import java.time.LocalDateTime;

public record ProductResponse(
        Integer id,
        String productName,
        String createdBy,
        LocalDateTime createdOn,
        String modifiedBy,
        LocalDateTime modifiedOn
) { }