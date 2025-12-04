import { GoogleGenerativeAI } from "@google/generative-ai";

export interface GeminiReadingResult {
  main_text: string;
  love: string;
  work: string;
  money: string;
  health: string;
  symbols: string[];
  lucky_numbers: number[];
  luck_score: number;
  mantra: string; // Dnevna mantra/poruka
  energy_score: number; // 0-100, opća energija dana
}

export async function generateReadingWithGemini(
  apiKey: string,
  imageBuffer: Buffer,
  zodiacSign?: string,
  gender?: string,
  focusArea?: string,
  readingMode: string = "instant"
): Promise<GeminiReadingResult> {
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
  
  const genAI = new GoogleGenerativeAI(cleanedApiKey);

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

    console.log("Calling Gemini API for coffee cup reading...");
    const result = await model.generateContent([prompt, imageData]);
    const response = await result.response;
    const text = response.text();

    console.log(`Gemini response length: ${text.length} characters`);
    const parsed = parseGeminiResponse(text);
    return parsed;
  } catch (error: any) {
    console.error("Gemini API error:", error);
    throw new Error(`Greška pri generiranju čitanja: ${error.message}`);
  }
}

function parseGeminiResponse(text: string): GeminiReadingResult {
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
    
    // Log raw luck_score iz Gemini response
    console.log("Raw luck_score from Gemini:", parsed.luck_score);
    console.log("Type of luck_score:", typeof parsed.luck_score);

    const finalLuckScore = typeof parsed.luck_score === "number"
      ? Math.max(0, Math.min(100, parsed.luck_score))
      : generateLuckScore();
    
    console.log("Final luck_score after processing:", finalLuckScore);

    const finalEnergyScore = typeof parsed.energy_score === "number"
      ? Math.max(0, Math.min(100, parsed.energy_score))
      : generateEnergyScore();
    
    return {
      main_text: parsed.main_text || "",
      love: parsed.love || "",
      work: parsed.work || "",
      money: parsed.money || "",
      health: parsed.health || "",
      symbols: Array.isArray(parsed.symbols) 
        ? parsed.symbols.slice(0, 3) // Max 3 simbola
        : [],
      lucky_numbers: Array.isArray(parsed.lucky_numbers)
        ? parsed.lucky_numbers
        : generateLuckyNumbers(),
      luck_score: finalLuckScore,
      mantra: parsed.mantra || generateDefaultMantra(),
      energy_score: finalEnergyScore,
    };
  } catch (error: any) {
    console.error("Failed to parse Gemini JSON response:", error);
    console.error("Response text:", jsonText.substring(0, 500));
    throw new Error(`Nije moguće parsirati JSON odgovor: ${error.message}`);
  }
}

function buildPrompt(
  zodiacSign?: string,
  gender?: string,
  focusArea?: string,
  readingMode: string = "instant"
): string {
  const contextParts: string[] = [];
  if (zodiacSign) contextParts.push(`Korisnikov znak zodijaka: ${zodiacSign}`);
  if (gender) contextParts.push(`Spol: ${gender}`);
  if (focusArea) contextParts.push(`Područje fokusa: ${focusArea}`);

  const contextStr = contextParts.length > 0
    ? `\n${contextParts.join("\n")}\n`
    : "";

  // Prilagodi prompt ovisno o modu čitanja - STVARNO RAZLIČITO PONAŠANJE
  let modeInstruction = "";
  switch (readingMode) {
    case "mystic":
      modeInstruction = `
MODE: MISTIČNI
- Koristi mističan, simboličan jezik s metaforama i alegorijama
- Fokusiraj se na skrivene značenja, duhovne aspekte i unutarnju mudrost
- Svaki segment (ljubav, posao, novac, zdravlje) treba biti 2-3 rečenice
- Koristi riječi poput "energija", "vibracija", "sudbina", "znakovi", "putovanje duše"
- Budi poetičan, ali zadrži pozitivnu poruku
- Identificiraj 4-5 simbola (ne samo 3)
- main_text treba biti 4-5 rečenica s mističnim tonom
`;
      break;
    case "deep":
      modeInstruction = `
MODE: DUBOKO ČITANJE
- Generiraj najdetaljnije i najopširnije čitanje
- Analiziraj svaki simbol, svaki oblik, svaku nijansu u šalici
- Svaki segment (ljubav, posao, novac, zdravlje) treba biti 3-4 rečenice s konkretnim savjetima
- Dodaj više konteksta, više povezanosti između simbola
- Identificiraj 5-7 simbola (maksimalno detaljno)
- main_text treba biti 6-8 rečenica s detaljnom analizom
- Uključi konkretne savjete i akcije koje korisnik može poduzeti
- Poveži simbole s različitim aspektima života
`;
      break;
    case "instant":
    default:
      modeInstruction = `
MODE: INSTANT
- Generiraj kratko, jasno i direktno čitanje
- Svaki segment (ljubav, posao, novac, zdravlje) treba biti maksimalno 1-2 rečenice
- Budi konkretan, direktan, bez nepotrebnih metafora
- Fokusiraj se na 2-3 ključne poruke
- Identificiraj 2-3 najvažnija simbola
- main_text treba biti 2-3 rečenice, direktno do točke
- Koristi jednostavan, razumljiv jezik
`;
      break;
  }

  return `
Ti si stara, mistična gatarica koja čita iz taloga kave (Tasseografija).
Pogledaj ovu sliku šalice kave. Identificiraj oblike i simbole u talogu.
${contextStr}
${modeInstruction}
Na temelju onoga što vidiš, generiraj čitanje na hrvatskom jeziku.
Budi misteriozna, malo dramatična, ali u konačnici pozitivna i ohrabrujuća.

MORAŠ vratiti rezultat u VALIDAN JSON format sa sljedećom strukturom:
{
    "main_text": "Paragraf koji opisuje opću energiju i ono što vidiš u šalici.",
    "love": "Tumačenje vezano uz ljubav i veze.",
    "work": "Tumačenje vezano uz karijeru i uspjeh.",
    "money": "Tumačenje vezano uz financije.",
    "health": "Tumačenje vezano uz vitalnost i energiju.",
    "symbols": ["Lista", "od", "3-5", "simbola", "koje", "identificiraš"],
    "luck_score": <broj od 0 do 100 koji odražava opću sreću i pozitivnu energiju u šalici>,
    "lucky_numbers": [1, 7, 12, 23, 45],
    "mantra": "Kratka, inspirativna mantra za dan (1-2 rečenice, mistična i pozitivna).",
    "energy_score": <broj od 0 do 100 koji odražava opću energiju i vitalnost dana>
}

VAŽNO: luck_score mora biti različit za svaku šalicu ovisno o onome što vidiš. 
Analiziraj oblike, simbole i opću energiju te dodijeli odgovarajući score (0-100).
Ne koristi uvijek isti broj!

Ne uključuj nikakav tekst izvan JSON objekta.
`;
}

function generateLuckyNumbers(): number[] {
  const numbers: number[] = [];
  while (numbers.length < 5) {
    const num = Math.floor(Math.random() * 49) + 1;
    if (!numbers.includes(num)) {
      numbers.push(num);
    }
  }
  return numbers.sort((a, b) => a - b);
}

function generateLuckScore(): number {
  return Math.floor(Math.random() * 40) + 50;
}

function generateEnergyScore(): number {
  return Math.floor(Math.random() * 40) + 50;
}

function generateDefaultMantra(): string {
  const mantras = [
    "Danas je dan za nove mogućnosti i pozitivne promjene.",
    "Tvoja energija privlači ono što ti treba.",
    "Vjeruj u svoju intuiciju i slijedi svoje srce.",
    "Svaki dan donosi nove prilike za rast i sreću.",
    "Tvoja unutarnja snaga vodi te prema uspjehu."
  ];
  return mantras[Math.floor(Math.random() * mantras.length)];
}