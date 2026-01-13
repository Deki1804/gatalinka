# Safety Improvements - Gatalinka Functions

## Datum: 2025-01-XX

## Implementirana poboljšanja

### 1. ✅ Infinite Loop Guard

**Problem:** `generateLuckyNumbers()` funkcija nije imala zaštitu od beskonačnih loopova.

**Rješenje:**
- Dodan `MAX_ATTEMPTS = 100` guard u obje funkcije:
  - `src/gemini.ts::generateLuckyNumbers()`
  - `src/dailyReading.ts::generateLuckyNumbers()`
- Ako se ne mogu generirati 5 jedinstvenih brojeva nakon 100 pokušaja, funkcija će popuniti preostale brojeve.

**Status:** ✅ Implementirano

---

### 2. ✅ Rate Limiting po Korisniku

**Problem:** Nije bilo rate limitinga - korisnik je mogao napraviti neograničen broj poziva.

**Rješenje:**
- Kreiran novi modul `src/rateLimiting.ts` s funkcijom `checkRateLimit()`
- Implementirani limiti:
  - **10 poziva/min** po korisniku
  - **50 poziva/dan** po korisniku
  - **500 poziva/mjesec** po korisniku
- Integriran u `readCup()` funkciju
- Baca `resource-exhausted` grešku ako je limit prekoračen

**Status:** ✅ Implementirano

---

### 3. ✅ Retry Logika za Gemini API

**Problem:** Ako Gemini API vrati 429 (rate limit) ili 503 (service unavailable), funkcija je odmah bacala grešku.

**Rješenje:**
- Implementiran **exponential backoff** retry mehanizam:
  - Max 3 retry pokušaja
  - Početni delay: 1 sekunda
  - Max delay: 10 sekundi
  - Formula: `delay = min(1000ms * 2^attempt, 10000ms)`
- Retry se aktivira za:
  - HTTP 429 (rate limit)
  - HTTP 503 (service unavailable)
  - HTTP 500 (server error)
  - Greške koje sadrže "rate limit" ili "quota" u poruci
- Implementirano u:
  - `src/gemini.ts::generateReadingWithGemini()`
  - `src/dailyReading.ts::getDailyReading()`

**Status:** ✅ Implementirano

---

### 4. ✅ Billing/Quota Provjera

**Problem:** Nije bilo globalne provjere dnevnih poziva - moglo je doći do neočekivanih troškova.

**Rješenje:**
- Kreirana funkcija `checkQuotaLimit()` u `src/rateLimiting.ts`
- Provjerava **globalni dnevni limit** (10,000 poziva/dan)
- Integrirana u `readCup()` funkciju
- Baca `resource-exhausted` grešku ako je limit prekoračen

**Status:** ✅ Implementirano

---

## Konfiguracija

### Rate Limiting Konfiguracija

U `src/rateLimiting.ts`:

```typescript
const RATE_LIMIT_CONFIG = {
  callsPerMinute: 10,    // Max poziva po korisniku u minuti
  callsPerDay: 50,        // Max poziva po korisniku dnevno
  callsPerMonth: 500,     // Max poziva po korisniku mjesečno
};

const MAX_DAILY_GLOBAL_CALLS = 10000; // Globalni dnevni limit
```

### Retry Konfiguracija

U `src/gemini.ts` i `src/dailyReading.ts`:

```typescript
const MAX_RETRIES = 3;
const INITIAL_RETRY_DELAY = 1000;  // 1 sekunda
const MAX_RETRY_DELAY = 10000;      // 10 sekundi
```

---

## Testiranje

### Test Rate Limiting

1. Napravi 11 poziva u minuti → trebao bi dobiti `resource-exhausted` grešku
2. Napravi 51 poziv u danu → trebao bi dobiti `resource-exhausted` grešku

### Test Retry Logike

1. Simuliraj 429 grešku → funkcija bi trebala retry-ati 3 puta s exponential backoff
2. Provjeri logove → trebao bi vidjeti retry pokušaje

### Test Infinite Loop Guard

1. Funkcija `generateLuckyNumbers()` ne bi trebala zaglaviti čak i ako Math.random() generira iste brojeve

---

## Napomene

- Rate limiting koristi Firestore `readings` kolekciju za brojanje poziva
- Quota provjera također koristi `readings` kolekciju
- Ako Firestore provjera ne uspije, funkcija će "fail open" (dozvoliti poziv) radi sigurnosti
- U production okruženju, možda želiš "fail closed" za dodatnu sigurnost

---

## Sljedeći koraci

1. ✅ Build projekta: `npm run build` u `infra/firebase/functions`
2. ✅ Deploy: `firebase deploy --only functions`
3. ✅ Testiraj u production okruženju
4. ⚠️ Prilagodi limite prema potrebama (ako je potrebno)

