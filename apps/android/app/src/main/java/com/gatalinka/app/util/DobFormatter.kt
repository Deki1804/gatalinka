package com.gatalinka.app.util

object DobFormatter {
    /**
     * Ulaz: originalni text + pozicija kursora (end).
     * Ideja: prebroji koliko je DIGIT-a bilo lijevo od kursora,
     * pa nakon formatiranja (dd.MM.yyyy) postavi kursor iza tog istog broja digit-a.
     */
    fun formatKeepingCursor(rawText: String, cursor: Int): Pair<String, Int> {
        val digits = StringBuilder()
        val digitsBeforeCursor = run {
            var count = 0
            for (i in rawText.indices) {
                val ch = rawText[i]
                val isDigit = ch.isDigit()
                if (isDigit) digits.append(ch)
                if (i < cursor && isDigit) count++
            }
            count
        }

        val clipped = digits.toString().take(8) // ddMMyyyy
        // Formatiraj u dd.MM.yyyy
        val out = buildString {
            for (i in clipped.indices) {
                append(clipped[i])
                if (i == 1 || i == 3) append('.')
            }
        }

        // Izračunaj novu poziciju kursora: mjesto nakon "digitsBeforeCursor"-tog digit-a u formatiranom stringu
        var seen = 0
        var newPos = 0
        while (newPos < out.length && seen < digitsBeforeCursor) {
            if (out[newPos].isDigit()) seen++
            newPos++
        }
        return out to newPos.coerceAtMost(out.length)
    }
}
