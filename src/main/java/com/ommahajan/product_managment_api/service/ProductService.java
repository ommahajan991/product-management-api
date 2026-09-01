package com.ommahajan.product_managment_api.service;

import com.ommahajan.product_managment_api.dto.request.ProductRequest;
import com.ommahajan.product_managment_api.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse create(ProductRequest productRequest);
    Page<ProductResponse> getAll(Pageable pageable);
    ProductResponse getById(Integer id);
    ProductResponse updateById(Integer id, ProductRequest productRequest);
    void delete(Integer id);
}