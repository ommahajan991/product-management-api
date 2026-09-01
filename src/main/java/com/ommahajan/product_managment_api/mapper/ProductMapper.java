package com.ommahajan.product_managment_api.mapper;

import com.ommahajan.product_managment_api.dto.response.ProductResponse;
import com.ommahajan.product_managment_api.entity.Product;

public final class ProductMapper {

    private ProductMapper() {}

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getCreatedBy(),
                product.getCreatedOn(),
                product.getModifiedBy(),
                product.getModifiedOn()
        );
    }
}