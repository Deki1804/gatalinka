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
exports.readCup = readCup;
var functions = require("firebase-functions");
var admin = require("firebase-admin");
var gemini_1 = require("./gemini");
var imageValidation_1 = require("./imageValidation");
var db = admin.firestore();
var storage = admin.storage();
function readCup(data, context) {
    return __awaiter(this, void 0, void 0, function () {
        var userId, imageBase64, imageUrl, zodiacSign, gender, focusArea, imageBuffer, imageStoragePath, base64Data, validation, timestamp, bucket, file, response, _a, _b, readingResult, readingResponse, readingData, error_1;
        return __generator(this, function (_c) {
            switch (_c.label) {
                case 0:
                    if (!context.auth) {
                        throw new functions.https.HttpsError("unauthenticated", "Korisnik mora biti prijavljen da može čitati iz šalice.");
                    }
                    userId = context.auth.uid;
                    imageBase64 = data.imageBase64, imageUrl = data.imageUrl, zodiacSign = data.zodiacSign, gender = data.gender, focusArea = data.focusArea;
                    _c.label = 1;
                case 1:
                    _c.trys.push([1, 12, , 13]);
                    if (!imageBase64 && !imageUrl) {
                        throw new functions.https.HttpsError("invalid-argument", "Potrebna je slika (imageBase64 ili imageUrl).");
                    }
                    imageBuffer = void 0;
                    imageStoragePath = void 0;
                    if (!imageBase64) return [3 /*break*/, 5];
                    base64Data = imageBase64.replace(/^data:image\/\w+;base64,/, "");
                    imageBuffer = Buffer.from(base64Data, "base64");
                    return [4 /*yield*/, (0, imageValidation_1.validateImage)(imageBuffer)];
                case 2:
                    validation = _c.sent();
                    if (!validation.isValid) {
                        return [2 /*return*/, {
                                main_text: "",
                                love: "",
                                work: "",
                                money: "",
                                health: "",
                                symbols: [],
                                lucky_numbers: [],
                                luck_score: 0,
                                is_valid_cup: false,
                                safety_level: validation.safetyLevel,
                                reason: validation.reason,
                            }];
                    }
                    timestamp = Date.now();
                    imageStoragePath = "readings/".concat(userId, "/").concat(timestamp, ".jpg");
                    bucket = storage.bucket();
                    file = bucket.file(imageStoragePath);
                    return [4 /*yield*/, file.save(imageBuffer, {
                            metadata: {
                                contentType: "image/jpeg",
                                metadata: {
                                    userId: userId,
                                    timestamp: timestamp.toString(),
                                },
                            },
                        })];
                case 3:
                    _c.sent();
                    return [4 /*yield*/, file.makePublic()];
                case 4:
                    _c.sent();
                    return [3 /*break*/, 9];
                case 5:
                    if (!imageUrl) return [3 /*break*/, 8];
                    return [4 /*yield*/, fetch(imageUrl)];
                case 6:
                    response = _c.sent();
                    if (!response.ok) {
                        throw new functions.https.HttpsError("invalid-argument", "Nije moguće dohvatiti sliku s navedenog URL-a.");
                    }
                    _b = (_a = Buffer).from;
                    return [4 /*yield*/, response.arrayBuffer()];
                case 7:
                    imageBuffer = _b.apply(_a, [_c.sent()]);
                    imageStoragePath = imageUrl;
                    return [3 /*break*/, 9];
                case 8: throw new functions.https.HttpsError("invalid-argument", "Potrebna je slika.");
                case 9: return [4 /*yield*/, (0, gemini_1.generateReadingWithGemini)(imageBuffer, zodiacSign || undefined, gender || undefined, focusArea || undefined)];
                case 10:
                    readingResult = _c.sent();
                    readingResponse = {
                        main_text: readingResult.main_text || "",
                        love: readingResult.love || "",
                        work: readingResult.work || "",
                        money: readingResult.money || "",
                        health: readingResult.health || "",
                        symbols: readingResult.symbols || [],
                        lucky_numbers: readingResult.lucky_numbers || [],
                        luck_score: readingResult.luck_score || 75,
                        is_valid_cup: true,
                        safety_level: "ok",
                        reason: "ok",
                    };
                    readingData = {
                        userId: userId,
                        imageUrl: imageStoragePath,
                        timestamp: admin.firestore.FieldValue.serverTimestamp(),
                        zodiacSign: zodiacSign || null,
                        gender: gender || null,
                        focusArea: focusArea || null,
                        reading: readingResponse,
                        createdAt: new Date(),
                    };
                    return [4 /*yield*/, db.collection("readings").add(readingData)];
                case 11:
                    _c.sent();
                    return [2 /*return*/, readingResponse];
                case 12:
                    error_1 = _c.sent();
                    console.error("Error in readCup function:", error_1);
                    if (error_1 instanceof functions.https.HttpsError) {
                        throw error_1;
                    }
                    throw new functions.https.HttpsError("internal", "Gre\u0161ka pri \u010Ditanju iz \u0161alice: ".concat(error_1.message || "Nepoznata greška"));
                case 13: return [2 /*return*/];
            }
        });
    });
}
