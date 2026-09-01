package com.ommahajan.product_managment_api.service;

import com.ommahajan.product_managment_api.dto.response.ItemResponse;
import com.ommahajan.product_managment_api.entity.Item;
import com.ommahajan.product_managment_api.entity.Product;
import com.ommahajan.product_managment_api.exception.ResourceNotFoundException;
import com.ommahajan.product_managment_api.repository.ItemRepository;
import com.ommahajan.product_managment_api.repository.ProductRepository;
import com.ommahajan.product_managment_api.service.implementation.ItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void getItemsByProductId_whenProductExists_returnsPagedItems() {
        Product product = new Product();
        product.setId(1);

        Item item = new Item();
        item.setId(1);
        item.setQuantity(5);
        item.setProduct(product);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> page = new PageImpl<>(List.of(item));

        when(productRepository.existsById(1)).thenReturn(true);
        when(itemRepository.findByProductId(1, pageable)).thenReturn(page);

        Page<ItemResponse> result = itemService.getItemsByProductId(1, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).quantity()).isEqualTo(5);
        assertThat(result.getContent().get(0).productId()).isEqualTo(1);
    }

    @Test
    void getItemsByProductId_whenProductDoesNotExist_throwsException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> itemService.getItemsByProductId(999, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}