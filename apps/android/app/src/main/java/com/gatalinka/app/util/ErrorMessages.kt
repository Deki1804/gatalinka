package com.gatalinka.app.util

/**
 * Centralizirani error messages za cijeli app
 * Osigurava konzistentnost i lako održavanje
 */
object ErrorMessages {
    // Authentication errors
    const val NOT_LOGGED_IN = "Niste prijavljeni. Molimo prijavite se i pokušajte ponovo."
    const val AUTH_FAILED = "Greška pri prijavi. Provjerite svoje podatke i pokušajte ponovo."
    
    // Network errors
    const val NETWORK_ERROR = "Ne mogu se spojiti na server. Provjeri internetsku vezu."
    const val TIMEOUT_ERROR = "Vrijeme je isteklo. Pokušaj ponovo."
    const val CONNECTION_ERROR = "Ne mogu se spojiti na Gatalinku. Provjeri internetsku vezu."
    
    // Reading errors
    const val READING_FAILED = "Greška pri čitanju. Pokušaj ponovo s jasnom slikom šalice kave."
    const val IMAGE_TOO_SMALL = "📸 Fotografija je premala\n\nPribliži šalicu i slikaj u boljem svjetlu. Šalica treba biti jasno vidljiva i oštra."
    const val BAD_ASPECT_RATIO = "📐 Pogrešan kut\n\nFotkaj šalicu odozgo, direktno. Šalica treba biti u centru okvira, kao krug."
    const val TOO_DARK = "🌙 Previše tamno\n\nUključi svjetlo ili priđi bliže prozoru. Talog mora biti dovoljno vidljiv."
    const val TOO_BRIGHT = "☀️ Previše svijetlo\n\nPokušaj bez blica ili malo dalje od svjetla. Trebamo vidjeti detalje taloga."
    const val LOW_CONTRAST = "🔍 Mutna slika\n\nDrži mobitel mirnije i približi se. Trebamo jasno vidjeti oblike u talogu."
    const val ANALYSIS_FAILED = "⚠️ Greška pri analizi\n\nPokušaj ponovno ili odaberi drugu fotografiju. Provjeri da je šalica jasno vidljiva."
    const val NSFW_DETECTED = "🚫 Fotografija nije prikladna\n\nMolimo koristi fotografiju šalice kave za čitanje."
    const val NOT_A_CUP = "📸 Ova slika nije prikladna\n\nMolimo fotkajte šalicu kave odozgo, u dobrom svjetlu."
    
    // Generic errors
    const val UNKNOWN_ERROR = "❌ Nešto je pošlo po zlu\n\nPokušaj ponovo ili kontaktiraj podršku."
    const val PERMISSION_DENIED = "Nemate dozvolu za ovu akciju. Provjerite postavke aplikacije."
    const val SAVE_FAILED = "Greška pri spremanju. Pokušaj ponovo."
    const val LOAD_FAILED = "Greška pri učitavanju. Pokušaj ponovo."
    
    /**
     * Mapira reason string na user-friendly poruku
     */
    fun getReadingErrorMessage(reason: String?): String {
        return when (reason) {
            "image_too_small", "image_too_small_dimensions" -> IMAGE_TOO_SMALL
            "bad_aspect_ratio" -> BAD_ASPECT_RATIO
            "too_dark" -> TOO_DARK
            "too_bright" -> TOO_BRIGHT
            "low_contrast" -> LOW_CONTRAST
            "analysis_failed" -> ANALYSIS_FAILED
            "nsfw_detected", "nsfw" -> NSFW_DETECTED
            "not_a_cup" -> NOT_A_CUP
            else -> READING_FAILED
        }
    }
    
    /**
     * Mapira exception na user-friendly poruku
     */
    fun getErrorMessage(exception: Throwable?): String {
        val message = exception?.message ?: ""
        return when {
            message.contains("prijavljen", ignoreCase = true) ||
            message.contains("authenticated", ignoreCase = true) ||
            message.contains("UNAUTHENTICATED", ignoreCase = true) -> NOT_LOGGED_IN
            message.contains("timeout", ignoreCase = true) -> TIMEOUT_ERROR
            message.contains("network", ignoreCase = true) ||
            message.contains("Unable to resolve host", ignoreCase = true) -> NETWORK_ERROR
            message.contains("PERMISSION_DENIED", ignoreCase = true) ||
            message.contains("permission", ignoreCase = true) -> PERMISSION_DENIED
            else -> UNKNOWN_ERROR
        }
    }
}

