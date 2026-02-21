package com.exchange.trader.dto;

import java.math.BigDecimal;

public record TradeRequest(String symbol, Double quantity, BigDecimal price) {
}
