from fastapi import FastAPI
import yfinance as yf
from GoogleNews import GoogleNews
from textblob import TextBlob

app = FastAPI()

# --- YARDIMCI FONKSİYONLAR (Streamlit Kodundan Aldık) ---

def temel_verileri_getir(ticker_obj):
    info = ticker_obj.info
    return {
        "F_K": info.get('trailingPE', 0), 
        "PD_DD": info.get('priceToBook', 0), 
        "FAVOK": info.get('ebitda', 0),      
        "Sektor": info.get('industry', 'Bilinmiyor'),
        "Analist_Hedefi": info.get('targetMeanPrice', 0)
    }

def duygu_analizi_yap(hisse_kodu):
    googlenews = GoogleNews()
    googlenews.set_lang('en')
    googlenews.search(hisse_kodu)
    sonuclar = googlenews.result()
    
    if not sonuclar:
        return 50, []

    toplam_duygu = 0
    haber_basliklari = []
    for haber in sonuclar[:5]:
        baslik = haber['title']
        analiz = TextBlob(baslik)
        skor = analiz.sentiment.polarity
        toplam_duygu += skor
        haber_basliklari.append(baslik)

    hype_puani = ((toplam_duygu / len(sonuclar[:5])) + 1) * 50
    return hype_puani, haber_basliklari

def fiyat_tahmin_et(guncel_fiyat, hype_puani, teknik_trend):
    katsayi = (hype_puani - 50) / 100
    trend_etkisi = 0.05 if teknik_trend == "POZİTİF" else -0.05

    kisa_vade = guncel_fiyat * (1 + (katsayi * 0.1) + trend_etkisi)     
    orta_vade = guncel_fiyat * (1 + (katsayi * 0.2) + (trend_etkisi*2)) 
    uzun_vade = guncel_fiyat * (1 + (katsayi * 0.5) + (trend_etkisi*4)) 

    return kisa_vade, orta_vade, uzun_vade

# --- API ENDPOINT (Java Buraya İstek Atacak) ---

@app.get("/analiz")
def tam_tesekkullu_analiz(hisse: str):
    try:
        # 1. Veri Çekme
        ticker = yf.Ticker(hisse)
        df = ticker.history(period="1y")
        
        if df.empty:
            return {"basarili": False, "mesaj": "Veri bulunamadı"}
            
        # 2. Hesaplamalar
        guncel_fiyat = df['Close'].iloc[-1]
        temel_veriler = temel_verileri_getir(ticker)
        hype, haberler = duygu_analizi_yap(hisse)
        
        # Teknik Analiz
        df['MA50'] = df['Close'].rolling(window=50).mean()
        ma50 = df['MA50'].iloc[-1] if len(df) >= 50 else guncel_fiyat
        teknik_trend = "POZİTİF" if guncel_fiyat > ma50 else "NEGATİF"
        
        # Tahminler
        kisa, orta, uzun = fiyat_tahmin_et(guncel_fiyat, hype, teknik_trend)
        
        # 3. JSON Yanıtı Oluşturma (Structured Data)
        return {
            "basarili": True,
            "meta": {
                "hisse_kodu": hisse,
                "sektor": temel_veriler["Sektor"]
            },
            "fiyat_verileri": {
                "guncel_fiyat": round(guncel_fiyat, 2),
                "teknik_trend": teknik_trend,
                "analist_hedefi_12ay": temel_veriler["Analist_Hedefi"]
            },
            "temel_analiz": {
                "fk_orani": round(temel_veriler["F_K"], 2) if temel_veriler["F_K"] else 0,
                "pddd_orani": round(temel_veriler["PD_DD"], 2) if temel_veriler["PD_DD"] else 0,
                "favok": temel_veriler["FAVOK"]
            },
            "duygu_analizi": {
                "hype_puani": round(hype, 1),
                "son_haberler": haberler
            },
            "yapay_zeka_tahminleri": {
                "kisa_vade_1hf": round(kisa, 2),
                "orta_vade_1ay": round(orta, 2),
                "uzun_vade_6ay": round(uzun, 2)
            }
        }

    except Exception as e:
        return {"basarili": False, "hata_mesaji": str(e)}

@app.get("/")
def home():
    return {"durum": "aktif", "kullanim": "/analiz?hisse=THYAO.IS"}


if __name__ == "__main__":
    import uvicorn
    # Artık terminalden çalıştırmak yerine dosyanın içinden "Play" tuşuna basabilirsin
    uvicorn.run("api:app", host="127.0.0.1", port=8000, reload=True)

