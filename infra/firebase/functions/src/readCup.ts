import * as functions from "firebase-functions";
import { CallableContext } from "firebase-functions/v1/https";
import * as admin from "firebase-admin";
import { generateReadingWithGemini } from "./gemini";
import { validateImage } from "./imageValidation";
import { ReadingResponse, ReadingErrorCode } from "./types";
import { checkRateLimit, checkQuotaLimit } from "./rateLimiting";
import * as crypto from "crypto";

export async function readCup(
  data: { imageBase64?: string; imageUrl?: string; zodiacSign?: string; gender?: string; focusArea?: string; readingMode?: string },
  context: CallableContext,
  geminiApiKey: string
): Promise<ReadingResponse & { is_cached?: boolean }> {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "Korisnik mora biti prijavljen da može čitati iz šalice."
    );
  }

  const userId = context.auth.uid;
  const { imageBase64, imageUrl, zodiacSign, gender, focusArea, readingMode } = data;

  // Rate limiting check
  const rateLimitCheck = await checkRateLimit(userId);
  if (!rateLimitCheck.allowed) {
    throw new functions.https.HttpsError(
      "resource-exhausted",
      rateLimitCheck.reason || "Prekoračen limit poziva."
    );
  }

  // Quota/billing check
  const quotaCheck = await checkQuotaLimit();
  if (!quotaCheck.allowed) {
    throw new functions.https.HttpsError(
      "resource-exhausted",
      quotaCheck.reason || "Dostignut je limit poziva."
    );
  }

  try {
    // SSRF hardening: we do not fetch arbitrary URLs from backend.
    if (imageUrl) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Slanje slike putem URL-a nije podržano. Pošaljite sliku kao imageBase64."
      );
    }

    if (!imageBase64) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Potrebna je slika (imageBase64)."
      );
    }

    let imageBuffer: Buffer;
    let imageStoragePath: string;

    // Varijable za image fingerprint (koristit će se kroz cijelu funkciju)
    let imageHash: string;
    let imageSize: number;
    let imageDimensions: string;

    {
      const base64Data = imageBase64.replace(/^data:image\/\w+;base64,/, "");
      imageBuffer = Buffer.from(base64Data, "base64");

      // Izračunaj image fingerprint (koristi se za caching, ne za logiranje)
      imageHash = crypto.createHash("sha256").update(imageBuffer).digest("hex");
      imageSize = imageBuffer.length;

      // Dobij dimensions koristeći sharp
      imageDimensions = "unknown";
      try {
        // @ts-ignore
        const sharp = require("sharp");
        const metadata = await sharp(imageBuffer).metadata();
        imageDimensions = `${metadata.width || 0}x${metadata.height || 0}`;
      } catch (e) {
        // No verbose logging here (image/content privacy)
      }

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
          error_code: "VALIDATION_FAIL",
          image_hash: imageHash,
          image_size: imageSize,
          image_dimensions: imageDimensions,
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
    }

    // 2. PROVJERI CACHE - ako postoji reading za ovaj imageHash, vrati isti rezultat
    const db = admin.firestore();
    const readingsByHashRef = db.collection("readings_by_hash");
    // Composite key da podržimo različite modove za istu sliku
    const mode = readingMode || "instant";
    const cacheKey = `${imageHash}_${mode}`;
    const cachedReadingDoc = await readingsByHashRef.doc(cacheKey).get();

    if (cachedReadingDoc.exists) {
      const cachedData = cachedReadingDoc.data();
      if (cachedData && cachedData.reading) {
        const cachedReading = cachedData.reading as ReadingResponse;
        // Osiguraj da ima sve potrebne podatke
        return {
          ...cachedReading,
          error_code: "OK",
          is_cached: true,
          image_hash: imageHash,
          image_size: imageSize,
          image_dimensions: imageDimensions,
        };
      }
    }

    let readingResult;
    let errorCode: ReadingErrorCode = "OK";

    try {
      readingResult = await generateReadingWithGemini(
        geminiApiKey,
        imageBuffer,
        zodiacSign || undefined,
        gender || undefined,
        focusArea || undefined,
        readingMode || "instant"
      );
    } catch (error: any) {
      console.error("AI_ERROR");
      errorCode = error.message?.includes("timeout") || error.message?.includes("TIMEOUT")
        ? "AI_TIMEOUT"
        : "AI_ERROR";

      // Vrati fallback response s error code-om (koristi već izračunate fingerprint podatke)

      return {
        main_text: "Talog se još skriva…",
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
        safety_level: "unknown",
        reason: "ai_error",
        error_code: errorCode,
        image_hash: imageHash,
        image_size: imageSize,
        image_dimensions: imageDimensions,
      };
    }

    const readingResponse: ReadingResponse = {
      main_text: readingResult.main_text || "",
      visible_symbols: readingResult.visible_symbols,
      interpretation: readingResult.interpretation,
      advice: readingResult.advice,
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
      error_code: "OK",
      is_cached: false,
      image_hash: imageHash,
      image_size: imageSize,
      image_dimensions: imageDimensions,
    };

    // 4. SPREMI U CACHE (readings_by_hash) - deterministički rezultat po cacheKey
    try {
      await readingsByHashRef.doc(cacheKey).set({
        reading: readingResponse,
        imageHash: imageHash,
        imageSize: imageSize,
        imageDimensions: imageDimensions,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        userId: userId, // Za analytics (opcionalno)
      }, { merge: false }); // merge: false = overwrite ako postoji (ne bi trebalo)
    } catch (cacheError: any) {
      console.error("Cache save error (non-critical)");
      // Ne bacaj grešku - čitanje je uspješno, samo cache nije
    }

    const readingData = {
      userId: userId,
      imageUrl: imageStoragePath,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      zodiacSign: zodiacSign || null,
      gender: gender || null,
      focusArea: focusArea || null,
      reading: readingResponse,
      imageHash: imageHash, // Dodaj hash za analytics
      createdAt: new Date(),
    };

    // Spremi u readings kolekciju za analytics/admin (opcionalno)
    // Klijent će spremiti u users/{userId}/readings kada korisnik klikne "Spremi čitanje"
    try {
      await db.collection("readings").add(readingData);
    } catch (firestoreError: any) {
      console.error("Firestore save error (non-critical)");
      // Ne bacaj grešku - čitanje je uspješno, samo spremanje nije
    }

    return readingResponse;
  } catch (error: any) {
    console.error("readCup UNKNOWN_ERROR");

    if (error instanceof functions.https.HttpsError) {
      throw error;
    }

    // Vrati response s error code-om umjesto da baci exception
    // Pokušaj izračunati fingerprint ako je moguće (za debugging)
    let errorImageHash: string | undefined;
    let errorImageSize: number | undefined;
    let errorImageDimensions: string | undefined;

    try {
      if (data.imageBase64) {
        const base64Data = data.imageBase64.replace(/^data:image\/\w+;base64,/, "");
        const errorBuffer = Buffer.from(base64Data, "base64");
        errorImageHash = crypto.createHash("sha256").update(errorBuffer).digest("hex");
        errorImageSize = errorBuffer.length;
        try {
          // @ts-ignore
          const sharp = require("sharp");
          const metadata = await sharp(errorBuffer).metadata();
          errorImageDimensions = `${metadata.width || 0}x${metadata.height || 0}`;
        } catch (e) {
          errorImageDimensions = "unknown";
        }
      }
    } catch (e) {
      // Ignore - nije moguće izračunati fingerprint
    }

    return {
      main_text: "Talog se još skriva…",
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
      safety_level: "unknown",
      reason: "unknown_error",
      error_code: "UNKNOWN_ERROR",
      image_hash: errorImageHash,
      image_size: errorImageSize,
      image_dimensions: errorImageDimensions,
    };
  }
}

function generateDefaultLuckScore(): number {
  // Generiraj random score između 50 i 90 ako Gemini ne vrati ništa
  return Math.floor(Math.random() * 40) + 50;
}
