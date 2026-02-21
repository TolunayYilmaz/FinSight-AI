package com.exchange.trader.controller;

import com.exchange.trader.dto.TradeRequest;
import com.exchange.trader.entity.Portfolio;
import com.exchange.trader.entity.User;
import com.exchange.trader.repository.UserRepository;
import com.exchange.trader.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portfoy") // SecurityConfig'deki kurala tam uyması için ana dizin
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserRepository userRepository;

    // 1. KULLANICININ KENDİ PORTFÖYÜNÜ GETİR
    // GET http://localhost:8080/portfoy
    @GetMapping
    public ResponseEntity<List<Portfolio>> getMyPortfolio(Authentication authentication) {
        // Giriş yapmış kullanıcının email/username bilgisini Security Context'ten alıyoruz
        String email = authentication.getName();

        // Veritabanından o kullanıcıyı buluyoruz (UserRepository'de findByEmail olduğunu varsayıyorum)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Sadece o kullanıcıya ait portföyü döndürüyoruz
        List<Portfolio> portfolio = portfolioService.getUserPortfolio(user.getId());
        return ResponseEntity.ok(portfolio);
    }

    // 2. ALIM İŞLEMİ (BUY)
    // POST http://localhost:8080/portfoy/buy
    @PostMapping("/buy")
    public ResponseEntity<Portfolio> buyAsset(@RequestBody TradeRequest request, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Portfolio updatedPortfolio = portfolioService.buyAsset(
                user.getId(),
                request.symbol(),
                request.quantity(),
                request.price()
        );
        return ResponseEntity.ok(updatedPortfolio);
    }

    // 3. SATIM İŞLEMİ (SELL)
    // POST http://localhost:8080/portfoy/sell
    @PostMapping("/sell")
    public ResponseEntity<Portfolio> sellAsset(@RequestBody TradeRequest request, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Portfolio updatedPortfolio = portfolioService.sellAsset(
                user.getId(),
                request.symbol(),
                request.quantity()
        );
        return ResponseEntity.ok(updatedPortfolio);
    }
}