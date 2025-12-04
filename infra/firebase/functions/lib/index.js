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
exports.getDailyReadingCallable = exports.readCupCallable = void 0;
const admin = __importStar(require("firebase-admin"));
const https_1 = require("firebase-functions/v2/https");
const params_1 = require("firebase-functions/params");
const readCup_1 = require("./readCup");
const dailyReading_1 = require("./dailyReading");
admin.initializeApp();
// Define secret for Gemini API key using v2 parameter injection
const geminiApiKey = (0, params_1.defineSecret)("GEMINI_API_KEY");
// Migrated to v2 onCall format with secret injection
exports.readCupCallable = (0, https_1.onCall)({
    secrets: [geminiApiKey],
    timeoutSeconds: 60,
    memory: "1GiB",
    minInstances: 0,
    maxInstances: 10,
    region: "us-central1",
}, async (request) => {
    console.log("=== readCupCallable INVOKED (v2) ===");
    console.log("request.auth exists:", !!request.auth);
    console.log("request.auth?.uid:", request.auth?.uid);
    console.log("data keys:", Object.keys(request.data || {}));
    // Check authentication
    if (!request.auth) {
        throw new https_1.HttpsError("unauthenticated", "Korisnik mora biti prijavljen da može čitati iz šalice.");
    }
    // Convert v2 request to v1-style context for backward compatibility
    const context = {
        auth: request.auth,
        rawRequest: request.rawRequest,
    };
    try {
        // Get API key from secret - use .value() method
        const apiKeyValue = geminiApiKey.value();
        return await (0, readCup_1.readCup)(request.data, context, apiKeyValue);
    }
    catch (error) {
        console.error("Error in readCup:", error);
        // If it's already an HttpsError, rethrow it
        if (error.code && error.message) {
            throw error;
        }
        // Otherwise wrap it
        throw new https_1.HttpsError("internal", `Greška pri čitanju iz šalice: ${error.message || "Nepoznata greška"}`);
    }
});
exports.getDailyReadingCallable = (0, https_1.onCall)({
    secrets: [geminiApiKey],
    timeoutSeconds: 60,
    memory: "1GiB",
    minInstances: 0,
    maxInstances: 10,
    region: "us-central1",
}, async (request) => {
    console.log("=== getDailyReadingCallable INVOKED (v2) ===");
    console.log("request.auth exists:", !!request.auth);
    console.log("request.auth?.uid:", request.auth?.uid);
    // Check authentication
    if (!request.auth) {
        throw new https_1.HttpsError("unauthenticated", "Korisnik mora biti prijavljen.");
    }
    // Convert v2 request to v1-style context for backward compatibility
    const context = {
        auth: request.auth,
        rawRequest: request.rawRequest,
    };
    try {
        return await (0, dailyReading_1.getDailyReading)(request.data, context, geminiApiKey.value());
    }
    catch (error) {
        console.error("Error in getDailyReading:", error);
        // If it's already an HttpsError, rethrow it
        if (error.code && error.message) {
            throw error;
        }
        // Otherwise wrap it
        throw new https_1.HttpsError("internal", `Greška pri dohvaćanju dnevnog čitanja: ${error.message || "Nepoznata greška"}`);
    }
});
//# sourceMappingURL=index.js.map