package com.gatalinka.app.util

import java.text.SimpleDateFormat
import java.util.Locale

object DateValidators {
    private val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).apply {
        isLenient = false
    }
    fun isValidDob(input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return false
        
        // Provjeri točan format dd.MM.yyyy - jednostavnija provjera
        if (trimmed.length != 10) return false
        if (trimmed[2] != '.' || trimmed[5] != '.') return false
        
        // Provjeri da su svi ostali karakteri brojevi
        val parts = trimmed.split(".")
        if (parts.size != 3) return false
        if (parts[0].length != 2 || parts[1].length != 2 || parts[2].length != 4) return false
        if (!parts[0].all { it.isDigit() } || !parts[1].all { it.isDigit() } || !parts[2].all { it.isDigit() }) return false
        
        // Provjeri je li datum stvarno valjan (npr. ne 32.13.2000)
        return try { 
            val parsed = sdf.parse(trimmed)
            if (parsed == null) return false
            // Provjeri da datum nije u budućnosti (ali dozvoli današnji dan)
            parsed.time <= System.currentTimeMillis() + 86400000 // +1 dan tolerance
        } catch (e: Exception) { 
            false 
        }
    }
}
