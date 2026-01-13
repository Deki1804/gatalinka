package com.gatalinka.app.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Helper klasa za Google Sign In funkcionalnost.
 */
object GoogleSignInHelper {
    
    /**
     * Kreira GoogleSignInClient sa potrebnim opcijama.
     * 
     * @param context Application context
     * @param webClientId Web Client ID iz Firebase Console (nalazi se u google-services.json)
     */
    fun getGoogleSignInClient(context: Context, webClientId: String): GoogleSignInClient {
        if (com.gatalinka.app.BuildConfig.DEBUG) {
            android.util.Log.d("GoogleSignInHelper", "🔍 Creating GoogleSignInClient with Web Client ID: $webClientId")
        }
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        
        if (com.gatalinka.app.BuildConfig.DEBUG) {
            android.util.Log.d("GoogleSignInHelper", "✅ GoogleSignInOptions created successfully")
        }
        
        return GoogleSignIn.getClient(context, gso)
    }
    
    /**
     * Kreira Intent za Google Sign In flow.
     */
    fun getSignInIntent(googleSignInClient: GoogleSignInClient): Intent {
        return googleSignInClient.signInIntent
    }
    
    /**
     * Parsira rezultat Google Sign In-a i vraća ID token.
     * 
     * @param data Intent data iz Activity result
     * @return Pair<ID token, error message> - token je null ako je greška, error message je null ako je uspješno
     */
    suspend fun getSignInResult(data: Intent?): Pair<String?, String?> {
        return try {
            if (data == null) {
                android.util.Log.e("GoogleSignInHelper", "❌ getSignInResult: data je null")
                return Pair(null, "Google Sign-In je otkazan ili nema podataka.")
            }
            
            android.util.Log.d("GoogleSignInHelper", "🔍 Parsiranje Google Sign-In rezultata...")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken == null) {
                android.util.Log.e("GoogleSignInHelper", "❌ ID token je null")
                Pair(null, "Nije moguće dobiti ID token od Google računa.")
            } else {
                android.util.Log.d("GoogleSignInHelper", "✅ ID token uspješno dobiven, duljina: ${idToken.length}")
                Pair(idToken, null)
            }
        } catch (e: ApiException) {
            android.util.Log.e("GoogleSignInHelper", "❌ ApiException: statusCode=${e.statusCode}, message=${e.message}")
            android.util.Log.e("GoogleSignInHelper", "Stack trace:", e)
            
            val errorMessage = when (e.statusCode) {
                com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR -> 
                    "Greška mreže. Provjeri internetsku vezu."
                com.google.android.gms.common.api.CommonStatusCodes.INTERNAL_ERROR -> 
                    "Interna greška Google servisa. Pokušaj ponovo."
                com.google.android.gms.common.api.CommonStatusCodes.INVALID_ACCOUNT -> 
                    "Neispravan Google račun."
                com.google.android.gms.common.api.CommonStatusCodes.SIGN_IN_REQUIRED -> 
                    "Potrebna je prijava."
                10 -> { // DEVELOPER_ERROR
                    val detailedError = """
                        Google Sign-In nije pravilno konfigurisan.
                        
                        Mogući uzroci:
                        1. SHA-1 fingerprint nije dodan u Firebase Console
                        2. Web Client ID nije ispravan
                        3. Package name se ne poklapa
                        
                        Status code: ${e.statusCode}
                        Detalji: ${e.message ?: "Nema dodatnih detalja"}
                    """.trimIndent()
                    android.util.Log.e("GoogleSignInHelper", detailedError)
                    detailedError
                }
                12501 -> // SIGN_IN_CANCELLED
                    "Google prijava je otkazana."
                12500 -> // SIGN_IN_CURRENTLY_IN_PROGRESS
                    "Google prijava je već u toku."
                else -> 
                    "Google prijava nije uspjela. Greška: ${e.statusCode}. ${e.message ?: ""}"
            }
            Pair(null, errorMessage)
        } catch (e: Exception) {
            android.util.Log.e("GoogleSignInHelper", "❌ Exception: ${e.javaClass.simpleName}, message=${e.message}")
            android.util.Log.e("GoogleSignInHelper", "Stack trace:", e)
            Pair(null, "Nepoznata greška pri Google prijavi: ${e.message ?: "Nepoznata greška"}")
        }
    }
    
    /**
     * Odjavi korisnika sa Google računa.
     */
    suspend fun signOut(context: Context, webClientId: String) {
        val googleSignInClient = getGoogleSignInClient(context, webClientId)
        googleSignInClient.signOut().await()
    }
}

