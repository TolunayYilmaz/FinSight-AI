package com.exchange.trader.service;

import com.exchange.trader.dto.LoginRequest;
import com.exchange.trader.dto.LoginResponse;
import com.exchange.trader.dto.RegisterRequest;
import com.exchange.trader.dto.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest registerRequest);
    LoginResponse login(LoginRequest loginRequest);
}
