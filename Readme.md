# 📈 FinSight-AI: Smart Stock Analysis & Prediction System

![Java](https://img.shields.io/badge/Java-17%2B-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green) ![Python](https://img.shields.io/badge/Python-3.11%2B-blue) ![FastAPI](https://img.shields.io/badge/FastAPI-0.95%2B-teal) ![Architecture](https://img.shields.io/badge/Architecture-Microservices-purple)

**FinSight-AI**, finansal verileri yapay zeka destekli duygu analizi (Sentiment Analysis) ve teknik göstergelerle birleştirerek hisse senedi tahminleri yapan modern bir mikroservis projesidir.

Proje, bir **Business Analyst** titizliğiyle seçilmiş finansal metrikleri (F/K, FAVÖK), bir **Full Stack Developer** vizyonuyla kurgulanmış ölçeklenebilir bir mimari üzerinde sunar.

---

## 🏗️ Mimari (Architecture)

Sistem **Mikroservis Mimarisi** üzerine kurulmuştur ve iki ana bileşenden oluşur:

1.  **AI Service (Python & FastAPI):** "İşçi" (Worker) katmanı. Veriyi çeker, işler, NLP ile haberleri analiz eder ve matematiksel modellerle fiyat tahmini üretir.
2.  **Core Backend (Java & Spring Boot):** "Yönetici" (Orchestrator) katmanı. İstemci ile konuşur, iş mantığını yürütür ve AI servisini yönetir.

```mermaid
graph LR
    Client[Client / Browser] -- HTTP Request --> Java[Spring Boot Backend]
    Java -- REST API --> Python[Python AI Service]
    Python -- Fetch Data --> Yahoo[Yahoo Finance API]
    Python -- Scraping --> Google[Google News]
    Python -- JSON Response --> Java
    Java -- JSON Response --> Client