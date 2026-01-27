export interface VisibleSymbol {
  symbol: string;
  meaning: string;
}

export type ReadingErrorCode =
  | "OK"
  | "VALIDATION_FAIL"
  | "UPLOAD_FAIL"
  | "AI_TIMEOUT"
  | "AI_ERROR"
  | "PARSE_FAIL"
  | "UNKNOWN_ERROR";

export interface ReadingResponse {
  main_text: string;
  visible_symbols?: VisibleSymbol[]; // Lista simbola s značenjem
  interpretation?: string; // Kako se to tumači (bapski stil)
  advice?: string; // Kratki savjet (1-2 rečenice)
  love: string;
  work: string;
  money: string;
  health: string;
  symbols: string[]; // Lista imena simbola (za kompatibilnost)
  lucky_numbers: number[];
  luck_score: number;
  mantra: string; // Dnevna mantra/poruka
  energy_score: number; // 0-100, opća energija dana
  is_valid_cup: boolean;
  safety_level: "ok" | "nsfw" | "unknown";
  reason: string;
  error_code?: ReadingErrorCode; // Error code za debugging
  image_hash?: string; // SHA-256 hash slike za provjeru da su različite
  image_size?: number; // Veličina slike u bytes
  image_dimensions?: string; // "width x height"
  is_cached?: boolean; // Je li rezultat iz cache-a
}

export interface FirestoreReading {
  userId: string;
  imageUrl: string;
  timestamp: FirebaseFirestore.Timestamp;
  zodiacSign: string | null;
  gender: string | null;
  focusArea: string | null;
  reading: ReadingResponse;
  createdAt: Date;
}