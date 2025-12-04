package com.gatalinka.app.api.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO za AI čitanje iz kave.
 * 
 * TODO: Ako backend vraća drugačije polja, mapiraj ih ovdje.
 */
data class GatalinkaReadingDto(
    @SerializedName("main_text")
    val mainText: String,
    
    val love: String,
    
    val work: String,
    
    val money: String,
    
    val health: String,
    
    val symbols: List<String>,
    
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
    
    val reason: String
)


