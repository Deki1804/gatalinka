package com.gatalinka.app.util

import java.text.SimpleDateFormat
import java.util.*

enum class ZodiacSign(val emoji: String, val displayName: String) {
    Aries("♈", "Ovan"),
    Taurus("♉", "Bik"),
    Gemini("♊", "Blizanci"),
    Cancer("♋", "Rak"),
    Leo("♌", "Lav"),
    Virgo("♍", "Djevica"),
    Libra("♎", "Vaga"),
    Scorpio("♏", "Škorpion"),
    Sagittarius("♐", "Strijelac"),
    Capricorn("♑", "Jarac"),
    Aquarius("♒", "Vodenjak"),
    Pisces("♓", "Ribe")
}

object ZodiacCalculator {
    private val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).apply {
        isLenient = false
    }

    fun calculateZodiac(birthdate: String): ZodiacSign? {
        if (birthdate.isBlank()) return null
        
        return try {
            val date = sdf.parse(birthdate.trim()) ?: return null
            val calendar = Calendar.getInstance().apply { time = date }
            val month = calendar.get(Calendar.MONTH) + 1 // 1-12
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            when {
                (month == 3 && day >= 21) || (month == 4 && day <= 19) -> ZodiacSign.Aries
                (month == 4 && day >= 20) || (month == 5 && day <= 20) -> ZodiacSign.Taurus
                (month == 5 && day >= 21) || (month == 6 && day <= 20) -> ZodiacSign.Gemini
                (month == 6 && day >= 21) || (month == 7 && day <= 22) -> ZodiacSign.Cancer
                (month == 7 && day >= 23) || (month == 8 && day <= 22) -> ZodiacSign.Leo
                (month == 8 && day >= 23) || (month == 9 && day <= 22) -> ZodiacSign.Virgo
                (month == 9 && day >= 23) || (month == 10 && day <= 22) -> ZodiacSign.Libra
                (month == 10 && day >= 23) || (month == 11 && day <= 21) -> ZodiacSign.Scorpio
                (month == 11 && day >= 22) || (month == 12 && day <= 21) -> ZodiacSign.Sagittarius
                (month == 12 && day >= 22) || (month == 1 && day <= 19) -> ZodiacSign.Capricorn
                (month == 1 && day >= 20) || (month == 2 && day <= 18) -> ZodiacSign.Aquarius
                (month == 2 && day >= 19) || (month == 3 && day <= 20) -> ZodiacSign.Pisces
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}







