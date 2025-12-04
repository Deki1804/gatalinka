export interface ReadingResponse {
  main_text: string;
  love: string;
  work: string;
  money: string;
  health: string;
  symbols: string[];
  lucky_numbers: number[];
  luck_score: number;
  mantra: string; // Dnevna mantra/poruka
  energy_score: number; // 0-100, opća energija dana
  is_valid_cup: boolean;
  safety_level: "ok" | "nsfw" | "unknown";
  reason: string;
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