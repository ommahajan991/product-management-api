package com.ommahajan.product_managment_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ommahajan.product_managment_api.dto.request.ProductRequest;
import com.ommahajan.product_managment_api.dto.response.ProductResponse;
import com.ommahajan.product_managment_api.exception.ResourceNotFoundException;
import com.ommahajan.product_managment_api.security.JwtAuthenticationFilter;
import com.ommahajan.product_managment_api.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ProductResponse sampleResponse;

    @Test
    @WithMockUser(roles = "USER")
    void create_withValidRequest_returns201() throws Exception {
        sampleResponse = new ProductResponse(1, "Wireless Mouse", "system", LocalDateTime.now(), null, null);
        when(productService.create(any(ProductRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductRequest("Wireless Mouse"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Wireless Mouse"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_withBlankName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.productName").exists());
    }

    @Test
    void create_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductRequest("Wireless Mouse"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getById_whenExists_returns200() throws Exception {
        sampleResponse = new ProductResponse(1, "Wireless Mouse", "system", LocalDateTime.now(), null, null);
        when(productService.getById(1)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getById_whenNotFound_returns404() throws Exception {
        when(productService.getById(999)).thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAll_returns200WithPagedContent() throws Exception {
        sampleResponse = new ProductResponse(1, "Wireless Mouse", "system", LocalDateTime.now(), null, null);
        when(productService.getAll(any())).thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName").value("Wireless Mouse"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_whenExists_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());
    }
}