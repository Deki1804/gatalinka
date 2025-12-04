# Firebase Setup za Gatalinka App

## Koraci za podešavanje Firebase Authentication

### 1. Kreiraj Firebase Projekat

1. Idite na [Firebase Console](https://console.firebase.google.com/)
2. Kliknite "Add project" ili izaberite postojeći projekat
3. Unesite naziv projekta (npr. "Gatalinka")
4. Pratite korake da završite kreiranje projekta

### 2. Dodaj Android App u Firebase

1. U Firebase Console, kliknite na Android ikonicu
2. Unesite package name: `com.gatalinka.app`
3. Unesite app nickname: "Gatalinka"
4. Download `google-services.json` fajl
5. Postavite `google-services.json` u `apps/android/app/` direktorijum

### 3. Omogući Authentication

1. U Firebase Console, idite na "Authentication" u levom meniju
2. Kliknite "Get started"
3. Omogući "Email/Password" sign-in method:
   - Kliknite na "Email/Password"
   - Uključite "Enable"
   - Kliknite "Save"
4. Omogući "Google" sign-in method:
   - Kliknite na "Google"
   - Uključite "Enable"
   - Unesite project support email
   - Kliknite "Save"

### 4. Konfigurisanje Google Sign In

1. U Firebase Console, idite na "Project Settings" (ikonica zupčanika)
2. Idite na "Your apps" sekciju
3. Kliknite na Android app
4. Idite na "SHA certificate fingerprints"
5. Dodaj SHA-1 fingerprint vašeg debug keystore-a:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
6. Kopiraj SHA-1 hash i dodaj ga u Firebase Console

### 5. Firebase Dependencies

Dependencies su već dodati u `build.gradle.kts`:
- Firebase BOM
- Firebase Auth
- Google Sign In

### 6. Integracija u Kod

AuthViewModel (`apps/android/app/src/main/java/com/gatalinka/app/vm/AuthViewModel.kt`) je spreman za integraciju.
Trebate samo odkomentarisati Firebase kod i dodati google-services.json fajl.

### TODO u AuthViewModel:

1. Import Firebase Auth:
   ```kotlin
   import com.google.firebase.auth.FirebaseAuth
   import com.google.firebase.auth.ktx.auth
   ```

2. Za Email/Password sign-in:
   ```kotlin
   FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
       .await()
   ```

3. Za Google Sign In:
   ```kotlin
   val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
       .requestIdToken(getString(R.string.default_web_client_id))
       .requestEmail()
       .build()
   ```

### 7. Testiranje

Nakon što dodate `google-services.json` fajl, možete testirati:
- Email/Password login
- Email/Password registration  
- Google Sign In

## Napomene

- `google-services.json` mora biti u `apps/android/app/` direktorijumu
- Uvek dodaj SHA-1 fingerprint kada testiraš na fizičkom uređaju
- Za production build, moraš dodati i release keystore SHA-1

