"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g = Object.create((typeof Iterator === "function" ? Iterator : Object).prototype);
    return g.next = verb(0), g["throw"] = verb(1), g["return"] = verb(2), typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (g && (g = 0, op[0] && (_ = 0)), _) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.generateReadingWithGemini = generateReadingWithGemini;
var generative_ai_1 = require("@google/generative-ai");
var functions = require("firebase-functions");
function getGeminiApiKey() {
    var _a;
    try {
        var config = functions.config();
        if ((_a = config === null || config === void 0 ? void 0 : config.gemini) === null || _a === void 0 ? void 0 : _a.api_key) {
            return config.gemini.api_key;
        }
    }
    catch (e) {
    }
    return process.env.GEMINI_API_KEY || "";
}
function generateReadingWithGemini(imageBuffer, zodiacSign, gender, focusArea) {
    return __awaiter(this, void 0, void 0, function () {
        var apiKey, genAI, model, prompt_1, imageBase64, imageData, result, response, text, parsed, error_1;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    apiKey = getGeminiApiKey();
                    if (!apiKey) {
                        throw new Error("Gemini API nije konfiguriran. Postavite functions config ili GEMINI_API_KEY environment varijablu.");
                    }
                    genAI = new generative_ai_1.GoogleGenerativeAI(apiKey);
                    _a.label = 1;
                case 1:
                    _a.trys.push([1, 4, , 5]);
                    model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });
                    prompt_1 = buildPrompt(zodiacSign, gender, focusArea);
                    imageBase64 = imageBuffer.toString("base64");
                    imageData = {
                        inlineData: {
                            data: imageBase64,
                            mimeType: "image/jpeg",
                        },
                    };
                    console.log("Calling Gemini API for coffee cup reading...");
                    return [4 /*yield*/, model.generateContent([prompt_1, imageData])];
                case 2:
                    result = _a.sent();
                    return [4 /*yield*/, result.response];
                case 3:
                    response = _a.sent();
                    text = response.text();
                    console.log("Gemini response length: ".concat(text.length, " characters"));
                    parsed = parseGeminiResponse(text);
                    return [2 /*return*/, parsed];
                case 4:
                    error_1 = _a.sent();
                    console.error("Gemini API error:", error_1);
                    throw new Error("Gre\u0161ka pri generiranju \u010Ditanja: ".concat(error_1.message));
                case 5: return [2 /*return*/];
            }
        });
    });
}
function parseGeminiResponse(text) {
    var cleaned = text;
    if (cleaned.includes("```json")) {
        cleaned = cleaned.replace(/```json/g, "").replace(/```/g, "").trim();
    }
    else if (cleaned.includes("```")) {
        cleaned = cleaned.replace(/```/g, "").trim();
    }
    var jsonStart = cleaned.indexOf("{");
    var jsonEnd = cleaned.lastIndexOf("}");
    if (jsonStart === -1 || jsonEnd === -1 || jsonEnd <= jsonStart) {
        throw new Error("Gemini API nije vratio validan JSON odgovor.");
    }
    var jsonText = cleaned.substring(jsonStart, jsonEnd + 1);
    try {
        var parsed = JSON.parse(jsonText);
        return {
            main_text: parsed.main_text || "",
            love: parsed.love || "",
            work: parsed.work || "",
            money: parsed.money || "",
            health: parsed.health || "",
            symbols: Array.isArray(parsed.symbols) ? parsed.symbols : [],
            lucky_numbers: Array.isArray(parsed.lucky_numbers)
                ? parsed.lucky_numbers
                : generateLuckyNumbers(),
            luck_score: typeof parsed.luck_score === "number"
                ? Math.max(0, Math.min(100, parsed.luck_score))
                : generateLuckScore(),
        };
    }
    catch (error) {
        console.error("Failed to parse Gemini JSON response:", error);
        console.error("Response text:", jsonText.substring(0, 500));
        throw new Error("Nije mogu\u0107e parsirati JSON odgovor: ".concat(error.message));
    }
}
function buildPrompt(zodiacSign, gender, focusArea) {
    var contextParts = [];
    if (zodiacSign)
        contextParts.push("Korisnikov znak zodijaka: ".concat(zodiacSign));
    if (gender)
        contextParts.push("Spol: ".concat(gender));
    if (focusArea)
        contextParts.push("Podru\u010Dje fokusa: ".concat(focusArea));
    var contextStr = contextParts.length > 0
        ? "\n".concat(contextParts.join("\n"), "\n")
        : "";
    return "\nTi si stara, misti\u010Dna gatarica koja \u010Dita iz taloga kave (Tasseografija).\nPogledaj ovu sliku \u0161alice kave. Identificiraj oblike i simbole u talogu.\n".concat(contextStr, "\nNa temelju onoga \u0161to vidi\u0161, generiraj \u010Ditanje na hrvatskom jeziku.\nBudi misteriozna, malo dramati\u010Dna, ali u kona\u010Dnici pozitivna i ohrabruju\u0107a.\n\nMORA\u0160 vratiti rezultat u VALIDAN JSON format sa sljede\u0107om strukturom:\n{\n    \"main_text\": \"Paragraf koji opisuje op\u0107u energiju i ono \u0161to vidi\u0161 u \u0161alici.\",\n    \"love\": \"Tuma\u010Denje vezano uz ljubav i veze.\",\n    \"work\": \"Tuma\u010Denje vezano uz karijeru i uspjeh.\",\n    \"money\": \"Tuma\u010Denje vezano uz financije.\",\n    \"health\": \"Tuma\u010Denje vezano uz vitalnost i energiju.\",\n    \"symbols\": [\"Lista\", \"od\", \"3-5\", \"simbola\", \"koje\", \"identificira\u0161\"],\n    \"luck_score\": 75,\n    \"lucky_numbers\": [1, 7, 12, 23, 45]\n}\n\nNe uklju\u010Duj nikakav tekst izvan JSON objekta.\n");
}
function generateLuckyNumbers() {
    var numbers = [];
    while (numbers.length < 5) {
        var num = Math.floor(Math.random() * 49) + 1;
        if (!numbers.includes(num)) {
            numbers.push(num);
        }
    }
    return numbers.sort(function (a, b) { return a - b; });
}
function generateLuckScore() {
    return Math.floor(Math.random() * 40) + 50;
}
