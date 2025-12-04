import * as admin from "firebase-admin";
import { onCall } from "firebase-functions/v2/https";
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
    
    // Convert v2 request to v1-style context for backward compatibility
    const context = {
      auth: request.auth,
      rawRequest: request.rawRequest,
    };
    
    return readCup(request.data, context, geminiApiKey.value());
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
    
    // Convert v2 request to v1-style context for backward compatibility
    const context = {
      auth: request.auth,
      rawRequest: request.rawRequest,
    };
    
    return getDailyReading(request.data, context, geminiApiKey.value());
  }
);