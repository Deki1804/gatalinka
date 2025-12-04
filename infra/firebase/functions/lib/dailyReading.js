"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.getDailyReading = getDailyReading;
const functions = __importStar(require("firebase-functions"));
const admin = __importStar(require("firebase-admin"));
const generative_ai_1 = require("@google/generative-ai");
async function getDailyReading(data, context, geminiApiKey) {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "Korisnik mora biti prijavljen.");
    }
    const userId = context.auth.uid;
    const { zodiacSign, gender } = data;
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
        // Vrati postojeće čitanje
        const existingReading = todayReading.docs[0].data();
        console.log("Returning existing daily reading for today");
        return existingReading;
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
    const genAI = new generative_ai_1.GoogleGenerativeAI(cleanedApiKey);
    const model = genAI.getGenerativeModel({
        model: "gemini-2.0-flash",
    });
    const prompt = buildDailyReadingPrompt(todayStr, zodiacSign, gender);
    console.log("Generating daily reading for zodiac:", zodiacSign);
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
    return parsed;
}
function buildDailyReadingPrompt(date, zodiacSign, gender) {
    const contextParts = [];
    if (zodiacSign)
        contextParts.push(`Korisnikov znak zodijaka: ${zodiacSign}`);
    if (gender)
        contextParts.push(`Spol: ${gender}`);
    const contextStr = contextParts.length > 0
        ? `\n${contextParts.join("\n")}\n`
        : "";
    return `
Ti si stara, mistična gatarica koja čita sudbinu iz energije dana.
Danas je ${date}. Generiraj dnevno čitanje na hrvatskom jeziku temeljeno na horoskopskim energijama i općoj energiji dana.
${contextStr}
Budi misteriozna, pozitivna i ohrabrujuća. Fokusiraj se na današnji dan i što on donosi.

MORAŠ vratiti rezultat u VALIDAN JSON format sa sljedećom strukturom:
{
    "main_text": "Paragraf koji opisuje opću energiju današnjeg dana i što donosi.",
    "love": "Tumačenje vezano uz ljubav i veze za danas.",
    "work": "Tumačenje vezano uz karijeru i uspjeh za danas.",
    "money": "Tumačenje vezano uz financije za danas.",
    "health": "Tumačenje vezano uz vitalnost i energiju za danas.",
    "symbols": ["Lista", "od", "3-5", "simbola", "koji", "karakteriziraju", "danas"],
    "luck_score": <broj od 0 do 100 koji odražava opću sreću dana>,
    "lucky_numbers": [1, 7, 12, 23, 45],
    "mantra": "Kratka, inspirativna mantra za danas (1-2 rečenice, mistična i pozitivna).",
    "energy_score": <broj od 0 do 100 koji odražava opću energiju dana>
}

VAŽNO: luck_score i energy_score moraju biti različiti svaki dan ovisno o horoskopskim energijama.
Ne koristi uvijek isti broj!

Ne uključuj nikakav tekst izvan JSON objekta.
`;
}
function parseDailyReadingResponse(text, date) {
    let cleaned = text;
    if (cleaned.includes("```json")) {
        cleaned = cleaned.replace(/```json/g, "").replace(/```/g, "").trim();
    }
    else if (cleaned.includes("```")) {
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
        const finalLuckScore = typeof parsed.luck_score === "number"
            ? Math.max(0, Math.min(100, parsed.luck_score))
            : Math.floor(Math.random() * 40) + 50;
        const finalEnergyScore = typeof parsed.energy_score === "number"
            ? Math.max(0, Math.min(100, parsed.energy_score))
            : Math.floor(Math.random() * 40) + 50;
        return {
            main_text: parsed.main_text || "Danas donosi nove mogućnosti i pozitivne promjene.",
            love: parsed.love || "Energija ljubavi je jaka danas.",
            work: parsed.work || "Poslovni uspjeh čeka te.",
            money: parsed.money || "Financijska energija je pozitivna.",
            health: parsed.health || "Tvoja vitalnost je visoka.",
            symbols: Array.isArray(parsed.symbols) ? parsed.symbols : ["Zvijezda", "Srce", "Put"],
            lucky_numbers: Array.isArray(parsed.lucky_numbers)
                ? parsed.lucky_numbers
                : generateLuckyNumbers(),
            luck_score: finalLuckScore,
            mantra: parsed.mantra || "Danas je dan za nove mogućnosti i pozitivne promjene.",
            energy_score: finalEnergyScore,
            date: date,
        };
    }
    catch (error) {
        console.error("Failed to parse Gemini JSON response:", error);
        console.error("Response text:", jsonText.substring(0, 500));
        throw new Error(`Nije moguće parsirati JSON odgovor: ${error.message}`);
    }
}
function generateLuckyNumbers() {
    const numbers = [];
    while (numbers.length < 5) {
        const num = Math.floor(Math.random() * 49) + 1;
        if (!numbers.includes(num)) {
            numbers.push(num);
        }
    }
    return numbers.sort((a, b) => a - b);
}
//# sourceMappingURL=dailyReading.js.map