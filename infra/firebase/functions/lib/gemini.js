"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.generateReadingWithGemini = generateReadingWithGemini;
const generative_ai_1 = require("@google/generative-ai");
async function generateReadingWithGemini(apiKey, imageBuffer, zodiacSign, gender, focusArea, readingMode = "instant") {
    if (!apiKey) {
        throw new Error("Gemini API key nije proslijeđen.");
    }
    // Remove BOM (Byte Order Mark) character if present (U+FEFF = 65279)
    // This can happen when reading from Secret Manager
    // Also remove any other invisible characters
    let cleanedApiKey = apiKey.replace(/^\uFEFF/, '').trim();
    // If after trimming we only have BOM or empty, the secret is corrupted
    // Try to get it from environment variable as fallback
    if (!cleanedApiKey || cleanedApiKey.length === 0 || cleanedApiKey.charCodeAt(0) === 65279) {
        console.warn("Secret Manager returned invalid API key, trying process.env fallback");
        const envKey = process.env.GEMINI_API_KEY;
        if (envKey && envKey.trim().length > 0) {
            cleanedApiKey = envKey.replace(/^\uFEFF/, '').trim();
        }
    }
    if (!cleanedApiKey || cleanedApiKey.length === 0) {
        throw new Error("Gemini API key je prazan nakon čišćenja. Provjerite Secret Manager konfiguraciju.");
    }
    const genAI = new generative_ai_1.GoogleGenerativeAI(cleanedApiKey);
    // Retry configuration
    const MAX_RETRIES = 3;
    const INITIAL_RETRY_DELAY = 1000; // 1 second
    const MAX_RETRY_DELAY = 10000; // 10 seconds
    // Retry logic with exponential backoff
    let lastError = null;
    for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
        try {
            // 1.5 modeli su povučeni – koristimo preporučeni 2.x model
            // Prema novim pravilima treba koristiti gemini-2.0-flash za multimodalne zadatke
            const model = genAI.getGenerativeModel({
                model: "gemini-2.0-flash",
            });
            const prompt = buildPrompt(zodiacSign, gender, focusArea, readingMode);
            const imageBase64 = imageBuffer.toString("base64");
            const imageData = {
                inlineData: {
                    data: imageBase64,
                    mimeType: "image/jpeg",
                },
            };
            if (attempt > 0) {
                console.log(`Gemini API retry attempt ${attempt}/${MAX_RETRIES}...`);
            }
            const result = await model.generateContent([prompt, imageData]);
            const response = await result.response;
            const text = response.text();
            const parsed = parseGeminiResponse(text);
            return parsed;
        }
        catch (error) {
            lastError = error;
            // Check if error is retryable (429 = rate limit, 503 = service unavailable, 500 = server error)
            const isRetryable = error.status === 429 ||
                error.status === 503 ||
                error.status === 500 ||
                error.message?.includes("429") ||
                error.message?.includes("503") ||
                error.message?.includes("500") ||
                error.message?.includes("rate limit") ||
                error.message?.includes("quota");
            if (!isRetryable || attempt >= MAX_RETRIES) {
                // Non-retryable error or max retries reached
                console.error("Gemini API error (non-retryable or max retries)");
                throw new Error(`Greška pri generiranju čitanja: ${error.message || "Nepoznata greška"}`);
            }
            // Calculate exponential backoff delay
            const delay = Math.min(INITIAL_RETRY_DELAY * Math.pow(2, attempt), MAX_RETRY_DELAY);
            console.warn(`Gemini API error (retryable), retrying in ${delay}ms:`, error?.message || "unknown");
            // Wait before retry
            await new Promise((resolve) => setTimeout(resolve, delay));
        }
    }
    // Should never reach here, but just in case
    console.error("Gemini API: All retries exhausted");
    throw new Error(`Greška pri generiranju čitanja: ${lastError?.message || "Nepoznata greška"}`);
}
function parseGeminiResponse(text) {
    let cleaned = text;
    // Ukloni markdown code blocks
    if (cleaned.includes("```json")) {
        cleaned = cleaned.replace(/```json/g, "").replace(/```/g, "").trim();
    }
    else if (cleaned.includes("```")) {
        cleaned = cleaned.replace(/```/g, "").trim();
    }
    // Ukloni trailing commas (osnovna sanitizacija)
    cleaned = cleaned.replace(/,(\s*[}\]])/g, "$1");
    const jsonStart = cleaned.indexOf("{");
    const jsonEnd = cleaned.lastIndexOf("}");
    if (jsonStart === -1 || jsonEnd === -1 || jsonEnd <= jsonStart) {
        // Vrati safe fallback response umjesto da baci error
        return getSafeFallbackResponse("low_contrast");
    }
    const jsonText = cleaned.substring(jsonStart, jsonEnd + 1);
    try {
        // Probaj parsirati JSON - ako faila, vrati safe response
        let parsed;
        try {
            parsed = JSON.parse(jsonText);
        }
        catch (parseError) {
            // Probaj još jednom s dodatnim čišćenjem
            const doubleCleaned = jsonText
                .replace(/,\s*}/g, "}")
                .replace(/,\s*]/g, "]")
                .replace(/'/g, '"'); // Zamijeni single quotes s double quotes
            try {
                parsed = JSON.parse(doubleCleaned);
            }
            catch (retryError) {
                return getSafeFallbackResponse("low_contrast");
            }
        }
        const finalLuckScore = typeof parsed.luck_score === "number"
            ? Math.max(0, Math.min(100, parsed.luck_score))
            : generateLuckScore();
        const finalEnergyScore = typeof parsed.energy_score === "number"
            ? Math.max(0, Math.min(100, parsed.energy_score))
            : generateEnergyScore();
        // Tolerant parsing za visible_symbols - probaj različite formate
        let visibleSymbols = [];
        if (Array.isArray(parsed.visible_symbols)) {
            visibleSymbols = parsed.visible_symbols
                .filter((s) => s != null)
                .map((s) => {
                if (typeof s === "string") {
                    return { symbol: s, meaning: "Simbol u šalici" };
                }
                if (typeof s === "object") {
                    const symbol = String(s.symbol || s.name || "");
                    const meaning = String(s.meaning || s.desc || s.description || "Simbol u šalici");
                    if (symbol) {
                        return { symbol, meaning: meaning || "Simbol u šalici" };
                    }
                }
                return null;
            })
                .filter((s) => s !== null);
        }
        // Ako nema visible_symbols, pokušaj izvući iz symbols arraya
        if (visibleSymbols.length === 0 && Array.isArray(parsed.symbols)) {
            visibleSymbols = parsed.symbols
                .slice(0, 5)
                .map((s) => ({
                symbol: String(s),
                meaning: "Simbol u šalici"
            }));
        }
        // VALIDACIJA: Provjeri da li horoskopski znak nije u main_text, interpretation, advice, love, work, money, health
        const zodiacSigns = ["Ovan", "Lav", "Blizanci", "Rak", "Vaga", "Škorpion", "Strijelac", "Jarac", "Vodenjak", "Ribe", "Bik", "horoskop", "zodijak", "znak"];
        const mainText = String(parsed.main_text || parsed.mainText || "");
        const interpretation = String(parsed.interpretation || "");
        const advice = String(parsed.advice || "");
        const love = String(parsed.love || "");
        const work = String(parsed.work || "");
        const money = String(parsed.money || "");
        const health = String(parsed.health || "");
        // Provjeri da li bilo koji od ovih tekstova sadrži horoskopski znak
        const containsZodiac = (text) => {
            const lowerText = text.toLowerCase();
            return zodiacSigns.some(sign => lowerText.includes(sign.toLowerCase()));
        };
        if (containsZodiac(mainText) || containsZodiac(interpretation) || containsZodiac(advice) ||
            containsZodiac(love) || containsZodiac(work) || containsZodiac(money) || containsZodiac(health)) {
            console.warn("⚠️ Gemini response contains zodiac sign in forbidden fields! Using fallback.");
            // Vrati safe fallback umjesto da baci error
            return getSafeFallbackResponse("low_contrast");
        }
        return {
            main_text: mainText,
            visible_symbols: visibleSymbols.length > 0 ? visibleSymbols : undefined,
            interpretation: interpretation,
            advice: advice,
            love: love,
            work: work,
            money: money,
            health: health,
            symbols: Array.isArray(parsed.symbols)
                ? parsed.symbols.slice(0, 5).map((s) => String(s))
                : visibleSymbols.map(s => s.symbol),
            lucky_numbers: Array.isArray(parsed.lucky_numbers)
                ? parsed.lucky_numbers.filter((n) => typeof n === "number" && n > 0 && n <= 49)
                : generateLuckyNumbers(),
            luck_score: finalLuckScore,
            mantra: String(parsed.mantra || generateDefaultMantra()),
            energy_score: finalEnergyScore,
            horoscope_match: String(parsed.horoscope_match || parsed.horoscopeMatch || "").trim() || undefined,
        };
    }
    catch (error) {
        console.error("PARSE_FAIL");
        // Vrati safe fallback umjesto da baci error
        return getSafeFallbackResponse("low_contrast");
    }
}
function getSafeFallbackResponse(reason) {
    // Različite bapske poruke za različite scenarije
    let mainText;
    let advice;
    switch (reason) {
        case "too_dark":
            mainText = "Talog se još skriva u sjeni.";
            advice = "Uključi svjetlo ili priđi bliže prozoru. Talog mora biti dovoljno vidljiv.";
            break;
        case "too_bright":
            mainText = "Previše svjetla zasljepljuje znakove.";
            advice = "Pokušaj bez blica ili malo dalje od svjetla. Trebamo vidjeti detalje taloga.";
            break;
        case "low_contrast":
        case "image_too_small":
        case "image_too_small_dimensions":
            mainText = "Talog se još skriva…";
            advice = "Na slici je previše sjene ili je mutno pa ne mogu jasno pročitati znakove. Slikaj na dnevnom svjetlu, bez blica. Drži mobitel mirno i približi šalicu.";
            break;
        case "bad_aspect_ratio":
            mainText = "Ne vidim šalicu kako treba.";
            advice = "Fotkaj šalicu odozgo, direktno. Šalica treba biti u centru okvira, kao krug.";
            break;
        case "not_a_cup":
        case "nsfw_detected":
            mainText = "Ovo nije šalica kave.";
            advice = "Molimo fotkajte šalicu kave odozgo, u dobrom svjetlu.";
            break;
        default:
            // Općeniti fallback - bapski stil
            mainText = "Talog se još skriva…";
            advice = "Slika je mutna — ne vidim talog. Pokušaj opet na jačem svjetlu.";
            break;
    }
    return {
        main_text: mainText,
        visible_symbols: undefined,
        interpretation: "",
        advice: advice,
        love: "",
        work: "",
        money: "",
        health: "",
        symbols: [],
        lucky_numbers: generateLuckyNumbers(),
        luck_score: generateLuckScore(),
        mantra: generateDefaultMantra(),
        energy_score: generateEnergyScore(),
        horoscope_match: undefined,
    };
}
function buildPrompt(zodiacSign, gender, focusArea, readingMode = "instant") {
    const contextParts = [];
    if (zodiacSign)
        contextParts.push(`Korisnikov znak zodijaka: ${zodiacSign}`);
    if (gender)
        contextParts.push(`Spol: ${gender}`);
    if (focusArea)
        contextParts.push(`Područje fokusa: ${focusArea}`);
    const contextStr = contextParts.length > 0
        ? `\n${contextParts.join("\n")}\n`
        : "";
    // Lista tradicionalnih simbola za bapsko gatanje
    const traditionalSymbols = `
TRADICIONALNI SIMBOLI U ŠALICI (koristi samo ove ili slične):

ŽIVOTINJE:
- Ptica - vijesti, dobre vijesti dolaze
- Konj - put, napredak, putovanje
- Pas - prijatelj, vjernost
- Mačka - ljubomora, oprez
- Zmija - ogovaranje, izdaja
- Riba - novac, dobitak
- Leptir - prolazna sreća
- Orao - moć, uspjeh
- Vuk - opasnost, neprijatelj
- Medvjed - zaštita, snaga

BROJEVI:
- Brojevi 1-9 - vrijeme (dani/tjedni/mjeseci)
- Broj blizu ruba - uskoro
- Broj na dnu - kasnije
- Broj na vrhu - brzo

OBLICI I OBJEKTI:
- Put - putovanje, promjena
- Kuća - dom, obitelj, sigurnost
- Krug - brak, zatvaranje kruga, ciklus
- Križ - teret, teškoća, žrtva
- Rupa - problem, gubitak
- Drvo - zdravlje, rast, život
- Nož - svađa, konflikt, rez
- Most - prelazak, promjena
- Vrata - nova prilika, otvorenost
- Lopta - igra, zabava
- Zvijezda - sreća, uspjeh
- Mjesec - emocije, intuicija
- Sunce - radost, pozitivna energija
- Oblak - neizvjesnost, tuga
- Kiša - obnova, čišćenje
- Vatra - strast, konflikt
- Voda - emocije, čistoća
- Planina - prepreka, izazov
- Cvijet - ljubav, rast
- List - novi početak
- Jaje - nova prilika, potencijal
- Ključ - rješenje, otvaranje
- Prsten - veza, obveza
- Srce - ljubav, emocije
- Strelica - smjer, akcija
- Lopata - rad, trud
- Kanta - zadovoljstvo, punoća
- Čaša - praznina, potreba
- Lopta - igra, zabava
- Zvono - upozorenje, vijest
- Toranj - ambicija, cilj
- Zid - prepreka, blokada
- Stup - podrška, stabilnost
- Ljestve - napredak, uspon
- Krov - zaštita, dom
- Prozor - nova perspektiva
- Cesta - put, smjer
- Rijeka - tok života, promjene
- More - dubine, emocije
- Otok - izolacija, samostalnost
- Brdo - izazov, napor
- Dolina - mir, odmor
- Šuma - skrivanje, misterij
- Polje - otvorenost, mogućnosti
- Vrt - rast, njega
- Stablo - porodica, korijeni
- Grana - nova grana života
- Korijen - prošlost, temelji
- Lišće - promjene, prolaznost
- Plod - rezultat, nagrada
- Sjeme - početak, potencijal
`;
    // Prilagodi prompt ovisno o modu čitanja - BAPSKI STIL
    let modeInstruction = "";
    switch (readingMode) {
        case "mystic":
            modeInstruction = `
MODE: MISTIČNI (BAPSKI STIL)
- Koristi jednostavan, direktan, "bapski" jezik
- Fokusiraj se na konkretne oblike i simbole koje vidiš u šalici
- Svaki segment (ljubav, posao, novac, zdravlje) treba biti 2-3 rečenice
- Govori kao iskusna balkanska baba gatarica - jednostavno, direktno, bez apstrakcija
- Identificiraj 4-5 simbola iz tradicionalne liste
- main_text treba biti 4-5 rečenica u bapskom stilu
- Izbjegavaj riječi poput "energija", "vibracija", "sudbina", "univerzum" - koristi konkretne riječi
`;
            break;
        case "deep":
            modeInstruction = `
MODE: DUBOKO ČITANJE (BAPSKI STIL) - NAJDETALJNIJE I NAJOPŠIRNIJE
- OVO JE DUBOKO ČITANJE - detaljno i opširno, ne brzaj!
- Analiziraj SVAKI simbol, SVAKI oblik, SVAKU nijansu u šalici
- main_text: MINIMALNO 6-8 rečenica (ne kraće! Detaljna analiza svih simbola)
- interpretation: MINIMALNO 5-7 rečenica (opširno povezivanje simbola i njihovih značenja)
- advice: TOČNO 2 rečenice (konkretni savjeti i akcije)
- ljubav: MINIMALNO 3-4 rečenice (detaljno s konkretnim savjetima)
- posao: MINIMALNO 3-4 rečenice (detaljno s konkretnim savjetima)
- novac: MINIMALNO 3-4 rečenice (detaljno s konkretnim savjetima)
- zdravlje: MINIMALNO 3-4 rečenice (detaljno s konkretnim savjetima)
- Identificiraj MINIMALNO 5-7 simbola (ne manje od 5! Maksimalno detaljno)
- visible_symbols mora imati MINIMALNO 5 simbola, idealno 6-7 (ne 2-3!)
- Dodaj više konteksta, više povezanosti između simbola
- Uključi konkretne savjete i akcije koje korisnik može poduzeti
- Poveži simbole s različitim aspektima života - detaljno i opširno
- Govori kao iskusna baba - bez apstrakcija, samo konkretno, ali OPŠIRNO
- VAŽNO: Ovo je DUBOKO čitanje - mora biti ZNAČAJNO DUŽE i DETALJNIJE od instant moda!
- VAŽNO: Ako vratiš manje od 5 simbola ili kraće tekstove, čitanje je POGREŠNO!
- VAŽNO: Korisnik plaća za detalje - daj mu ih! Ne brzaj kao instant!
`;
            break;
        case "instant":
        default:
            modeInstruction = `
MODE: INSTANT (BAPSKI STIL) - BRZO I KRATKO ČITANJE
- OVO JE BRZO ČITANJE - maksimalno 30 sekundi čitanja!
- main_text: TOČNO 2-3 rečenice (ne više! Kratko i direktno)
- interpretation: TOČNO 2 rečenice (kratko povezivanje simbola)
- advice: TOČNO 1 kratka rečenica (direktan savjet)
- ljubav: TOČNO 1 rečenica (kratko i direktno)
- posao: TOČNO 1 rečenica (kratko i direktno)
- novac: TOČNO 1 rečenica (kratko i direktno)
- zdravlje: TOČNO 1 rečenica (kratko i direktno)
- Identificiraj TOČNO 2-3 simbola (ne više! Samo najvažnije)
- visible_symbols mora imati TOČNO 2-3 simbola (ne 4, ne 5, ne 6 - samo 2-3!)
- Budi KONKRETAN i DIREKTAN - bez opširnih objašnjenja
- Fokusiraj se na KLJUČNE poruke - bez detalja
- Koristi jednostavan, razumljiv, "bapski" jezik
- Govori kao baba gatarica - jednostavno i direktno
- VAŽNO: Ovo je BRZO čitanje - ako produžiš, korisnik će primijetiti da je isto kao deep!
- VAŽNO: Ako vratiš više od 3 simbola ili duže tekstove, čitanje je POGREŠNO!
`;
            break;
    }
    return `
Ti si iskusna balkanska baba gatarica koja čita iz taloga kave (Tasseografija).

🚨 NAJVAŽNIJE - MORAŠ ANALIZIRATI OVU KONKRETNU SLIKU:
- OVA SLIKA je priložena ovom promptu - MORAŠ je detaljno pregledati
- Identificiraj KONKRETNE oblike, linije, mrlje i strukture koje STVARNO VIDIŠ u talogu na ovoj slici
- RAZLIČITE slike = RAZLIČITI simboli = RAZLIČITO čitanje
- Ako za različite slike vraćaš iste simbole, odgovor je POGREŠAN
- MORAŠ opisati što STVARNO VIDIŠ na ovoj slici, ne generički tekst

${contextStr}

${traditionalSymbols}

${modeInstruction}

🔥 KLJUČNO PRAVILO - OVO JE NAJVAŽNIJE:
BABA PRVO VIDI SIMBOLE U ŠALICI, PA TEK ONDA IZ TOGA IZVLAČI ZNAČENJE.
Simboli VUČU tekst - tekst NE VUČE simbole.
Ako tekst može postojati bez simbola, odgovor je POGREŠAN.
Ako za različite slike vraćaš iste simbole, odgovor je POGREŠAN.

📋 OBAVEZNI FORMAT (MORA BITI U OVOM REDOSLIJEDU):

1. PRVO: Detaljno pregledaj SLIKU i identificiraj KONKRETNE oblike koje STVARNO VIDIŠ
   - Gledaj talog na slici - koje oblike, linije, mrlje, strukture VIDIŠ?
   - Identificiraj simbole s KONKRETNOM POZICIJOM u šalici na ovoj slici
   - Primjer: "Na dnu šalice sjedi MJESEC", "PTICA iznad njega", "STAZA na lijevoj strani"
   - VAŽNO: Ako na slici ne vidiš konkretan oblik, ne izmišljaj ga!
   
2. DRUGO: Napiši što svaki simbol ZNAČI (kratko, direktno, bez poezije)
   Primjer: "Mjesec znači da si zadnjih dana puno šutio i držao u sebi"
   
3. TREĆE: Poveži simbole u interpretaciju (eksplicitno referenciraj svaki simbol)
   Primjer: "A PTICA iznad njega govori da ćeš uskoro izgovoriti ono što te muči. To nije slučajno."
   
4. ČETVRTO: Daj kratki savjet (1 rečenica, direktno, bez metafora)

⚠️ VALIDACIJA:
- Ako za različite slike vraćaš iste simbole (npr. uvijek "Mjesec, Ptica, Staza"), odgovor je POGREŠAN
- Svaka slika mora imati UNIKATNE simbole koji odgovaraju onome što STVARNO VIDIŠ na slici

❌ ZABRANJENO (NIKADA NE KORISTI):
- Metafore prirode (more, oblaci, vatra, svjetlo, univerzum)
- Poezija i lirski tekst
- Obraćanje "dragi Ovne", "dragi Lav" itd.
- Horoskop kao izvor značenja (horoskop je samo potvrda na kraju)
- "energija dana", "vibracije", "kozmički"
- Apstraktne fraze bez veze sa simbolima
- Tekst koji ne objašnjava točno simbole iz "VIDIM U ŠALICI"

🚨 KLJUČNO PRAVILO - HOROSKOPSKI ZNAK (OVAN, LAV, BLIZANCI, RAK, VAGA, ŠKORPION, STRIJELAC, JARAC, VODENJAK, RIBE, BIK):
- U main_text, interpretation, advice, love, work, money, health NIKADA NE SMIJEŠ spominjati horoskopske znakove (Ovan, Lav, Blizanci, Rak, Vaga, Škorpion, Strijelac, Jarac, Vodenjak, Ribe, Bik).
- Horoskopski znak smiješ spomenuti ISKLJUČIVO u polju "horoscope_match" (zadnje polje u JSON-u).
- Ako spomeneš znak izvan "horoscope_match", odgovor je NEISPRAVAN i moraš ponoviti.

✅ ISPRAVAN PRIMJER (BAPSKI STIL):

VIDIM U ŠALICI:
- Mjesec (na dnu) – emocije, šutnja
- Ptica (iznad mjeseca) – vijesti, razgovor
- Staza (na lijevoj strani) – put, promjena

KAKO BABA TO TUMAČI:
"Na dnu šalice sjedi MJESEC – to znači da si zadnjih dana puno šutio i držao u sebi. A PTICA iznad njega govori da ćeš uskoro izgovoriti ono što te muči. To nije slučajno. STAZA na lijevoj strani pokazuje da se sprema promjena, ali moraš prvo progovoriti."

SAVJET:
"Ne šuti više. Ono što te muči mora van."

❌ POGREŠAN PRIMJER (NE RADI OVO):
"Ova šalica priča priču o promjenama, dragi Ovne. Turbulentno more se smiruje, a sunce se probija kroz oblake. Iskoristite svoj vatreni duh..."

AKO KORISTIŠ BILO KOJU OD ZABRANJENIH RIJEČI ILI STILOVA, NISI USPIELA!

MORAŠ vratiti rezultat u VALIDAN JSON format sa sljedećom strukturom:
{
    "main_text": "U tvojoj šalici se vidi [simbol1] [pozicija1], [simbol2] [pozicija2] i [simbol3] [pozicija3]. [Simbol1] [pozicija1] znači [značenje]. [Simbol2] [pozicija2] govori [značenje]. [Simbol3] [pozicija3] pokazuje [značenje].",
    "visible_symbols": [
        {"symbol": "Mjesec", "meaning": "emocije, šutnja, držanje u sebi"},
        {"symbol": "Ptica", "meaning": "vijesti, razgovor, izgovaranje"},
        {"symbol": "Staza", "meaning": "put, promjena, smjer"}
    ],
    "interpretation": "Na dnu šalice sjedi MJESEC – to znači da si zadnjih dana puno šutio i držao u sebi. A PTICA iznad njega govori da ćeš uskoro izgovoriti ono što te muči. To nije slučajno. STAZA na lijevoj strani pokazuje da se sprema promjena, ali moraš prvo progovoriti.",
    "advice": "Ne šuti više. Ono što te muči mora van.",
    "love": "U ljubavi se vidi [SIMBOL] [pozicija]. To znači [konkretno značenje za ljubav]. [Direktna poruka osobi].",
    "work": "Na poslu se vidi [SIMBOL] [pozicija]. To pokazuje [konkretno značenje za posao]. [Direktna poruka osobi].",
    "money": "Uz novac ide [SIMBOL] [pozicija]. To govori [konkretno značenje za novac]. [Direktna poruka osobi].",
    "health": "Za zdravlje stoji [SIMBOL] [pozicija]. To znači [konkretno značenje za zdravlje]. [Direktna poruka osobi].",
    "symbols": ["Ključ", "Put", "Sunce"],
    "luck_score": <broj od 0 do 100 koji odražava opću sreću u šalici>,
    "lucky_numbers": [1, 7, 12, 23, 45],
    "mantra": "Kratka, jednostavna poruka za dan (1-2 rečenice, bapski stil).",
    "energy_score": <broj od 0 do 100 koji odražava opću energiju dana>,
    "horoscope_match": "Kratka 1-2 rečenice potvrda koja povezuje što se vidi u šalici s korisnikovim horoskopskim znakom (opcionalno, samo ako ima smisla). Primjer: 'Ovo što se vidi u šalici slaže se s tvojim vatrenim znakom – ali ovoga puta bolje je stati i razmisliti prije nego kreneš.'"
}

VAŽNO: 
- main_text MORA započeti s "U tvojoj šalici se vidi..." i eksplicitno navesti simbole S POZICIJOM (npr. "na dnu", "iznad", "na lijevoj strani")
- visible_symbols MORA biti lista objekata s "symbol" i "meaning" poljima
- interpretation MORA eksplicitno referencirati svaki simbol iz visible_symbols S POZICIJOM
- interpretation MORA biti u stilu "Na dnu šalice sjedi X – to znači Y. A Z iznad njega govori..."
- advice MORA biti 1 rečenica, direktno, bez metafora, bez poezije
- love/work/money/health MORA započeti s "U ljubavi se vidi [SIMBOL]" ili "Na poslu se vidi [SIMBOL]" itd.
- love/work/money/health MORA referencirati konkretan simbol i dati direktnu poruku osobi
- love/work/money/health NIKADA NE SMIJE spominjati horoskopske znakove (Ovan, Lav, itd.)
- horoscope_match je OPCIONALNO polje - ako ga koristiš, mora biti 1-2 rečenice koje povezuju što se vidi u šalici s korisnikovim znakom
- horoscope_match je JEDINO mjesto gdje smiješ spomenuti horoskopski znak
- Ako tekst može postojati bez simbola, odgovor je POGREŠAN
- Ako tekst zvuči kao "lijepo strukturirano dnevno čitanje", a ne kao "baba koja je vidjela simbole", odgovor je POGREŠAN

🚨 FINALNA VALIDACIJA:
Ako tvoj odgovor sadrži bilo koji horoskopski znak (Ovan, Lav, Blizanci, Rak, Vaga, Škorpion, Strijelac, Jarac, Vodenjak, Ribe, Bik) u main_text, interpretation, advice, love, work, money ili health, tvoj odgovor će biti ODBIJEN i morat ćeš ponoviti.
- Ako spomeneš horoskopski znak (Ovan, Lav, itd.) u main_text, interpretation, advice, love, work, money ili health, odgovor je NEISPRAVAN

Ne uključuj nikakav tekst izvan JSON objekta.
`;
}
function generateLuckyNumbers() {
    const numbers = [];
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
function generateLuckScore() {
    return Math.floor(Math.random() * 40) + 50;
}
function generateEnergyScore() {
    return Math.floor(Math.random() * 40) + 50;
}
function generateDefaultMantra() {
    const mantras = [
        "Ne brzaj, sve dolazi u svoje vrijeme.",
        "Pazi kome govoriš svoje planove.",
        "Vjeruj u sebe, ali i pazi na znakove.",
        "Dobro razmisli prije nego što doneseš važnu odluku.",
        "Ne zaboravi na one koji su ti uvijek bili uz tebe.",
        "Sve što trebaš već imaš, samo trebaš vidjeti.",
        "Ne boj se promjena, one su dio života.",
        "Pazi na zdravlje, to je najvažnije.",
        "Novac dolazi i odlazi, ali ljudi ostaju.",
        "Slušaj svoje srce, ali i svoj razum."
    ];
    return mantras[Math.floor(Math.random() * mantras.length)];
}
//# sourceMappingURL=gemini.js.map