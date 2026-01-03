package com.exchange.trader.controller;
import com.exchange.trader.dto.LoginRequest;
import com.exchange.trader.dto.LoginResponse;
import com.exchange.trader.dto.RegisterRequest;
import com.exchange.trader.dto.RegisterResponse;
import com.exchange.trader.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    private final AuthService authService;

    @PostMapping("/register")
    public RegisterResponse register(@Validated @RequestBody RegisterRequest registerRequest){

        return authService.register(registerRequest);
    }
    @PostMapping("/login")
    public LoginResponse loginResponse(@Validated @RequestBody LoginRequest loginRequest){
        return authService.login(loginRequest);
    }

}
