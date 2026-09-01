package com.ommahajan.product_managment_api.mapper;

import com.ommahajan.product_managment_api.dto.response.ItemResponse;
import com.ommahajan.product_managment_api.entity.Item;

public final class ItemMapper {

    private ItemMapper() {}

    public static ItemResponse toResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getQuantity(),
                item.getProduct().getId()
        );
    }
}