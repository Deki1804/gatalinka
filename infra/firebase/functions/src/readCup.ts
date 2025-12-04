import * as functions from "firebase-functions";
import { CallableContext } from "firebase-functions/v1/https";
import * as admin from "firebase-admin";
import { generateReadingWithGemini } from "./gemini";
import { validateImage } from "./imageValidation";
import { ReadingResponse } from "./types";

export async function readCup(
  data: { imageBase64?: string; imageUrl?: string; zodiacSign?: string; gender?: string; focusArea?: string; readingMode?: string },
  context: CallableContext,
  geminiApiKey: string
): Promise<ReadingResponse> {
  // Debug logging - detaljna provjera auth konteksta
  console.log("=== readCup AUTH DEBUG ===");
  console.log("context.auth:", context.auth ? "EXISTS" : "NULL");
  if (context.auth) {
    console.log("context.auth.uid:", context.auth.uid);
    console.log("context.auth.token:", context.auth.token ? "EXISTS" : "NULL");
  } else {
    console.error("UNAUTHENTICATED: context.auth is null");
    console.error("Available context keys:", Object.keys(context));
  }
  
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "Korisnik mora biti prijavljen da može čitati iz šalice."
    );
  }

  const userId = context.auth.uid;
  console.log("User authenticated, UID:", userId);
  const { imageBase64, imageUrl, zodiacSign, gender, focusArea, readingMode } = data;
  
  // Diff log za debugging - prije i poslije
  console.log("=== readCup DIFF LOG START ===");
  console.log("Input data:", {
    zodiacSign,
    gender,
    focusArea,
    readingMode: readingMode || "instant",
    hasImageBase64: !!imageBase64,
    hasImageUrl: !!imageUrl
  });

  try {
    if (!imageBase64 && !imageUrl) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Potrebna je slika (imageBase64 ili imageUrl)."
      );
    }

    let imageBuffer: Buffer;
    let imageStoragePath: string;

    if (imageBase64) {
      const base64Data = imageBase64.replace(/^data:image\/\w+;base64,/, "");
      imageBuffer = Buffer.from(base64Data, "base64");

      const validation = await validateImage(imageBuffer);
      if (!validation.isValid) {
        return {
          main_text: "",
          love: "",
          work: "",
          money: "",
          health: "",
          symbols: [],
          lucky_numbers: [],
          luck_score: 0,
          mantra: "",
          energy_score: 0,
          is_valid_cup: false,
          safety_level: validation.safetyLevel,
          reason: validation.reason,
        };
      }

      const timestamp = Date.now();
      imageStoragePath = `readings/${userId}/${timestamp}.jpg`;
      
      // Privremeno preskoči Storage - koristimo base64 direktno za analizu
      // Storage ćemo dodati kasnije kada se bucket kreira u Firebase konzoli
      // TODO: Kada se Storage bucket kreira, vrati ovaj kod:
      /*
      const storage = admin.storage();
      const bucket = storage.bucket();
      const file = bucket.file(imageStoragePath);
      await file.save(imageBuffer, {
        metadata: {
          contentType: "image/jpeg",
          metadata: {
            userId: userId,
            timestamp: timestamp.toString(),
          },
        },
      });
      await file.makePublic();
      */
      
      // Za sada koristimo placeholder URL
      imageStoragePath = `base64://${timestamp}.jpg`;
    } else if (imageUrl) {
      const response = await fetch(imageUrl);
      if (!response.ok) {
        throw new functions.https.HttpsError(
          "invalid-argument",
          "Nije moguće dohvatiti sliku s navedenog URL-a."
        );
      }
      imageBuffer = Buffer.from(await response.arrayBuffer());
      imageStoragePath = imageUrl;
    } else {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Potrebna je slika."
      );
    }

    const readingResult = await generateReadingWithGemini(
      geminiApiKey,
      imageBuffer,
      zodiacSign || undefined,
      gender || undefined,
      focusArea || undefined,
      readingMode || "instant"
    );
    
    // Diff log - rezultat iz Gemini
    console.log("=== readCup DIFF LOG - Gemini Result ===");
    console.log("Reading result:", {
      luckScore: readingResult.luck_score,
      energyScore: readingResult.energy_score,
      symbolsCount: readingResult.symbols?.length || 0,
      symbols: readingResult.symbols,
      hasMantra: !!readingResult.mantra,
      readingMode: readingMode || "instant"
    });

    // Log luck_score da vidimo što Gemini vraća
    console.log("Gemini returned luck_score:", readingResult.luck_score);
    console.log("Type of luck_score:", typeof readingResult.luck_score);

    const readingResponse: ReadingResponse = {
      main_text: readingResult.main_text || "",
      love: readingResult.love || "",
      work: readingResult.work || "",
      money: readingResult.money || "",
      health: readingResult.health || "",
      symbols: readingResult.symbols || [],
      lucky_numbers: readingResult.lucky_numbers || [],
      luck_score: readingResult.luck_score != null && readingResult.luck_score > 0 
        ? readingResult.luck_score 
        : generateDefaultLuckScore(),
      mantra: readingResult.mantra || "Danas je dan za nove mogućnosti.",
      energy_score: readingResult.energy_score != null && readingResult.energy_score >= 0
        ? Math.max(0, Math.min(100, readingResult.energy_score))
        : Math.floor(Math.random() * 40) + 50,
      is_valid_cup: true,
      safety_level: "ok",
      reason: "ok",
    };
    
    console.log("Final luck_score in response:", readingResponse.luck_score);

    const readingData = {
      userId: userId,
      imageUrl: imageStoragePath,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      zodiacSign: zodiacSign || null,
      gender: gender || null,
      focusArea: focusArea || null,
      reading: readingResponse,
      createdAt: new Date(),
    };

    // Spremi u readings kolekciju za analytics/admin (opcionalno)
    // Klijent će spremiti u users/{userId}/readings kada korisnik klikne "Spremi čitanje"
    try {
      const db = admin.firestore();
      await db.collection("readings").add(readingData);
      console.log("Reading saved to Firestore 'readings' collection successfully");
    } catch (firestoreError: any) {
      console.error("Firestore save error (non-critical):", firestoreError);
      // Ne bacaj grešku - čitanje je uspješno, samo spremanje nije
    }

    console.log("Returning reading response:", JSON.stringify(readingResponse).substring(0, 200));
    return readingResponse;
  } catch (error: any) {
    console.error("Error in readCup function:", error);

    if (error instanceof functions.https.HttpsError) {
      throw error;
    }

    throw new functions.https.HttpsError(
      "internal",
      `Greška pri čitanju iz šalice: ${error.message || "Nepoznata greška"}`
    );
  }
}

function generateDefaultLuckScore(): number {
  // Generiraj random score između 50 i 90 ako Gemini ne vrati ništa
  return Math.floor(Math.random() * 40) + 50;
}
