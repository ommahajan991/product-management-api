package com.ommahajan.product_managment_api.service;

import com.ommahajan.product_managment_api.dto.request.LoginRequest;
import com.ommahajan.product_managment_api.dto.request.RegisterRequest;
import com.ommahajan.product_managment_api.dto.response.AuthResponse;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {

    public void register(RegisterRequest request);
    public AuthResponse login(LoginRequest request);
    public AuthResponse refresh(String rawRefreshToken);
    void logout(String rawRefreshToken);
}
