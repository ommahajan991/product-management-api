package com.ommahajan.product_managment_api.controller;

import com.ommahajan.product_managment_api.dto.request.ProductRequest;
import com.ommahajan.product_managment_api.dto.response.ProductResponse;
import com.ommahajan.product_managment_api.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAll(Pageable pageable){
        return ResponseEntity.ok(productService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Integer id){
        return ResponseEntity.ok(productService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updatebyId(@PathVariable Integer id, @Valid @RequestBody ProductRequest productRequest){
        return ResponseEntity.ok(productService.updateById(id, productRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id){
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}