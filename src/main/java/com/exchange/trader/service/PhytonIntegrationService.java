package com.exchange.trader.service;

import com.exchange.trader.dto.AnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PhytonIntegrationService {


    private final String PYTHON_API_URL = "http://127.0.0.1:8000/analiz?hisse=";
    @Autowired
    private final RestTemplate restTemplate;

    public AnalysisResponse getInterest(String hisseKodu){
        String url = PYTHON_API_URL + hisseKodu;
        try {
            return restTemplate.getForObject(url, AnalysisResponse.class);
        } catch (Exception e) {
            // Python kapalıysa veya hata varsa null veya boş obje dönebiliriz
            System.err.println("Python servisine ulaşılamadı: " + e.getMessage());
            throw new RuntimeException("Yapay zeka servisi şu an yanıt vermiyor.");
        }

    }
}
