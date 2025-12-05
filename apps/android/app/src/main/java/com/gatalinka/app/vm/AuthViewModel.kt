package com.gatalinka.app.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel za autentifikaciju korisnika.
 * Koristi Firebase Authentication.
 */
class AuthViewModel : ViewModel() {
    
    private val auth: FirebaseAuth = Firebase.auth
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.NotAuthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        // Provjeri je li korisnik već prijavljen
        checkAuthState()
        
        // Slušaj promene u autentifikaciji
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                _authState.value = AuthState.Authenticated(
                    userId = user.uid,
                    email = user.email,
                    displayName = user.displayName
                )
            } else {
                _authState.value = AuthState.NotAuthenticated
            }
        }
    }
    
    /**
     * Prijava sa email/password.
     */
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                Log.d("AuthViewModel", "Pokušavam prijavu sa emailom: ${email.take(3)}...")
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                
                if (user != null) {
                    Log.d("AuthViewModel", "✅ Uspješna prijava: ${user.uid}")
                    _authState.value = AuthState.Authenticated(
                        userId = user.uid,
                        email = user.email,
                        displayName = user.displayName
                    )
                }
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Greška pri email prijavi: ${e.message}", e)
                _errorMessage.value = when {
                    e.message?.contains("not allowed", ignoreCase = true) == true ||
                    e.message?.contains("sign-in provider is disabled", ignoreCase = true) == true ||
                    e.message?.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) == true ->
                        "Email/Password prijava nije omogućena u Firebase projektu. Molimo kontaktirajte administratora."
                    e.message?.contains("email") == true -> "Email adresa nije valjana"
                    e.message?.contains("password") == true -> "Lozinka nije ispravna"
                    e.message?.contains("user") == true && e.message?.contains("found") == true -> "Korisnik s ovim emailom ne postoji"
                    e.message?.contains("password") == true && e.message?.contains("invalid") == true -> "Pogrešna lozinka"
                    e.message?.contains("network") == true -> "Provjeri internetsku vezu"
                    else -> e.message ?: "Greška pri prijavi"
                }
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Registracija sa email/password.
     */
    fun signUpWithEmail(email: String, password: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                
                if (user != null) {
                    // Ažuriraj display name
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    user.updateProfile(profileUpdates).await()
                    
                    _authState.value = AuthState.Authenticated(
                        userId = user.uid,
                        email = user.email,
                        displayName = name
                    )
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = when {
                    e.message?.contains("not allowed", ignoreCase = true) == true ||
                    e.message?.contains("sign-in provider is disabled", ignoreCase = true) == true ||
                    e.message?.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) == true ->
                        "Email/Password registracija nije omogućena u Firebase projektu. Molimo kontaktirajte administratora."
                    e.message?.contains("email") == true -> "Email adresa nije valjana ili već postoji"
                    e.message?.contains("password") == true -> "Lozinka mora imati najmanje 6 znakova"
                    e.message?.contains("already") == true -> "Korisnik s ovim emailom već postoji"
                    e.message?.contains("network") == true -> "Provjeri internetsku vezu"
                    else -> e.message ?: "Greška pri registraciji"
                }
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Google Sign In - zahteva Intent koji se vraća iz Google Sign In flow-a.
     * Ova metoda će biti pozvana iz Activity-a nakon što se vrati rezultat.
     */
    fun signInWithGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                Log.d("AuthViewModel", "Pokušavam Google prijavu sa ID tokenom")
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user
                
                if (user != null) {
                    Log.d("AuthViewModel", "✅ Uspješna Google prijava: ${user.uid}")
                    _authState.value = AuthState.Authenticated(
                        userId = user.uid,
                        email = user.email,
                        displayName = user.displayName
                    )
                }
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Greška pri Google prijavi: ${e.message}", e)
                _errorMessage.value = when {
                    e.message?.contains("not allowed", ignoreCase = true) == true ||
                    e.message?.contains("sign-in provider is disabled", ignoreCase = true) == true ||
                    e.message?.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) == true ->
                        "Google prijava nije omogućena u Firebase projektu. Molimo kontaktirajte administratora."
                    e.message?.contains("network") == true -> "Provjeri internetsku vezu"
                    e.message?.contains("10:", ignoreCase = true) == true -> "Google prijava nije uspjela. Pokušajte ponovo."
                    else -> e.message ?: "Greška pri Google prijavi"
                }
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Postavi error poruku (korisno za UI validaciju).
     */
    fun setError(message: String?) {
        _errorMessage.value = message
    }
    
    /**
     * Odjava korisnika.
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _authState.value = AuthState.NotAuthenticated
            } catch (e: Exception) {
                _errorMessage.value = "Greška pri odjavi: ${e.message}"
            }
        }
    }
    
    /**
     * Provjeri je li korisnik prijavljen.
     */
    private fun checkAuthState() {
        val user = auth.currentUser
        if (user != null) {
            _authState.value = AuthState.Authenticated(
                userId = user.uid,
                email = user.email,
                displayName = user.displayName
            )
        }
    }
}

/**
 * Stanje autentifikacije.
 */
sealed class AuthState {
    data object NotAuthenticated : AuthState()
    data class Authenticated(
        val userId: String,
        val email: String?,
        val displayName: String? = null
    ) : AuthState()
}
