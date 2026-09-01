package com.ommahajan.product_managment_api.service.implementation;

import com.ommahajan.product_managment_api.dto.request.LoginRequest;
import com.ommahajan.product_managment_api.dto.request.RegisterRequest;
import com.ommahajan.product_managment_api.dto.response.AuthResponse;
import com.ommahajan.product_managment_api.entity.AppUser;
import com.ommahajan.product_managment_api.entity.RefreshToken;
import com.ommahajan.product_managment_api.entity.Role;
import com.ommahajan.product_managment_api.exception.InvalidTokenException;
import com.ommahajan.product_managment_api.repository.AppUserRepository;
import com.ommahajan.product_managment_api.security.JwtService;
import com.ommahajan.product_managment_api.security.RefreshTokenService;
import com.ommahajan.product_managment_api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    @Override
    public void register(RegisterRequest request) {
        if (appUserRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }
        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        appUserRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidTokenException("Invalid username or password");
        }

        AppUser user = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidTokenException("Invalid username or password"));

        String accessToken = jwtService.generateToken(user.getUsername(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createFor(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    @Transactional
    @Override
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshToken oldToken = refreshTokenService.verify(rawRefreshToken);
        AppUser user = oldToken.getUser();

        refreshTokenService.revoke(oldToken);
        RefreshToken newRefreshToken = refreshTokenService.createFor(user);
        String newAccessToken = jwtService.generateToken(user.getUsername(), user.getRole().name());

        return new AuthResponse(newAccessToken, newRefreshToken.getToken());
    }

    @Transactional
    @Override
    public void logout(String rawRefreshToken) {
        RefreshToken token = refreshTokenService.verify(rawRefreshToken);
        refreshTokenService.revoke(token);
    }
}