package com.gatalinka.app.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.HttpsCallableOptions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import com.gatalinka.app.api.dto.GatalinkaReadingDto
import com.gatalinka.app.data.UserInput
import com.google.firebase.ktx.app
import java.io.ByteArrayOutputStream

/**
 * Service za pozivanje Firebase Cloud Functions.
 * Zamjena za Retrofit HTTP pozive.
 */
object FirebaseFunctionsService {
    
    // Koristi istu Firebase app instancu kao Auth
    // VAŽNO: Firebase Functions SDK automatski prosljeđuje token iz Firebase.auth.currentUser
    // PROBLEM: Kada koristimo custom region (europe-west1), možda postoji problem sa prosljeđivanjem tokena
    // RJEŠENJE: Pokušajmo koristiti default region prvo (bez eksplicitnog regiona)
    // Ako funkcija nije u default regionu, moramo koristiti custom region
    private fun getFunctions(): FirebaseFunctions {
        // Koristi istu Firebase app instancu kao Auth
        // V2 funkcije su deployane u us-central1 regionu
        // Eksplicitno navodimo region za v2 funkcije
        return Firebase.functions(Firebase.app, "us-central1")
    }
    
    /**
     * Poziva readCup Cloud Function za čitanje iz šalice.
     * 
     * @param imageUri URI slike šalice
     * @param userInput Korisnički podaci (zodiac, gender) za personalizaciju
     * @return GatalinkaReadingDto s rezultatom čitanja
     */
    suspend fun readCup(
        imageUri: Uri,
        context: Context,
        userInput: UserInput? = null,
        readingMode: String = "instant"
    ): GatalinkaReadingDto {
        // DETALJNO LOGIRANJE - svaki API poziv
        val callId = System.currentTimeMillis()
        if (com.gatalinka.app.BuildConfig.DEBUG) {
            Log.w("🔥 API_CALL", "=== readCup CALL #$callId ===")
            Log.w("🔥 API_CALL", "imageUri: $imageUri")
            Log.w("🔥 API_CALL", "readingMode: $readingMode")
            Log.w("🔥 API_CALL", "Thread: ${Thread.currentThread().name}")
            Log.w("🔥 API_CALL", "Stack trace:")
            Thread.currentThread().stackTrace.take(5).forEach { 
                Log.w("🔥 API_CALL", "  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})")
            }
        }
        
        try {
            // Provjeri da li je korisnik prijavljen - koristi istu instancu kao AuthViewModel
            val auth = Firebase.auth
            val currentUser = auth.currentUser
            if (currentUser == null) {
                if (com.gatalinka.app.BuildConfig.DEBUG) {
                    Log.e("🔥 API_CALL", "❌ CALL #$callId FAILED: User not authenticated")
                }
                throw IllegalStateException("Korisnik mora biti prijavljen da može čitati iz šalice.")
            }
            
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                Log.w("🔥 API_CALL", "✅ CALL #$callId: User authenticated: ${currentUser.uid}")
            }
            
            // Osvježi ID token da osiguramo da je validan
            val idToken = currentUser.getIdToken(true).await()
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                Log.d("FirebaseFunctionsService", "Pozivanje readCup za korisnika: ${currentUser.uid}")
            }
            
            // Konvertiraj sliku u base64
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                Log.d("FirebaseFunctionsService", "Converting image to base64...")
            }
            val imageBase64 = try {
                imageUriToBase64(imageUri, context)
            } catch (e: Exception) {
                if (com.gatalinka.app.BuildConfig.DEBUG) {
                    Log.e("FirebaseFunctionsService", "Error converting image to base64", e)
                }
                throw e
            }
            
            // 1. LOG "DOKAZ" DA SU SLIKE RAZLIČITE
            val imageBytes = android.util.Base64.decode(imageBase64.replace("data:image/jpeg;base64,", ""), android.util.Base64.DEFAULT)
            val imageHash = java.security.MessageDigest.getInstance("SHA-256").digest(imageBytes)
            val hashString = imageHash.joinToString("") { "%02x".format(it) }
            val imageSize = imageBytes.size
            
            // Dobij dimensions
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
            val imageDimensions = "${options.outWidth}x${options.outHeight}"
            
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                android.util.Log.d("API_CALL", "=== IMAGE FINGERPRINT (ANDROID) ===")
                android.util.Log.d("API_CALL", "Image URI: $imageUri")
                android.util.Log.d("API_CALL", "Image hash (SHA-256): $hashString")
                android.util.Log.d("API_CALL", "Image size: $imageSize bytes")
                android.util.Log.d("API_CALL", "Image dimensions: $imageDimensions")
                android.util.Log.d("API_CALL", "Base64 length: ${imageBase64.length} characters")
                android.util.Log.d("API_CALL", "====================================")
            }
            
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                Log.d("FirebaseFunctionsService", "Image converted to base64, length: ${imageBase64.length}")
            }
            
            // Pripremi podatke za Cloud Function
            // Koristi displayName za zodiac sign (hrvatski naziv) umjesto enum name
            val zodiacSignDisplayName = userInput?.zodiacSign?.displayName ?: ""
            val genderName = userInput?.gender?.name ?: ""
            
            val data = hashMapOf(
                "imageBase64" to imageBase64,
                "zodiacSign" to zodiacSignDisplayName,
                "gender" to genderName,
                "focusArea" to "", // Za sada prazno, može se dodati kasnije
                "readingMode" to readingMode
            )
            
            // Log za debugging - DETALJNO
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                Log.d("FirebaseFunctionsService", "Sending to API: zodiac=$zodiacSignDisplayName, gender=$genderName, mode=$readingMode")
            }
            
            // Eksplicitno osvježi token prije poziva i provjeri da je validan
            val freshToken = currentUser.getIdToken(true).await()
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                Log.d("FirebaseFunctionsService", "Token osvježen")
            }
            
            // Pozovi Cloud Function - Firebase Functions SDK automatski prosljeđuje token iz Firebase.auth
            // Osiguraj da koristimo ispravnu Firebase app instancu i da je currentUser postavljen
            // SDK automatski prosljeđuje token, ali možemo eksplicitno provjeriti da je currentUser postavljen
            if (Firebase.auth.currentUser == null) {
                throw IllegalStateException("Firebase auth currentUser je null - token se ne može proslijediti")
            }
            
            // Osiguraj da koristimo istu Firebase app instancu za Functions kao za Auth
            // Koristimo istu instancu koja je definirana na vrhu klase
            val functionsInstance = getFunctions()
            
            // VAŽNO: Firebase Functions SDK automatski prosljeđuje token iz Firebase.auth.currentUser
            // Međutim, kada koristimo custom region (europe-west1), možda postoji problem
            // Pokušajmo koristiti default region prvo da vidimo da li to rješava problem
            // Ako funkcija nije u default regionu, moramo koristiti custom region
            // Ali možda trebamo koristiti drugačiji pristup za prosljeđivanje tokena
            
            // Eksplicitno osvježi token i provjeri da je validan
            val tokenString = freshToken.token
            if (tokenString == null) {
                throw IllegalStateException("Token je null - korisnik nije prijavljen")
            }
            
            // Firebase SDK automatski prosljeđuje token iz Firebase.auth.currentUser
            val callable = functionsInstance.getHttpsCallable("readCupCallable")
            val result = callable.call(data).await()
            
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                Log.d("FirebaseFunctionsService", "Function call successful")
            }
            
            // Parsiraj rezultat
            @Suppress("UNCHECKED_CAST")
            val resultData = result.data as? Map<String, Any>
                ?: throw IllegalStateException("Neočekivan format odgovora od Cloud Function: ${result.data?.javaClass?.simpleName}")
            
            val dto = try {
                mapToReadingDto(resultData)
            } catch (e: Exception) {
                if (com.gatalinka.app.BuildConfig.DEBUG) {
                    Log.e("FirebaseFunctionsService", "Error mapping to DTO", e)
                }
                throw e
            }
            
            return dto
        } catch (e: FirebaseFunctionsException) {
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                Log.e("FirebaseFunctionsService", "Firebase Functions error: ${e.code} - ${e.message}", e)
            }
            when (e.code) {
                FirebaseFunctionsException.Code.UNAUTHENTICATED -> {
                    throw IllegalStateException("Niste prijavljeni. Molimo prijavite se i pokušajte ponovo.")
                }
                FirebaseFunctionsException.Code.PERMISSION_DENIED -> {
                    throw IllegalStateException("Nemate dozvolu za ovu akciju.")
                }
                else -> {
                    throw RuntimeException("Greška pri pozivanju Cloud Function: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                Log.e("FirebaseFunctionsService", "Error in readCup", e)
            }
            throw RuntimeException("Greška pri pozivanju Cloud Function: ${e.message}", e)
        }
    }
    
    /**
     * Konvertira URI slike u base64 string.
     */
    private suspend fun imageUriToBase64(imageUri: Uri, context: Context): String {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw IllegalArgumentException("Nije moguće otvoriti sliku: $imageUri")
            
            try {
                // Pročitaj sliku i komprimiraj je
                val bitmap = BitmapFactory.decodeStream(inputStream)
                    ?: throw IllegalArgumentException("Nije moguće dekodirati sliku")
                
                val compressedBitmap = compressBitmap(bitmap)
                
                // Konvertiraj u base64
                val outputStream = ByteArrayOutputStream()
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val imageBytes = outputStream.toByteArray()
                
                Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            } finally {
                inputStream.close()
            }
        }
    }
    
    /**
     * Komprimira bitmap da smanji veličinu.
     */
    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val maxWidth = 1920
        val maxHeight = 1920
        
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val scale = minOf(
            maxWidth.toFloat() / width,
            maxHeight.toFloat() / height
        )
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Mapira Cloud Function odgovor u GatalinkaReadingDto.
     */
    private fun mapToReadingDto(data: Map<String, Any>): GatalinkaReadingDto {
        // Tolerant parsing za visible_symbols - probaj snake_case i camelCase
        val visibleSymbolsRaw = data["visible_symbols"] ?: data["visibleSymbols"]
        val visibleSymbols = when {
            visibleSymbolsRaw is List<*> -> {
                visibleSymbolsRaw.mapNotNull { symbolObj ->
                    if (symbolObj is Map<*, *>) {
                        // Tolerant parsing: probaj symbol/name i meaning/desc
                        val symbol = (symbolObj["symbol"] as? String) 
                            ?: (symbolObj["name"] as? String)
                            ?: ""
                        val meaning = (symbolObj["meaning"] as? String)
                            ?: (symbolObj["desc"] as? String)
                            ?: (symbolObj["description"] as? String)
                            ?: ""
                        
                        if (symbol.isNotEmpty()) {
                            com.gatalinka.app.api.dto.VisibleSymbolDto(
                                symbol = symbol,
                                meaning = meaning.ifEmpty { "Simbol u šalici" }
                            )
                        } else null
                    } else if (symbolObj is String) {
                        // Fallback: ako je samo string, koristi ga kao symbol
                        com.gatalinka.app.api.dto.VisibleSymbolDto(
                            symbol = symbolObj,
                            meaning = "Simbol u šalici"
                        )
                    } else null
                }
            }
            visibleSymbolsRaw is String -> {
                // Ako je string umjesto array, probaj parsirati kao JSON
                emptyList()
            }
            else -> emptyList()
        }
        
        // Tolerant parsing za interpretation - probaj različite ključeve
        val interpretation = (data["interpretation"] as? String)?.takeIf { it.isNotBlank() }
            ?: (data["how_to_interpret"] as? String)?.takeIf { it.isNotBlank() }
            ?: (data["howToInterpret"] as? String)?.takeIf { it.isNotBlank() }
        
        // Tolerant parsing za advice
        val advice = (data["advice"] as? String)?.takeIf { it.isNotBlank() }
            ?: (data["tip"] as? String)?.takeIf { it.isNotBlank() }
            ?: (data["savjet"] as? String)?.takeIf { it.isNotBlank() }
        
        // Tolerant parsing za horoscope_match
        val horoscopeMatch = (data["horoscope_match"] as? String)?.takeIf { it.isNotBlank() }
            ?: (data["horoscopeMatch"] as? String)?.takeIf { it.isNotBlank() }
        
        // Log error code i image fingerprint za debugging
        val errorCode = (data["error_code"] as? String) ?: (data["errorCode"] as? String)
        val imageHash = (data["image_hash"] as? String) ?: (data["imageHash"] as? String)
        val imageSize = (data["image_size"] as? Number)?.toInt() ?: (data["imageSize"] as? Number)?.toInt()
        val imageDimensions = (data["image_dimensions"] as? String) ?: (data["imageDimensions"] as? String)
        
        if (com.gatalinka.app.BuildConfig.DEBUG) {
            android.util.Log.d("API_CALL", "=== READING RESPONSE DEBUG ===")
            android.util.Log.d("API_CALL", "error_code: $errorCode")
            android.util.Log.d("API_CALL", "image_hash: $imageHash")
            android.util.Log.d("API_CALL", "image_size: $imageSize bytes")
            android.util.Log.d("API_CALL", "image_dimensions: $imageDimensions")
            android.util.Log.d("API_CALL", "visible_symbols count: ${visibleSymbols.size}")
            android.util.Log.d("API_CALL", "symbols: ${visibleSymbols.map { it.symbol }.joinToString(", ")}")
            android.util.Log.d("API_CALL", "================================")
        }
        
        return GatalinkaReadingDto(
            mainText = (data["main_text"] as? String) ?: (data["mainText"] as? String) ?: "",
            visibleSymbols = visibleSymbols.ifEmpty { null },
            interpretation = interpretation,
            advice = advice,
            love = (data["love"] as? String) ?: "",
            work = (data["work"] as? String) ?: "",
            money = (data["money"] as? String) ?: "",
            health = (data["health"] as? String) ?: "",
            symbols = (data["symbols"] as? List<*>)?.mapNotNull { 
                when (it) {
                    is String -> it
                    is Map<*, *> -> (it["symbol"] as? String) ?: (it["name"] as? String)
                    else -> null
                }
            } ?: emptyList(),
            luckyNumbers = (data["lucky_numbers"] as? List<*>)?.mapNotNull {
                when (it) {
                    is Number -> it.toInt()
                    is String -> it.toIntOrNull()
                    else -> null
                }
            } ?: (data["luckyNumbers"] as? List<*>)?.mapNotNull {
                when (it) {
                    is Number -> it.toInt()
                    is String -> it.toIntOrNull()
                    else -> null
                }
            } ?: emptyList(),
            luckScore = ((data["luck_score"] as? Number)?.toInt()) 
                ?: ((data["luckScore"] as? Number)?.toInt()) 
                ?: 0,
            mantra = (data["mantra"] as? String) ?: "Danas je dan za nove mogućnosti.",
            energyScore = ((data["energy_score"] as? Number)?.toInt())
                ?: ((data["energyScore"] as? Number)?.toInt())
                ?: 50,
            isValidCup = (data["is_valid_cup"] as? Boolean) 
                ?: (data["isValidCup"] as? Boolean) 
                ?: false,
            safetyLevel = (data["safety_level"] as? String)
                ?: (data["safetyLevel"] as? String)
                ?: "unknown",
            reason = (data["reason"] as? String) ?: "",
            horoscopeMatch = horoscopeMatch,
            errorCode = errorCode,
            imageHash = imageHash,
            imageSize = imageSize,
            imageDimensions = imageDimensions
        )
    }
    
    /**
     * Poziva getDailyReading Cloud Function za dnevno čitanje bez slike.
     * 
     * @param userInput Korisnički podaci (zodiac, gender) za personalizaciju
     * @return GatalinkaReadingDto s rezultatom dnevnog čitanja
     */
    suspend fun getDailyReading(
        userInput: UserInput? = null
    ): GatalinkaReadingDto {
        // DETALJNO LOGIRANJE - svaki API poziv
        val callId = System.currentTimeMillis()
        if (com.gatalinka.app.BuildConfig.DEBUG) {
            Log.w("🔥 API_CALL", "=== getDailyReading CALL #$callId ===")
            Log.w("🔥 API_CALL", "zodiacSign: ${userInput?.zodiacSign?.displayName}")
            Log.w("🔥 API_CALL", "gender: ${userInput?.gender?.name}")
            Log.w("🔥 API_CALL", "Thread: ${Thread.currentThread().name}")
            Log.w("🔥 API_CALL", "Stack trace:")
            Thread.currentThread().stackTrace.take(5).forEach { 
                Log.w("🔥 API_CALL", "  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})")
            }
        }
        
        try {
            val auth = Firebase.auth
            val currentUser = auth.currentUser
            if (currentUser == null) {
                if (com.gatalinka.app.BuildConfig.DEBUG) {
                    Log.e("🔥 API_CALL", "❌ CALL #$callId FAILED: User not authenticated")
                }
                throw IllegalStateException("Korisnik mora biti prijavljen.")
            }
            
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                Log.w("🔥 API_CALL", "✅ CALL #$callId: User authenticated: ${currentUser.uid}")
            }
            
            val functions = getFunctions()
            val callable = functions.getHttpsCallable("getDailyReadingCallable")
            
            // Pripremi podatke
            val data = hashMapOf(
                "zodiacSign" to (userInput?.zodiacSign?.displayName ?: ""),
                "gender" to (userInput?.gender?.name ?: "")
            )
            
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                Log.w("🔥 API_CALL", "📤 CALL #$callId: Sending request to Firebase Functions")
            }
            
            val result = callable.call(data).await()
            
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                Log.w("🔥 API_CALL", "✅ CALL #$callId: Response received successfully")
            }
            // Parsiraj rezultat
            @Suppress("UNCHECKED_CAST")
            val resultData = result.data as? Map<String, Any>
                ?: throw IllegalStateException("Neočekivani format odgovora od Cloud Function: ${result.data?.javaClass?.simpleName}")
            
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                Log.d("FirebaseFunctionsService", "Daily reading received successfully")
            }
            return mapToReadingDto(resultData)
            
        } catch (e: FirebaseFunctionsException) {
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                Log.e("FirebaseFunctionsService", "Firebase Functions error", e)
            }
            throw when (e.code) {
                FirebaseFunctionsException.Code.UNAUTHENTICATED ->
                    IllegalStateException("Niste prijavljeni. Molimo prijavite se i pokušajte ponovo.")
                FirebaseFunctionsException.Code.PERMISSION_DENIED ->
                    IllegalStateException("Nemate dozvolu za ovu akciju.")
                else ->
                    IllegalStateException("Greška pri dohvaćanju dnevnog čitanja: ${e.message ?: e.code.name}")
            }
        } catch (e: Exception) {
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                Log.e("FirebaseFunctionsService", "Error getting daily reading", e)
            }
            throw IllegalStateException("Nešto je pošlo po zlu: ${e.message ?: e.javaClass.simpleName}")
        }
    }
}

