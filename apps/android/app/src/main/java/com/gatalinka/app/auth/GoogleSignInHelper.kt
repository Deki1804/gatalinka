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
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        
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
     * @return ID token ako je uspješno, null ako je greška
     */
    suspend fun getSignInResult(data: Intent?): String? {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            account?.idToken
        } catch (e: ApiException) {
            null
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

