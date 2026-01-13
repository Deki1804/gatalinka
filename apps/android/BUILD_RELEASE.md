# Build Release AAB - Brzi Vodič

## ⚠️ VAŽNO: Prvo postavi lozinke!

Prije build-a, postavi environment variables s lozinkama koje si koristio za keystore:

### Windows PowerShell:
```powershell
cd C:\Users\Dejan\Projekti\gatalinka\apps\android
$env:KEYSTORE_PASSWORD = "tvoja_lozinka_ovdje"
$env:KEY_PASSWORD = "tvoja_lozinka_ovdje"
```

**ILI** ako si koristio istu lozinku za oboje:
```powershell
$env:KEYSTORE_PASSWORD = "tvoja_lozinka"
$env:KEY_PASSWORD = "tvoja_lozinka"
```

---

## Korak 1: Build Release AAB

```powershell
cd C:\Users\Dejan\Projekti\gatalinka\apps\android
.\gradlew.bat bundleRelease
```

---

## Korak 2: Provjeri da je AAB kreiran

AAB će biti u:
```
apps/android/app/build/outputs/bundle/release/app-release.aab
```

Provjeri:
```powershell
Test-Path "app\build\outputs\bundle\release\app-release.aab"
```

Trebao bi vratiti `True` ✅

---

## Korak 3: Upload na Google Play Console

1. Idi na [Google Play Console](https://play.google.com/console)
2. Production → Create new release
3. Upload `app-release.aab`
4. Slijedi upute iz `GOOGLE_PLAY_CONSOLE_STEPS.md`

---

## ⚠️ Ako dobiješ grešku "Keystore password was incorrect"

Provjeri:
1. Da si postavio environment variables PRIJE build-a
2. Da su lozinke točne (iste kao u Android Studio)
3. Da si u istom PowerShell prozoru gdje si postavio environment variables

---

## Alternativa: Hardcode lozinke (NE PREPORUČENO)

Ako ne želiš koristiti environment variables, možeš direktno unijeti lozinke u `build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../gatalinka-release.jks")
        storePassword = "tvoja_lozinka"  // ⚠️ NE COMMITAJ OVO U GIT!
        keyAlias = "gatalinka"
        keyPassword = "tvoja_lozinka"    // ⚠️ NE COMMITAJ OVO U GIT!
    }
}
```

**ALI:** Provjeri da `.gitignore` već sadrži `build.gradle.kts` exclusions (ne bi trebao commitati lozinke).
