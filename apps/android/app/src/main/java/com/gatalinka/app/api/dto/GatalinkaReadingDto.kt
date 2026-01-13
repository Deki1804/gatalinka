package com.gatalinka.app.api.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO za simbol s značenjem (bapsko gatanje).
 */
data class VisibleSymbolDto(
    val symbol: String,
    val meaning: String
)

/**
 * DTO za AI čitanje iz kave (bapski stil).
 */
data class GatalinkaReadingDto(
    @SerializedName("main_text")
    val mainText: String,
    
    @SerializedName("visible_symbols")
    val visibleSymbols: List<VisibleSymbolDto>? = null, // Lista simbola s značenjem
    
    val interpretation: String? = null, // Kako se to tumači (bapski stil)
    
    val advice: String? = null, // Kratki savjet (1-2 rečenice)
    
    val love: String,
    
    val work: String,
    
    val money: String,
    
    val health: String,
    
    val symbols: List<String>, // Lista imena simbola (za kompatibilnost)
    
    @SerializedName("lucky_numbers")
    val luckyNumbers: List<Int>,
    
    @SerializedName("luck_score")
    val luckScore: Int, // 0–100
    
    val mantra: String, // Dnevna mantra/poruka
    
    @SerializedName("energy_score")
    val energyScore: Int, // 0–100, opća energija dana
    
    @SerializedName("is_valid_cup")
    val isValidCup: Boolean,
    
    @SerializedName("safety_level")
    val safetyLevel: String, // "ok" | "nsfw" | "unknown"
    
    val reason: String,
    
    @SerializedName("horoscope_match")
    val horoscopeMatch: String? = null, // Kratka horoskopska potvrda (1-2 rečenice, opcionalno)
    
    @SerializedName("error_code")
    val errorCode: String? = null, // Error code za debugging: "OK", "VALIDATION_FAIL", "UPLOAD_FAIL", "AI_TIMEOUT", "AI_ERROR", "PARSE_FAIL", "UNKNOWN_ERROR"
    
    @SerializedName("image_hash")
    val imageHash: String? = null, // SHA-256 hash slike za provjeru da su različite
    
    @SerializedName("image_size")
    val imageSize: Int? = null, // Veličina slike u bytes
    
    @SerializedName("image_dimensions")
    val imageDimensions: String? = null // "width x height"
)


