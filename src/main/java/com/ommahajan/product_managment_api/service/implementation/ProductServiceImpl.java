package com.ommahajan.product_managment_api.service.implementation;

import com.ommahajan.product_managment_api.dto.request.ProductRequest;
import com.ommahajan.product_managment_api.dto.response.ProductResponse;
import com.ommahajan.product_managment_api.entity.Product;
import com.ommahajan.product_managment_api.exception.ResourceNotFoundException;
import com.ommahajan.product_managment_api.mapper.ProductMapper;
import com.ommahajan.product_managment_api.repository.ProductRepository;
import com.ommahajan.product_managment_api.service.AuditLogService;
import com.ommahajan.product_managment_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        product.setProductName(request.productName());
        Product saved = productRepository.save(product);
        auditLogService.logProductCreated(saved.getId(), saved.getCreatedBy());
        return ProductMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Integer id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product Not Found with ID: " + id));
        return ProductMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateById(Integer id, ProductRequest request) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product Not Found with ID: " + id));
        product.setProductName(request.productName());
        Product update = productRepository.save(product);
        return ProductMapper.toResponse(update);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product Not Found with ID: " + id));
        productRepository.delete(product);
    }
}