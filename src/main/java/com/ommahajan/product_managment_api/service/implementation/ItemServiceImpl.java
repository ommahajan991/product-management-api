package com.ommahajan.product_managment_api.service.implementation;

import com.ommahajan.product_managment_api.dto.response.ItemResponse;
import com.ommahajan.product_managment_api.exception.ResourceNotFoundException;
import com.ommahajan.product_managment_api.mapper.ItemMapper;
import com.ommahajan.product_managment_api.repository.ItemRepository;
import com.ommahajan.product_managment_api.repository.ProductRepository;
import com.ommahajan.product_managment_api.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ItemResponse> getItemsByProductId(Integer productId, Pageable pageable) {
        if(!productRepository.existsById(productId)){
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        return itemRepository.findByProductId(productId, pageable).map(ItemMapper::toResponse);
    }
}