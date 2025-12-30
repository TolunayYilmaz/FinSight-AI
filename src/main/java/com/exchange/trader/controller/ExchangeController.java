package com.exchange.trader.controller;

import com.exchange.trader.dto.AnalysisResponse;
import com.exchange.trader.service.PhytonIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exchange")
public class ExchangeController {
    @Autowired
    private PhytonIntegrationService phytonIntegrationService;

    @GetMapping("/hisse")
    public AnalysisResponse getInterest(@RequestParam String kod){

        return  phytonIntegrationService.getInterest(kod);
    }
}
