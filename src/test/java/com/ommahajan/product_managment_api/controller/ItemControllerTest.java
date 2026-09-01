package com.ommahajan.product_managment_api.controller;

import com.ommahajan.product_managment_api.dto.response.ItemResponse;
import com.ommahajan.product_managment_api.exception.ResourceNotFoundException;
import com.ommahajan.product_managment_api.security.JwtAuthenticationFilter;
import com.ommahajan.product_managment_api.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(roles = "USER")
    void getItemsByProductId_whenProductExists_returns200() throws Exception {
        ItemResponse response = new ItemResponse(1, 5, 1);
        when(itemService.getItemsByProductId(eq(1), any())).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/products/1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].quantity").value(5));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getItemsByProductId_whenProductNotFound_returns404() throws Exception {
        when(itemService.getItemsByProductId(eq(999), any()))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

        mockMvc.perform(get("/api/v1/products/999/items"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItemsByProductId_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/items"))
                .andExpect(status().isUnauthorized());
    }
}