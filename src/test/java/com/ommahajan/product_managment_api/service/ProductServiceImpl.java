package com.ommahajan.product_managment_api.service;

import com.ommahajan.product_managment_api.dto.request.ProductRequest;
import com.ommahajan.product_managment_api.dto.response.ProductResponse;
import com.ommahajan.product_managment_api.entity.Product;
import com.ommahajan.product_managment_api.exception.ResourceNotFoundException;
import com.ommahajan.product_managment_api.repository.ProductRepository;
import com.ommahajan.product_managment_api.service.implementation.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product existingProduct;

    @BeforeEach
    void setUp() {
        existingProduct = new Product();
        existingProduct.setId(1);
        existingProduct.setProductName("Wireless Mouse");
        existingProduct.setCreatedBy("system");
        existingProduct.setCreatedOn(LocalDateTime.now());
    }

    @Test
    void create_savesAndReturnsProduct() {
        ProductRequest request = new ProductRequest("Wireless Mouse");
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        ProductResponse response = productService.create(request);

        assertThat(response.productName()).isEqualTo("Wireless Mouse");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void getById_whenExists_returnsProduct() {
        when(productRepository.findById(1)).thenReturn(Optional.of(existingProduct));

        ProductResponse response = productService.getById(1);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.productName()).isEqualTo("Wireless Mouse");
    }

    @Test
    void getById_whenNotFound_throwsException() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getAll_returnsPagedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(existingProduct));
        when(productRepository.findAll(pageable)).thenReturn(page);

        Page<ProductResponse> result = productService.getAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).productName()).isEqualTo("Wireless Mouse");
    }

    @Test
    void updateById_whenExists_updatesAndReturnsProduct() {
        ProductRequest request = new ProductRequest("Wireless Mouse V2");
        when(productRepository.findById(1)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        ProductResponse response = productService.updateById(1, request);

        assertThat(existingProduct.getProductName()).isEqualTo("Wireless Mouse V2");
        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateById_whenNotFound_throwsException() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());
        ProductRequest request = new ProductRequest("Doesn't matter");

        assertThatThrownBy(() -> productService.updateById(999, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_deletesProduct() {
        when(productRepository.findById(1)).thenReturn(Optional.of(existingProduct));

        productService.delete(1);

        verify(productRepository, times(1)).delete(existingProduct);
    }

    @Test
    void delete_whenNotFound_throwsException() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}