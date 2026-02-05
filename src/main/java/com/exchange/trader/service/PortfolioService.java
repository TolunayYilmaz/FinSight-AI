package com.exchange.trader.service;

import com.exchange.trader.entity.Portfolio;
import java.math.BigDecimal;
import java.util.List;

public interface PortfolioService {
    public List<Portfolio> getUserPortfolio(Long userId);
    public Portfolio buyAsset(Long userId, String symbol, Double quantity, BigDecimal price);
    public Portfolio sellAsset(Long userId, String symbol, Double quantity);
}
