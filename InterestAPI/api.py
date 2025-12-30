from fastapi import FastAPI
import yfinance as yf

# --- DEĞİŞİKLİK BURADA (Uyumlu Import) ---
try:
    # Yeni versiyon (ddgs) varsa onu kullan
    from ddgs import DDGS
except ImportError:
    # Yoksa eski versiyonu (duckduckgo_search) dene
    from duckduckgo_search import DDGS

from textblob import TextBlob
import uvicorn
import requests
from bs4 import BeautifulSoup
import re
import numpy as np
from collections import Counter
import time
import warnings

# Gereksiz uyarıları terminalde gizle
warnings.filterwarnings("ignore")

app = FastAPI()

# --- SABİTLER ---
STOP_WORDS = {
    "the", "a", "an", "and", "or", "but", "stock", "price", "market", 
    "ve", "ile", "bir", "bu", "şu", "o", "ama", "fakat", "için", 
    "hisse", "fiyat", "borsa", "analiz", "tl", "dolar", "yorum", 
    "yatırım", "tavsiyesi", "değildir", "ytd", "hedef", "teknik", "nedir"
}

# --- YARDIMCI FONKSİYONLAR ---
def metin_temizle(text):
    return ' '.join(re.sub("(@[A-Za-z0-9]+)|([^0-9A-Za-z \t])|(\w+:\/\/\S+)", " ", text).split())

def metinden_fiyat_cikart(metin, guncel_fiyat):
    olasi_fiyatlar = re.findall(r'\b\d{2,5}(?:\.\d{1,2})?\b', metin)
    gecerli_tahminler = []
    for f in olasi_fiyatlar:
        try:
            fiyat = float(f)
            if (guncel_fiyat * 0.5) < fiyat < (guncel_fiyat * 3.0):
                gecerli_tahminler.append(fiyat)
        except:
            continue
    return np.mean(gecerli_tahminler) if gecerli_tahminler else None

def temel_verileri_getir(ticker_obj):
    info = ticker_obj.info
    hedef = info.get('targetMeanPrice') or info.get('targetMedianPrice') or 0
    return {
        "F_K": info.get('trailingPE', 0), 
        "PD_DD": info.get('priceToBook', 0), 
        "FAVOK": info.get('ebitda', 0),      
        "Sektor": info.get('industry', 'Bilinmiyor'),
        "Analist_Hedefi": hedef
    }

def url_baslik_ve_ozet_cek(url):
    try:
        headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'}
        response = requests.get(url, headers=headers, timeout=3)
        if response.status_code == 200:
            soup = BeautifulSoup(response.content, 'html.parser')
            title = soup.title.string if soup.title else ""
            desc = ""
            meta = soup.find('meta', attrs={'name': 'description'})
            if meta: desc = meta.get('content')
            else:
                p = soup.find('p')
                if p: desc = p.text[:200]
            return f"{title} {desc}"
    except:
        return None
    return None

def gelismis_hibrit_analiz(hisse_kodu, guncel_fiyat):
    saf_kod = hisse_kodu.split('.')[0]
    
    sorgular = [
        f'site:twitter.com "{saf_kod}" hedef fiyat',
        f'site:tr.investing.com "{saf_kod}" yorum',
        f'site:tr.tradingview.com "{saf_kod}"',
        f'{saf_kod} hisse teknik analiz yorum'
    ]

    toplam_duygu = 0
    kullanici_fiyat_tahminleri = []
    detayli_kaynaklar = [] 
    analiz_sayisi = 0
    tum_kelimeler = [] 
    sentiment_counts = {"Pozitif": 0, "Negatif": 0, "Nötr": 0}
    processed_urls = set()

    ddgs = DDGS()

    for sorgu in sorgular:
        try:
            # Bekleme süresi (Engel yememek için)
            time.sleep(2) 

            # DuckDuckGo Araması
            sonuclar = list(ddgs.text(sorgu, region='tr-tr', max_results=2))
            
            for sonuc in sonuclar:
                url = sonuc['href']
                baslik = sonuc['title']
                ozet = sonuc['body']
                
                if url in processed_urls: continue
                processed_urls.add(url)

                full_text = f"{baslik} {ozet}"
                temiz_icerik = metin_temizle(full_text)
                
                # Duygu Analizi
                skor = TextBlob(temiz_icerik).sentiment.polarity
                lower_txt = temiz_icerik.lower()
                if any(x in lower_txt for x in ["yükseliş", "al", "tavan", "rekor", "olumlu", "kâr", "hedef"]): skor += 0.4
                elif any(x in lower_txt for x in ["düşüş", "sat", "taban", "zarar", "kriz", "olumsuz"]): skor -= 0.4
                
                toplam_duygu += skor
                analiz_sayisi += 1
                
                if skor > 0.05: sentiment_counts["Pozitif"] += 1
                elif skor < -0.05: sentiment_counts["Negatif"] += 1
                else: sentiment_counts["Nötr"] += 1

                for k in temiz_icerik.lower().split():
                    if len(k) > 3 and k not in STOP_WORDS: tum_kelimeler.append(k.capitalize())

                tahmin = metinden_fiyat_cikart(full_text, guncel_fiyat)
                
                kaynak_adi = "Web"
                if "twitter" in url: kaynak_adi = "Twitter"
                elif "investing" in url: kaynak_adi = "Investing"
                elif "tradingview" in url: kaynak_adi = "TradingView"
                
                kisa_icerik = (ozet[:80] + '..') if len(ozet) > 80 else ozet
                
                ozet_metin = f"[{kaynak_adi}] {kisa_icerik}"
                if tahmin:
                    kullanici_fiyat_tahminleri.append(tahmin)
                    ozet_metin = f"[{kaynak_adi}] 🎯 [HEDEF:{tahmin:.2f}] {kisa_icerik}"
                
                detayli_kaynaklar.append(ozet_metin)
                
        except Exception as e:
            print(f"Sorgu Hatası ({sorgu}): {e}")
            continue

    if analiz_sayisi == 0:
        return 0, 50, 0, ["Veri Bulunamadı"], {"Pozitif":0,"Negatif":0,"Nötr":0}, []

    ortalama_skor = toplam_duygu / analiz_sayisi 
    hype_puani = max(0, min(100, (ortalama_skor + 1) * 50))
    forum_hedefi = np.mean(kullanici_fiyat_tahminleri) if kullanici_fiyat_tahminleri else 0
    en_cok_gecenler = [k[0] for k in Counter(tum_kelimeler).most_common(7)]
    
    return ortalama_skor, hype_puani, forum_hedefi, detayli_kaynaklar, sentiment_counts, en_cok_gecenler

# --- TAHMİN ALGORİTMASI ---
def fiyat_tahmin_et(guncel_fiyat, duygu_skoru, forum_hedefi, teknik_trend):
    trend_katsayisi = 0.05 if teknik_trend == "POZİTİF" else -0.05
    if forum_hedefi > 0:
        baz_hedef = (guncel_fiyat * 0.4) + (forum_hedefi * 0.6)
        kisa = baz_hedef * (1 + trend_katsayisi)
    else:
        duygu_etkisi = duygu_skoru * 0.15
        kisa = guncel_fiyat * (1 + duygu_etkisi + trend_katsayisi)
    orta = kisa * 1.05 if teknik_trend == "POZİTİF" else kisa * 0.95
    uzun = kisa * 1.15 if teknik_trend == "POZİTİF" else kisa * 0.85
    return kisa, orta, uzun

# --- API ENDPOINT ---
@app.get("/analiz")
def tam_tesekkullu_analiz(hisse: str):
    try:
        ticker = yf.Ticker(hisse)
        df = ticker.history(period="3mo")
        if df.empty: return {"basarili": False, "mesaj": "Borsa verisi çekilemedi"}
        guncel_fiyat = df['Close'].iloc[-1]
        temel = temel_verileri_getir(ticker)
        df['MA50'] = df['Close'].rolling(window=50).mean()
        ma50 = df['MA50'].iloc[-1] if not np.isnan(df['MA50'].iloc[-1]) else guncel_fiyat
        teknik_trend = "POZİTİF" if guncel_fiyat > ma50 else "NEGATİF"

        duygu, hype, forum_hedef, icerik, dagilim, kelimeler = gelismis_hibrit_analiz(hisse, guncel_fiyat)
        kisa, orta, uzun = fiyat_tahmin_et(guncel_fiyat, duygu, forum_hedef, teknik_trend)
        
        return {
            "basarili": True,
            "meta": {"hisse_kodu": hisse, "sektor": temel["Sektor"], "guncel_fiyat": round(guncel_fiyat, 2)},
            "fiyat_verileri": {"guncel_fiyat": round(guncel_fiyat, 2), "teknik_trend": teknik_trend, "analist_hedefi_12ay": temel["Analist_Hedefi"]},
            "temel_analiz": {"fk_orani": round(temel["F_K"], 2), "pddd_orani": round(temel["PD_DD"], 2), "favok": temel["FAVOK"]},
            "duygu_analizi": { "hype_puani": round(hype, 1), "son_haberler": icerik },
            "sosyal_analiz": {
                "duygu_skoru": round(duygu, 2), "trend": teknik_trend, "kaynak_ozetleri": icerik,
                "duygu_dagilimi": dagilim, "anahtar_kelimeler": kelimeler,
                "sosyal_medya_tahmini": round(forum_hedef, 2) if forum_hedef > 0 else "Veri Yok"
            },
            "yapay_zeka_tahminleri": {"kisa_vade_1hf": round(kisa, 2), "orta_vade_1ay": round(orta, 2), "uzun_vade_6ay": round(uzun, 2)}
        }
    except Exception as e:
        return {"basarili": False, "hata_mesaji": str(e)}

if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)