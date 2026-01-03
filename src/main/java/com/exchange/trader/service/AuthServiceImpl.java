package com.exchange.trader.service;

import com.exchange.trader.dto.LoginRequest;
import com.exchange.trader.dto.LoginResponse;
import com.exchange.trader.dto.RegisterRequest;
import com.exchange.trader.dto.RegisterResponse;
import com.exchange.trader.entity.Role;
import com.exchange.trader.entity.User;
import com.exchange.trader.repository.RoleRepository;
import com.exchange.trader.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.Console;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final RoleRepository roleRepository;

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        Optional<User> optionalUser = userRepository.findbyEmail(registerRequest.email());
        if(optionalUser.isPresent()){
           System.out.println("Kullanıcı bulunuyor");
        }
        String encodedPasword=passwordEncoder.encode(registerRequest.password());
        Optional<Role> role = roleRepository.findRole("USER");
        if(role.isEmpty()){
            role.get().setAuthority("USER");
            roleRepository.save(role.get());
        }
        User user = new User();
        user.setUserName(registerRequest.userName());
        user.setEmail(registerRequest.email());
        user.setPassword(registerRequest.password());
        return new RegisterResponse(user.getEmail(),"Kullanıcı başarı ile kayıt olmuştur");

    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Optional<User> optionalUser =userRepository.findbyEmail(loginRequest.email());
        if (optionalUser.isEmpty()){
            System.out.println("Kullanıcı yok");
        }
        else if(!passwordEncoder.matches(loginRequest.password(),optionalUser.get().getPassword())){
            System.out.println("Şifre veya email yanlış");
        }


        return new LoginResponse(optionalUser.get().getEmail(),"Kullanıcı Başarı ile giriş yapt");
    }
}
