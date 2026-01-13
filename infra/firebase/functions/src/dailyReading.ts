import * as functions from "firebase-functions";
import { CallableContext } from "firebase-functions/v1/https";
import * as admin from "firebase-admin";
import { GoogleGenerativeAI } from "@google/generative-ai";
import { checkQuotaLimit } from "./rateLimiting";

export interface DailyReadingResponse {
  main_text: string;
  love: string;
  work: string;
  money: string;
  health: string;
  symbols: string[];
  lucky_numbers: number[];
  luck_score: number;
  mantra: string;
  energy_score: number;
  date: string; // YYYY-MM-DD format
}

export async function getDailyReading(
  data: { zodiacSign?: string; gender?: string },
  context: CallableContext,
  geminiApiKey: string
): Promise<DailyReadingResponse> {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "Korisnik mora biti prijavljen."
    );
  }

  const userId = context.auth.uid;
  const { zodiacSign, gender } = data;

  // Quota/billing check
  const quotaCheck = await checkQuotaLimit();
  if (!quotaCheck.allowed) {
    throw new functions.https.HttpsError(
      "resource-exhausted",
      quotaCheck.reason || "Dostignut je limit poziva."
    );
  }

  // Provjeri da li korisnik već ima dnevno čitanje za danas
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const todayStr = today.toISOString().split("T")[0]; // YYYY-MM-DD

  const db = admin.firestore();
  const dailyReadingsRef = db.collection(`users/${userId}/dailyReadings`);
  
  // Provjeri da li postoji čitanje za danas
  const todayReading = await dailyReadingsRef
    .where("date", "==", todayStr)
    .limit(1)
    .get();

  if (!todayReading.empty) {
    // Vrati postojeće čitanje, ali prvo provjeri validaciju
    const existingReading = todayReading.docs[0].data() as DailyReadingResponse;
    
    // VALIDACIJA: Provjeri da li postojeće čitanje sadrži horoskopske znakove
    const horoscopeSigns = [
      "Ovan", "Ovnov", "Ovna", "Ovne", "Ovni",
      "Lav", "Lava", "Lave", "Lavi",
      "Blizanci", "Blizanca", "Blizance", "Blizancima",
      "Rak", "Raka", "Raku", "Raci",
      "Vaga", "Vage", "Vagi",
      "Škorpion", "Škorpiona", "Škorpionu", "Škorpioni",
      "Strijelac", "Strijelca", "Strijelcu", "Strijelci",
      "Jarac", "Jarca", "Jarcu", "Jarci",
      "Vodenjak", "Vodenjaka", "Vodenjaku", "Vodenjaci",
      "Ribe", "Riba", "Ribama",
      "Bik", "Bika", "Biku", "Bikovi",
      "dragi Ovne", "vatreni znak", "tvoj znak", "tvoja znaka",
      "Sine Ovnov", "Sine Lava", "Sine Bik", "Sine Rak"
    ];
    
    const checkForHoroscope = (text: string): boolean => {
      if (!text) return false;
      const lowerText = text.toLowerCase();
      return horoscopeSigns.some(sign => lowerText.includes(sign.toLowerCase()));
    };
    
    // Provjeri sve polja koja NE SMIJU imati horoskopski jezik
    const hasHoroscopeInForbiddenFields = 
      checkForHoroscope(existingReading.main_text || "") ||
      checkForHoroscope(existingReading.love || "") ||
      checkForHoroscope(existingReading.work || "") ||
      checkForHoroscope(existingReading.money || "") ||
      checkForHoroscope(existingReading.health || "") ||
      checkForHoroscope(existingReading.mantra || "");
    
    if (hasHoroscopeInForbiddenFields) {
      console.warn("⚠️ Existing daily reading contains horoscope signs! Invalidating cache and generating new reading.");
      // Obriši stari cache i generiraj novo čitanje
      await todayReading.docs[0].ref.delete();
      // Nastavi dalje da generira novo čitanje
    } else {
      console.log("✅ Returning existing daily reading for today (validated)");
      return existingReading;
    }
  }

  // Generiraj novo dnevno čitanje
  if (!geminiApiKey) {
    throw new Error("Gemini API nije konfiguriran.");
  }

  // Remove BOM (Byte Order Mark) character if present (U+FEFF = 65279)
  // This can happen when reading from Secret Manager
  const cleanedApiKey = geminiApiKey.replace(/^\uFEFF/, '').trim();
  
  if (!cleanedApiKey) {
    throw new Error("Gemini API key je prazan nakon čišćenja.");
  }

  const genAI = new GoogleGenerativeAI(cleanedApiKey);
  
  // Retry configuration
  const MAX_RETRIES = 3;
  const INITIAL_RETRY_DELAY = 1000; // 1 second
  const MAX_RETRY_DELAY = 10000; // 10 seconds

  // Retry logic with exponential backoff
  let lastError: any = null;
  
  for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
    try {
      const model = genAI.getGenerativeModel({
        model: "gemini-2.0-flash",
      });

      const prompt = buildDailyReadingPrompt(todayStr, zodiacSign, gender);
      
      if (attempt > 0) {
        console.log(`Daily reading retry attempt ${attempt}/${MAX_RETRIES}...`);
      } else {
        console.log("Generating daily reading for zodiac:", zodiacSign);
      }
      
      const result = await model.generateContent(prompt);
      const response = await result.response;
      const text = response.text();

      const parsed = parseDailyReadingResponse(text, todayStr);
      
      // Spremi u Firestore
      await dailyReadingsRef.add({
        ...parsed,
        userId: userId,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      console.log("Daily reading generated and saved for", todayStr);
      
      // Success - return parsed result
      return parsed;
    } catch (error: any) {
      lastError = error;
      
      // Check if error is retryable (429 = rate limit, 503 = service unavailable, 500 = server error)
      const isRetryable = 
        error.status === 429 || 
        error.status === 503 || 
        error.status === 500 ||
        error.message?.includes("429") ||
        error.message?.includes("503") ||
        error.message?.includes("500") ||
        error.message?.includes("rate limit") ||
        error.message?.includes("quota");

      if (!isRetryable || attempt >= MAX_RETRIES) {
        // Non-retryable error or max retries reached
        console.error("Daily reading Gemini API error (non-retryable or max retries):", error);
        throw new Error(`Greška pri generiranju dnevnog čitanja: ${error.message || "Nepoznata greška"}`);
      }

      // Calculate exponential backoff delay
      const delay = Math.min(
        INITIAL_RETRY_DELAY * Math.pow(2, attempt),
        MAX_RETRY_DELAY
      );

      console.warn(
        `Daily reading Gemini API error (retryable), retrying in ${delay}ms:`,
        error.message || error
      );

      // Wait before retry
      await new Promise((resolve) => setTimeout(resolve, delay));
    }
  }

  // Should never reach here, but just in case
  console.error("Daily reading Gemini API: All retries exhausted");
  throw new Error(`Greška pri generiranju dnevnog čitanja: ${lastError?.message || "Nepoznata greška"}`);
}

function buildDailyReadingPrompt(
  date: string,
  zodiacSign?: string,
  gender?: string
): string {
  const contextParts: string[] = [];
  if (zodiacSign) contextParts.push(`Korisnikov znak zodijaka: ${zodiacSign}`);
  if (gender) contextParts.push(`Spol: ${gender}`);

  const contextStr = contextParts.length > 0
    ? `\n${contextParts.join("\n")}\n`
    : "";

  return `
Ti si stara balkanska baba gatarica koja daje dnevne poruke i savjete. Danas je ${date}.

${contextStr}

🔥 KLJUČNO PRAVILO - DNEVNA PORUKA (BEZ SLIKE ŠALICE):
- Ovo je DNEVNA PORUKA, ne čitanje iz šalice (nema fotografije)
- Govori kao baba koja daje dnevne savjete i poruke
- Direktno, konkretno, bez poezije
- Koristi tradicionalne simbole (ptica, konj, ključ, mjesec, sunce, staza, srce, knjiga, itd.)
- Simboli dolaze iz "energije dana", ne iz šalice

❌ ZABRANJENO (NIKADA NE KORISTI - STRICT VALIDATION):
- "U tvojoj šalici se vidi..." - NEMA SLIKE ŠALICE!
- "U šalici", "šalica", "talog", "dno šalice", "rub šalice" - NIKADA!
- Horoskopski znakovi (Ovan, Ovnov, Ovna, Ovne, Lav, Lava, Blizanci, Rak, Vaga, Škorpion, Strijelac, Jarac, Vodenjak, Ribe, Bik) u main_text, love, work, money, health, mantra
- Obraćanje "dragi Ovne", "Sine Ovnov", "vatreni znak", "tvoj znak", "tvoja znaka" itd.
- "vibracije", "kozmički", "univerzum", "kozmička energija"
- Metafore prirode (more, oblaci, vatra, svjetlo) bez veze sa simbolima
- Poezija i lirski tekst
- Apstraktne fraze bez simbola
- BILO KAKVO spominjanje horoskopskog znaka u glavnom tekstu - čak i kao kontekst!

✅ ISPRAVAN PRIMJER (DNEVNA PORUKA):
"Danas te prate simboli: Ptica, Ključ i Mjesec. Ptica donosi vijest koja stiže brzo. Ključ pokazuje rješenje ili izlaz koji ćeš pronaći. Mjesec govori o emocijama ili tajni koja se otkriva."

MORAŠ vratiti rezultat u VALIDAN JSON format sa sljedećom strukturom:
{
    "main_text": "Danas te prate simboli: [simbol1], [simbol2] i [simbol3]. [Simbol1] donosi [značenje]. [Simbol2] pokazuje [značenje]. [Simbol3] govori [značenje].",
    "love": "Danas u ljubavi [SIMBOL] donosi [konkretno značenje za ljubav]. [Direktna poruka osobi].",
    "work": "Na poslu danas [SIMBOL] pokazuje [konkretno značenje za posao]. [Direktna poruka osobi].",
    "money": "S financijama danas [SIMBOL] govori [konkretno značenje za novac]. [Direktna poruka osobi].",
    "health": "Za zdravlje danas [SIMBOL] znači [konkretno značenje za zdravlje]. [Direktna poruka osobi].",
    "symbols": ["Ptica", "Ključ", "Mjesec"],
    "luck_score": <broj od 0 do 100 koji odražava opću sreću dana>,
    "lucky_numbers": [1, 7, 12, 23, 45],
    "mantra": "Kratka, jednostavna poruka za dan (1-2 rečenice, bapski stil).",
    "energy_score": <broj od 0 do 100 koji odražava opću energiju dana>
}

VAŽNO: 
- main_text MORA započeti s "Danas te prate simboli:" ili "Današnja poruka:" - NIKADA "U tvojoj šalici se vidi..."
- love/work/money/health MORA započeti s "Danas u ljubavi...", "Na poslu danas...", "S financijama danas...", "Za zdravlje danas..."
- love/work/money/health MORA referencirati konkretan simbol i dati direktnu poruku osobi
- NIKADA NE SMIJEŠ spominjati "šalica", "talog", "dno", "rub" ili bilo što vezano uz šalicu
- NIKADA NE SMIJEŠ spominjati horoskopske znakove (Ovan, Lav, itd.) u main_text, love, work, money, health
- luck_score i energy_score moraju biti različiti svaki dan
- Ne koristi uvijek isti broj!

Ne uključuj nikakav tekst izvan JSON objekta.
`;
}

function parseDailyReadingResponse(text: string, date: string): DailyReadingResponse {
  let cleaned = text;
  if (cleaned.includes("```json")) {
    cleaned = cleaned.replace(/```json/g, "").replace(/```/g, "").trim();
  } else if (cleaned.includes("```")) {
    cleaned = cleaned.replace(/```/g, "").trim();
  }

  const jsonStart = cleaned.indexOf("{");
  const jsonEnd = cleaned.lastIndexOf("}");

  if (jsonStart === -1 || jsonEnd === -1 || jsonEnd <= jsonStart) {
    throw new Error("Gemini API nije vratio validan JSON odgovor.");
  }

  const jsonText = cleaned.substring(jsonStart, jsonEnd + 1);

  try {
    const parsed = JSON.parse(jsonText);
    
    // VALIDACIJA: Provjeri da li tekst sadrži horoskopske znakove
    const horoscopeSigns = [
      "Ovan", "Ovnov", "Ovna", "Ovne", "Ovni",
      "Lav", "Lava", "Lave", "Lavi",
      "Blizanci", "Blizanca", "Blizance", "Blizancima",
      "Rak", "Raka", "Raku", "Raci",
      "Vaga", "Vage", "Vagi",
      "Škorpion", "Škorpiona", "Škorpionu", "Škorpioni",
      "Strijelac", "Strijelca", "Strijelcu", "Strijelci",
      "Jarac", "Jarca", "Jarcu", "Jarci",
      "Vodenjak", "Vodenjaka", "Vodenjaku", "Vodenjaci",
      "Ribe", "Riba", "Ribama",
      "Bik", "Bika", "Biku", "Bikovi",
      "dragi Ovne", "vatreni znak", "tvoj znak", "tvoja znaka",
      "Sine Ovnov", "Sine Lava", "Sine Bik", "Sine Rak"
    ];
    
    const checkForHoroscope = (text: string): boolean => {
      if (!text) return false;
      const lowerText = text.toLowerCase();
      return horoscopeSigns.some(sign => lowerText.includes(sign.toLowerCase()));
    };
    
    // Provjeri sve polja koja NE SMIJU imati horoskopski jezik
    const hasHoroscopeInForbiddenFields = 
      checkForHoroscope(parsed.main_text || "") ||
      checkForHoroscope(parsed.love || "") ||
      checkForHoroscope(parsed.work || "") ||
      checkForHoroscope(parsed.money || "") ||
      checkForHoroscope(parsed.health || "") ||
      checkForHoroscope(parsed.mantra || "");
    
    if (hasHoroscopeInForbiddenFields) {
      console.warn("⚠️ Daily reading contains horoscope signs in forbidden fields! Returning fallback.");
      // Vrati fallback čitanje bez horoskopskog jezika
      const fallbackSymbols = ["Ptica", "Ključ", "Mjesec"];
      const fallbackLuckScore = Math.floor(Math.random() * 40) + 50;
      const fallbackEnergyScore = Math.floor(Math.random() * 40) + 50;
      
      return {
        main_text: `Danas te prate simboli: ${fallbackSymbols[0]}, ${fallbackSymbols[1]} i ${fallbackSymbols[2]}. ${fallbackSymbols[0]} donosi vijest koja stiže brzo. ${fallbackSymbols[1]} pokazuje rješenje ili izlaz. ${fallbackSymbols[2]} govori o emocijama ili tajni.`,
        love: `Danas u ljubavi ${fallbackSymbols[0]} donosi vijest koja se tiče emocija. Pričekaj i vidi što donosi.`,
        work: `Na poslu danas ${fallbackSymbols[1]} pokazuje da će se pojaviti rješenje ili nova prilika. Budi spreman.`,
        money: `S financijama danas ${fallbackSymbols[2]} govori da će emocije ili tajne utjecati na financije. Pazi kome se povjeravaš.`,
        health: `Za zdravlje danas ${fallbackSymbols[0]} znači da će stići vijest koja se tiče zdravlja. Slušaj svoje tijelo.`,
        symbols: fallbackSymbols,
        lucky_numbers: generateLuckyNumbers(),
        luck_score: fallbackLuckScore,
        mantra: "Danas donosi nove mogućnosti. Budi otvoren za promjene.",
        energy_score: fallbackEnergyScore,
        date: date,
      };
    }
    
    const finalLuckScore = typeof parsed.luck_score === "number"
      ? Math.max(0, Math.min(100, parsed.luck_score))
      : Math.floor(Math.random() * 40) + 50;
    
    const finalEnergyScore = typeof parsed.energy_score === "number"
      ? Math.max(0, Math.min(100, parsed.energy_score))
      : Math.floor(Math.random() * 40) + 50;

    // Helper funkcija za filtriranje "U šalici" iz teksta (za slučaj da AI još uvijek koristi stari format)
    const filterCupReferences = (text: string): string => {
      if (!text) return text;
      return text
        .replace(/u tvojoj šalici se vidi/gi, "Danas te prate simboli:")
        .replace(/u šalici se vidi/gi, "Danas te prate simboli:")
        .replace(/u ljubavi se vidi/gi, "Danas u ljubavi")
        .replace(/na poslu se vidi/gi, "Na poslu danas")
        .replace(/uz novac ide/gi, "S financijama danas")
        .replace(/za zdravlje stoji/gi, "Za zdravlje danas")
        .replace(/u šalici/gi, "danas")
        .replace(/šalici/gi, "danas")
        .replace(/talog/gi, "energija")
        .replace(/na dnu/gi, "danas")
        .replace(/uz rub/gi, "danas")
        .replace(/u sredini/gi, "danas");
    };
    
    return {
      main_text: filterCupReferences(parsed.main_text || "Danas donosi nove mogućnosti i pozitivne promjene."),
      love: filterCupReferences(parsed.love || "Energija ljubavi je jaka danas."),
      work: filterCupReferences(parsed.work || "Poslovni uspjeh čeka te."),
      money: filterCupReferences(parsed.money || "Financijska energija je pozitivna."),
      health: filterCupReferences(parsed.health || "Tvoja vitalnost je visoka."),
      symbols: Array.isArray(parsed.symbols) ? parsed.symbols : ["Zvijezda", "Srce", "Put"],
      lucky_numbers: Array.isArray(parsed.lucky_numbers)
        ? parsed.lucky_numbers
        : generateLuckyNumbers(),
      luck_score: finalLuckScore,
      mantra: filterCupReferences(parsed.mantra || "Danas je dan za nove mogućnosti i pozitivne promjene."),
      energy_score: finalEnergyScore,
      date: date,
    };
  } catch (error: any) {
    console.error("Failed to parse Gemini JSON response:", error);
    console.error("Response text:", jsonText.substring(0, 500));
    throw new Error(`Nije moguće parsirati JSON odgovor: ${error.message}`);
  }
}

function generateLuckyNumbers(): number[] {
  const numbers: number[] = [];
  const MAX_ATTEMPTS = 100; // Infinite loop guard
  let attempts = 0;
  
  while (numbers.length < 5 && attempts < MAX_ATTEMPTS) {
    attempts++;
    const num = Math.floor(Math.random() * 49) + 1;
    if (!numbers.includes(num)) {
      numbers.push(num);
    }
  }
  
  // If we couldn't generate 5 unique numbers, fill with random numbers
  // Use a more efficient approach: generate all possible numbers and shuffle
  if (numbers.length < 5) {
    console.warn(`generateLuckyNumbers: Only generated ${numbers.length} unique numbers after ${MAX_ATTEMPTS} attempts`);
    // Generate all numbers from 1 to 49, shuffle, and take first 5
    const allNumbers = Array.from({ length: 49 }, (_, i) => i + 1);
    // Remove already generated numbers
    const remainingNumbers = allNumbers.filter(n => !numbers.includes(n));
    // Shuffle and take what we need
    for (let i = remainingNumbers.length - 1; i > 0 && numbers.length < 5; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [remainingNumbers[i], remainingNumbers[j]] = [remainingNumbers[j], remainingNumbers[i]];
      if (!numbers.includes(remainingNumbers[i])) {
        numbers.push(remainingNumbers[i]);
      }
    }
  }
  
  return numbers.sort((a, b) => a - b);
}

