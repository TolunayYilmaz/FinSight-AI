package com.exchange.trader.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


public record AnalysisResponse(
        boolean basarili,
        Meta meta,
        @JsonProperty("fiyat_verileri") FiyatVerileri fiyatVerileri,
        @JsonProperty("temel_analiz") TemelAnaliz temelAnaliz,
        @JsonProperty("duygu_analizi") DuyguAnalizi duyguAnalizi,
        @JsonProperty("yapay_zeka_tahminleri") Tahminler tahminler
) {



    public record Meta(
            @JsonProperty("hisse_kodu") String hisseKodu,
            String sektor
    ) {}

    public record FiyatVerileri(
            @JsonProperty("guncel_fiyat") Double guncelFiyat,
            @JsonProperty("teknik_trend") String teknikTrend,
            @JsonProperty("analist_hedefi_12ay") Double analistHedefi
    ) {}

    public record TemelAnaliz(
            @JsonProperty("fk_orani") Double fkOrani,
            @JsonProperty("pddd_orani") Double pdddOrani,
            Double favok
    ) {}

    public record DuyguAnalizi(
            @JsonProperty("hype_puani") Double hypePuani,
            @JsonProperty("son_haberler") List<String> sonHaberler
    ) {}

    public record Tahminler(
            @JsonProperty("kisa_vade_1hf") Double kisaVade,
            @JsonProperty("orta_vade_1ay") Double ortaVade,
            @JsonProperty("uzun_vade_6ay") Double uzunVade
    ) {}
}