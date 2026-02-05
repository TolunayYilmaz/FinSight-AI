package com.exchange.trader.repository;

import com.exchange.trader.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    // Bir kullanıcının tüm portföyünü getir
    List<Portfolio> findByUserId(Long userId);

    // Bir kullanıcının belirli bir coini/hissesini getir (Örn: User 1'in BTC cüzdanı)
    Optional<Portfolio> findByUserIdAndSymbol(Long userId, String symbol);

    // Varlık kontrolü (Opsiyonel)
    boolean existsByUserIdAndSymbol(Long userId, String symbol);
}