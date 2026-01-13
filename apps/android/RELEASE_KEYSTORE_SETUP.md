# Release Keystore Setup

## ⚠️ VAŽNO: Keystore je KRITIČAN za release!

Bez release keystore-a ne možeš:
- Uploadati app na Google Play Store
- Update-ati postojeću verziju app-a
- Potpisati release build

**NE COMMITAJ keystore u git!**

---

## Korak 1: Kreiraj Release Keystore

Otvori terminal u `apps/android` direktoriju:

```bash
cd apps/android
keytool -genkey -v -keystore gatalinka-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias gatalinka
```

### Pitanja koja će se pojaviti:

1. **Enter keystore password:** (unesi jaku lozinku, zapamti je!)
2. **Re-enter new password:** (potvrdi lozinku)
3. **What is your first and last name?** 
   - Unesi: `Gatalinka App` (ili svoje ime)
4. **What is the name of your organizational unit?**
   - Unesi: `Development`
5. **What is the name of your organization?**
   - Unesi: `Gatalinka` (ili ime tvrtke)
6. **What is the name of your City or Locality?**
   - Unesi: `Zagreb` (ili svoj grad)
7. **What is the name of your State or Province?**
   - Unesi: `Zagreb` (ili svoju županiju)
8. **What is the two-letter country code for this unit?**
   - Unesi: `HR`
9. **Is CN=Gatalinka App, OU=Development, O=Gatalinka, L=Zagreb, ST=Zagreb, C=HR correct?**
   - Unesi: `yes`
10. **Enter key password for <gatalinka>**
    - Unesi: `RETURN` (koristi istu lozinku kao keystore)
    - Ili unesi novu lozinku (ali zapamti je!)

### Rezultat:
- Keystore će biti kreiran u: `apps/android/gatalinka-release.jks`
- **SPREMI LOZINKU U PASSWORD MANAGER!**

---

## Korak 2: Dodaj u .gitignore

Provjeri da `apps/android/.gitignore` sadrži:
```
*.jks
*.keystore
gatalinka-release.jks
```

---

## Korak 3: Dodaj Signing Config u build.gradle.kts

Dodaj u `android { }` blok u `apps/android/app/build.gradle.kts`:

```kotlin
android {
    // ... postojeća konfiguracija
    
    signingConfigs {
        create("release") {
            storeFile = file("../gatalinka-release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = "gatalinka"
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

---

## Korak 4: Postavi Environment Variables (Opcijski)

Umjesto hardkodiranih lozinki, možeš koristiti environment variables:

### Windows (PowerShell):
```powershell
$env:KEYSTORE_PASSWORD = "tvoja_lozinka"
$env:KEY_PASSWORD = "tvoja_lozinka"
```

### Linux/Mac:
```bash
export KEYSTORE_PASSWORD="tvoja_lozinka"
export KEY_PASSWORD="tvoja_lozinka"
```

**ILI** možeš direktno unijeti lozinke u `build.gradle.kts` (ali **NE COMMITAJ** to u git!):

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../gatalinka-release.jks")
        storePassword = "tvoja_lozinka"  // ⚠️ NE COMMITAJ!
        keyAlias = "gatalinka"
        keyPassword = "tvoja_lozinka"    // ⚠️ NE COMMITAJ!
    }
}
```

---

## Korak 5: Testiraj Release Build

```bash
cd apps/android
./gradlew assembleRelease
```

APK će biti u: `app/build/outputs/apk/release/app-release.apk`

**Provjeri:**
- App se može instalirati
- App radi normalno
- Nema crashova

---

## Korak 6: Build AAB za Google Play

```bash
cd apps/android
./gradlew bundleRelease
```

AAB će biti u: `app/build/outputs/bundle/release/app-release.aab`

**Ovaj AAB uploadaj na Google Play Console!**

---

## 🔐 Sigurnost

1. **Backup keystore-a:**
   - Spremi `gatalinka-release.jks` na sigurno mjesto (USB, cloud storage s enkripcijom)
   - Spremi lozinku u password manager

2. **NE COMMITAJ:**
   - `gatalinka-release.jks`
   - Lozinke u `build.gradle.kts`
   - Environment variables s lozinkama

3. **Ako izgubiš keystore:**
   - **NE MOŽEŠ** update-ati postojeću app na Store-u
   - Moraš kreirati novu app (novi package name)

---

## ✅ Provjera

Nakon što dodaš signing config, provjeri da build radi:

```bash
cd apps/android
./gradlew clean
./gradlew bundleRelease
```

Ako sve prođe bez greške → ✅ Spremno za upload!
