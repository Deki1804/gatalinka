import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import { readCup } from "./readCup";
import { getDailyReading } from "./dailyReading";

admin.initializeApp();

// Define secret for Gemini API key using v2 parameter injection
const geminiApiKey = defineSecret("GEMINI_API_KEY");

// Migrated to v2 onCall format with secret injection
export const readCupCallable = onCall(
  {
    secrets: [geminiApiKey],
    timeoutSeconds: 60,
    memory: "1GiB",
    minInstances: 0,
    maxInstances: 10,
    region: "us-central1",
  },
  async (request) => {
    console.log("=== readCupCallable INVOKED (v2) ===");
    console.log("request.auth exists:", !!request.auth);
    console.log("request.auth?.uid:", request.auth?.uid);
    console.log("data keys:", Object.keys(request.data || {}));
    
    // Check authentication
    if (!request.auth) {
      throw new HttpsError(
        "unauthenticated",
        "Korisnik mora biti prijavljen da može čitati iz šalice."
      );
    }
    
    // Convert v2 request to v1-style context for backward compatibility
    const context = {
      auth: request.auth,
      rawRequest: request.rawRequest,
    };
    
    try {
      // Get API key from secret - use .value() method
      const apiKeyValue = geminiApiKey.value();
      
      return await readCup(request.data, context, apiKeyValue);
    } catch (error: any) {
      console.error("Error in readCup:", error);
      // If it's already an HttpsError, rethrow it
      if (error.code && error.message) {
        throw error;
      }
      // Otherwise wrap it
      throw new HttpsError(
        "internal",
        `Greška pri čitanju iz šalice: ${error.message || "Nepoznata greška"}`
      );
    }
  }
);

export const getDailyReadingCallable = onCall(
  {
    secrets: [geminiApiKey],
    timeoutSeconds: 60,
    memory: "1GiB",
    minInstances: 0,
    maxInstances: 10,
    region: "us-central1",
  },
  async (request) => {
    console.log("=== getDailyReadingCallable INVOKED (v2) ===");
    console.log("request.auth exists:", !!request.auth);
    console.log("request.auth?.uid:", request.auth?.uid);
    
    // Check authentication
    if (!request.auth) {
      throw new HttpsError(
        "unauthenticated",
        "Korisnik mora biti prijavljen."
      );
    }
    
    // Convert v2 request to v1-style context for backward compatibility
    const context = {
      auth: request.auth,
      rawRequest: request.rawRequest,
    };
    
    try {
      return await getDailyReading(request.data, context, geminiApiKey.value());
    } catch (error: any) {
      console.error("Error in getDailyReading:", error);
      // If it's already an HttpsError, rethrow it
      if (error.code && error.message) {
        throw error;
      }
      // Otherwise wrap it
      throw new HttpsError(
        "internal",
        `Greška pri dohvaćanju dnevnog čitanja: ${error.message || "Nepoznata greška"}`
      );
    }
  }
);