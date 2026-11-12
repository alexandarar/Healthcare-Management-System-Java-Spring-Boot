package com.hms.service;

import com.hms.dto.auth.AuthResponse;
import com.hms.dto.auth.LoginRequest;
import com.hms.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
}
