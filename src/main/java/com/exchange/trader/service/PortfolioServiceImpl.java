package com.exchange.trader.service;

import com.exchange.trader.entity.Portfolio;
import com.exchange.trader.entity.User;
import com.exchange.trader.repository.PortfolioRepository;
import com.exchange.trader.repository.UserRepository; // User entity'si için gerekli
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository; // User'ı bulmak için

    // Kullanıcının tüm varlıklarını listele
    public List<Portfolio> getUserPortfolio(Long userId) {
        return portfolioRepository.findByUserId(userId);
    }

    // ALIM İŞLEMİ (Buy) - Ağırlıklı Ortalama Maliyet Hesabı Yapar
    @Transactional
    public Portfolio buyAsset(Long userId, String symbol, Double quantity, BigDecimal price) {
        // 1. Kullanıcıyı bul
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // 2. Portföyde bu varlık daha önce var mı?
        Optional<Portfolio> existingPortfolio = portfolioRepository.findByUserIdAndSymbol(userId, symbol);

        if (existingPortfolio.isPresent()) {
            // VARSA: Üzerine ekle ve maliyet ortalamasını güncelle
            Portfolio portfolio = existingPortfolio.get();

            // Eski Toplam Değer = (Eski Adet * Eski Maliyet)
            BigDecimal oldTotalCost = portfolio.getAverageCost().multiply(BigDecimal.valueOf(portfolio.getQuantity()));

            // Yeni Alım Değeri = (Yeni Adet * Yeni Fiyat)
            BigDecimal newPurchaseCost = price.multiply(BigDecimal.valueOf(quantity));

            // Yeni Toplam Adet
            Double newTotalQuantity = portfolio.getQuantity() + quantity;

            // Yeni Ortalama Maliyet = (Eski Toplam + Yeni Alım) / Yeni Toplam Adet
            BigDecimal totalCost = oldTotalCost.add(newPurchaseCost);
            BigDecimal newAverageCost = totalCost.divide(BigDecimal.valueOf(newTotalQuantity), 8, RoundingMode.HALF_UP);

            portfolio.setQuantity(newTotalQuantity);
            portfolio.setAverageCost(newAverageCost);

            return portfolioRepository.save(portfolio);
        } else {
            // YOKSA: Yeni kayıt oluştur
            Portfolio newPortfolio = new Portfolio();
            newPortfolio.setUser(user);
            newPortfolio.setSymbol(symbol);
            newPortfolio.setQuantity(quantity);
            newPortfolio.setAverageCost(price);

            return portfolioRepository.save(newPortfolio);
        }
    }

    // SATIM İŞLEMİ (Sell) - Sadece Adet Düşer, Maliyet Değişmez
    @Transactional
    public Portfolio sellAsset(Long userId, String symbol, Double quantity) {
        Portfolio portfolio = portfolioRepository.findByUserIdAndSymbol(userId, symbol)
                .orElseThrow(() -> new RuntimeException("Satılacak varlık portföyde bulunamadı: " + symbol));

        if (portfolio.getQuantity() < quantity) {
            throw new RuntimeException("Yetersiz bakiye! Mevcut: " + portfolio.getQuantity());
        }

        Double newQuantity = portfolio.getQuantity() - quantity;

        // Eğer tüm varlık satıldıysa kaydı silebilirsin veya 0 olarak tutabilirsin.
        // Genelde işlem geçmişi için 0 olarak tutmak veya silmek iş kuralına bağlıdır.
        if (newQuantity <= 0.000001) { // Floating point hatası olmaması için 0 kontrolü
            portfolioRepository.delete(portfolio);
            return null;
        } else {
            portfolio.setQuantity(newQuantity);
            return portfolioRepository.save(portfolio);
        }
    }
}