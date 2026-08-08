package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.auth.AuthResponse;
import com.shopsphere.shopsphere.dto.auth.LoginRequest;
import com.shopsphere.shopsphere.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
