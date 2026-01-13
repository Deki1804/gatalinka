# Google Sign-In - Kompletan Checklist

## ✅ Što je provjereno

### 1. Package Name
- ✅ `com.gatalinka.app` u `build.gradle.kts`
- ✅ `com.gatalinka.app` u `AndroidManifest.xml`
- ✅ `com.gatalinka.app` u `google-services.json`

### 2. Web Client ID
- ✅ U `strings.xml`: `253930174034-f9su74ckmll14gaqelpo9ng046lnvjpq.apps.googleusercontent.com`
- ✅ U Firebase Authentication: `253930174034-f9su74ckmll14gaqelpo9ng046lnvjpq.apps.googleusercontent.com`
- ✅ U `google-services.json`: `253930174034-f9su74ckmll14gaqelpo9ng046lnvjpq.apps.googleusercontent.com`

### 3. SHA-1 Fingerprints
- ✅ Debug SHA-1: `AB:76:ED:3E:0C:48:E3:DA:CA:17:E1:97:B9:10:C9:98:85:FF:CB:73`
- ✅ U `google-services.json`: `ab76ed3e0c48e3daca17e197b910c99885ffcb73` (lowercase, bez dvotočaka)
- ✅ U Firebase Console: Dodan
- ✅ U Google Cloud Console: Stariji client (Nov 27) ima ovaj SHA-1

### 4. Android Client ID
- ✅ U `google-services.json`: `253930174034-qcag6hthb953qqrdh31jbltqpsh5cint.apps.googleusercontent.com`
- ✅ U Google Cloud Console: Stariji client (Nov 27) ima ovaj Client ID

## ❌ Što treba provjeriti

### 1. Provjeri SHA-1 koji se STVARNO koristi
**VAŽNO:** Možda se koristi drugačiji debug keystore!

```powershell
cd apps/android
.\get-sha1.ps1
```

Ili ručno:
```powershell
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

**Provjeri:**
- Da li je SHA-1 koji se prikaže: `AB:76:ED:3E:0C:48:E3:DA:CA:17:E1:97:B9:10:C9:98:85:FF:CB:73`?
- Ako NIJE, to je problem! Treba dodati pravi SHA-1.

### 2. Provjeri Google Cloud Console - OAuth Consent Screen
1. Google Cloud Console → **APIs & Services** → **OAuth consent screen**
2. Provjeri da je **User Type** postavljen (Internal ili External)
3. Provjeri da je **App name** postavljen
4. Provjeri da je **Support email** postavljen
5. Provjeri da je **Authorized domains** postavljen (ako je External)

### 3. Provjeri da je Google Sign-In API omogućen
1. Google Cloud Console → **APIs & Services** → **Enabled APIs & services**
2. Provjeri da je **Google Sign-In API** omogućen
3. Ako nije, klikni **+ ENABLE APIS AND SERVICES** i omogući **Google Sign-In API**

### 4. Provjeri da je Android Client ID točan u Google Cloud Console
1. Google Cloud Console → **APIs & Services** → **Credentials**
2. Pronađi Android client (Nov 27, 2025, `253930174034-qcag...`)
3. Provjeri:
   - Package name: `com.gatalinka.app`
   - SHA-1: `AB:76:ED:3E:0C:48:E3:DA:CA:17:E1:97:B9:10:C9:98:85:FF:CB:73`
   - Client ID: `253930174034-qcag6hthb953qqrdh31jbltqpsh5cint.apps.googleusercontent.com`

### 5. Re-download google-services.json
Nakon što si obrisao noviji client, možda treba re-downloadati `google-services.json`:

1. Firebase Console → Project Settings → Your apps → Android app
2. Klikni **Download google-services.json**
3. Zamijeni postojeći `apps/android/app/google-services.json`
4. Rebuild app u Android Studio

### 6. Provjeri da nema više Android client-a
1. Google Cloud Console → **APIs & Services** → **Credentials**
2. Provjeri da postoji SAMO JEDAN Android client s:
   - Package name: `com.gatalinka.app`
   - SHA-1: `AB:76:ED:3E:0C:48:E3:DA:CA:17:E1:97:B9:10:C9:98:85:FF:CB:73`
3. Ako postoji više, obriši sve osim onog koji ima ispravan SHA-1

## 🔧 Rješenje - Korak po korak

### Korak 1: Provjeri SHA-1 koji se stvarno koristi
```powershell
cd apps/android
.\get-sha1.ps1
```

**Ako SHA-1 NIJE `AB:76:ED:3E:0C:48:E3:DA:CA:17:E1:97:B9:10:C9:98:85:FF:CB:73`:**
1. Kopiraj SHA-1 koji se prikaže
2. Dodaj ga u Firebase Console → Project Settings → Your apps → Android app → SHA certificate fingerprints
3. Dodaj ga u Google Cloud Console → APIs & Services → Credentials → Android client → SHA-1 certificate fingerprint

### Korak 2: Provjeri OAuth Consent Screen
1. Google Cloud Console → **APIs & Services** → **OAuth consent screen**
2. Ako nije konfiguriran, konfiguriraj ga:
   - User Type: **External** (ako nemaš Google Workspace) ili **Internal** (ako imaš)
   - App name: **Gatalinka**
   - Support email: **larrydj@gmail.com** (ili tvoj email)
   - Authorized domains: (ako je External, dodaj svoj domen)
3. Klikni **Save and Continue** kroz sve korake

### Korak 3: Omogući Google Sign-In API
1. Google Cloud Console → **APIs & Services** → **Enabled APIs & services**
2. Klikni **+ ENABLE APIS AND SERVICES**
3. Traži **Google Sign-In API**
4. Klikni na **Google Sign-In API**
5. Klikni **ENABLE**

### Korak 4: Re-download google-services.json
1. Firebase Console → Project Settings → Your apps → Android app
2. Klikni **Download google-services.json**
3. Zamijeni `apps/android/app/google-services.json`
4. U Android Studio: **File** → **Sync Project with Gradle Files**

### Korak 5: Clean & Rebuild
1. Android Studio → **Build** → **Clean Project**
2. Android Studio → **Build** → **Rebuild Project**
3. Restartaj app

### Korak 6: Sačekaj propagaciju
1. Sačekaj **10-15 minuta** nakon svih promjena
2. Restartaj app
3. Pokušaj Google Sign-In ponovo

## 🐛 Ako i dalje ne radi

Provjeri logove:
```
tag:LoginScreen | tag:GoogleSignInHelper | tag:AuthViewModel
```

Ako vidiš status code 10, provjeri:
1. Da li je SHA-1 točan (korak 1)
2. Da li je OAuth Consent Screen konfiguriran (korak 2)
3. Da li je Google Sign-In API omogućen (korak 3)

