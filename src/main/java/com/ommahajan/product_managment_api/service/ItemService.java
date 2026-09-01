package com.ommahajan.product_managment_api.service;

import com.ommahajan.product_managment_api.dto.response.ItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemService {

    Page<ItemResponse> getItemsByProductId(Integer productId, Pageable pageable);
}
