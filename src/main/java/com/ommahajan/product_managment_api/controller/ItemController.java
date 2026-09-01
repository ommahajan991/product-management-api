package com.ommahajan.product_managment_api.controller;

import com.ommahajan.product_managment_api.dto.response.ItemResponse;
import com.ommahajan.product_managment_api.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{productId}/items")
    public ResponseEntity<Page<ItemResponse>> getItemsByProductId(@PathVariable Integer productId, Pageable pageable) {
        return ResponseEntity.ok(itemService.getItemsByProductId(productId, pageable));
    }
}