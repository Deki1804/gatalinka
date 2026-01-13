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
    
    // Varijable za image fingerprint (koristit će se kroz cijelu funkciju)
    let imageHash: string;
    let imageSize: number;
    let imageDimensions: string;

    if (imageBase64) {
      const base64Data = imageBase64.replace(/^data:image\/\w+;base64,/, "");
      imageBuffer = Buffer.from(base64Data, "base64");

      // Izračunaj image fingerprint
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
        console.warn("Could not get image dimensions:", e);
      }
      
      console.log("=== IMAGE FINGERPRINT ===");
      console.log(`Image hash (SHA-256): ${imageHash}`);
      console.log(`Image size: ${imageSize} bytes`);
      console.log(`Image dimensions: ${imageDimensions}`);
      console.log(`Image URI/source: base64 (${imageBase64.substring(0, 50)}...)`);
      console.log("=========================");

      const validation = await validateImage(imageBuffer);
      if (!validation.isValid) {
        console.log(`❌ VALIDATION_FAIL: reason=${validation.reason}`);
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
    } else if (imageUrl) {
      const response = await fetch(imageUrl);
      if (!response.ok) {
        console.log(`❌ UPLOAD_FAIL: Could not fetch image from URL`);
        throw new functions.https.HttpsError(
          "invalid-argument",
          "Nije moguće dohvatiti sliku s navedenog URL-a."
        );
      }
      imageBuffer = Buffer.from(await response.arrayBuffer());
      imageStoragePath = imageUrl;
      
      // Izračunaj image fingerprint
      imageHash = crypto.createHash("sha256").update(imageBuffer).digest("hex");
      imageSize = imageBuffer.length;
      imageDimensions = "unknown";
      try {
        // @ts-ignore
        const sharp = require("sharp");
        const metadata = await sharp(imageBuffer).metadata();
        imageDimensions = `${metadata.width || 0}x${metadata.height || 0}`;
      } catch (e) {
        console.warn("Could not get image dimensions:", e);
      }
      
      console.log("=== IMAGE FINGERPRINT ===");
      console.log(`Image hash (SHA-256): ${imageHash}`);
      console.log(`Image size: ${imageSize} bytes`);
      console.log(`Image dimensions: ${imageDimensions}`);
      console.log(`Image URI/source: ${imageUrl}`);
      console.log("=========================");
    } else {
      console.log(`❌ UPLOAD_FAIL: No image provided`);
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Potrebna je slika."
      );
    }

    // 2. PROVJERI CACHE - ako postoji reading za ovaj imageHash, vrati isti rezultat
    const db = admin.firestore();
    const readingsByHashRef = db.collection("readings_by_hash");
    const cachedReadingDoc = await readingsByHashRef.doc(imageHash).get();
    
    if (cachedReadingDoc.exists) {
      const cachedData = cachedReadingDoc.data();
      if (cachedData && cachedData.reading) {
        console.log("✅ CACHE HIT: Returning cached reading for imageHash:", imageHash);
        const cachedReading = cachedData.reading as ReadingResponse;
        // Osiguraj da ima sve potrebne podatke
        return {
          ...cachedReading,
          error_code: "OK",
          image_hash: imageHash,
          image_size: imageSize,
          image_dimensions: imageDimensions,
        };
      }
    }
    
    console.log("🔄 CACHE MISS: Generating new reading for imageHash:", imageHash);
    
    // 3. PROVJERI DA LI VALIDATOR BLOKIRA PRIJE AI-JA
    console.log("✅ Image validation passed, proceeding to AI analysis...");
    
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
      console.log("✅ AI analysis completed successfully");
    } catch (error: any) {
      console.error("❌ AI_ERROR:", error);
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
      image_hash: imageHash,
      image_size: imageSize,
      image_dimensions: imageDimensions,
    };
    
    console.log("Final luck_score in response:", readingResponse.luck_score);

    // 4. SPREMI U CACHE (readings_by_hash) - deterministički rezultat po imageHash
    try {
      await readingsByHashRef.doc(imageHash).set({
        reading: readingResponse,
        imageHash: imageHash,
        imageSize: imageSize,
        imageDimensions: imageDimensions,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        userId: userId, // Za analytics (opcionalno)
      }, { merge: false }); // merge: false = overwrite ako postoji (ne bi trebalo)
      console.log("✅ Reading cached successfully for imageHash:", imageHash);
    } catch (cacheError: any) {
      console.error("⚠️ Cache save error (non-critical):", cacheError);
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
      console.log("Reading saved to Firestore 'readings' collection successfully");
    } catch (firestoreError: any) {
      console.error("Firestore save error (non-critical):", firestoreError);
      // Ne bacaj grešku - čitanje je uspješno, samo spremanje nije
    }

    console.log("Returning reading response:", JSON.stringify(readingResponse).substring(0, 200));
    return readingResponse;
  } catch (error: any) {
    console.error("❌ UNKNOWN_ERROR in readCup function:", error);

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
