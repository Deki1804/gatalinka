# Gatalinka - Release Checklist za Google Play Store

## ✅ Pre-Release Provjere

### 1. Build Konfiguracija
- [x] `versionCode = 1` (prvi release)
- [x] `versionName = "1.0.0"` (prvi release)
- [x] `minSdk = 24` (Android 7.0+)
- [x] `targetSdk = 36` (najnoviji)
- [x] `isMinifyEnabled = true` (release build)
- [x] `isShrinkResources = true` (optimizacija)

### 2. Signing Configuration
- [ ] **KREIRATI RELEASE KEYSTORE** (vidi `RELEASE_KEYSTORE_SETUP.md`)
- [ ] Dodati signing config u `build.gradle.kts`
- [ ] Testirati release build lokalno

### 3. Kod Provjera
- [x] Debug logovi zaštićeni s `BuildConfig.DEBUG` ✅
- [x] TODO komentari su samo feature placeholders (OK) ✅
- [x] Nema hardkodiranih tajni ✅
- [x] ProGuard rules konfigurirani ✅

### 4. Manifest Provjera
- [x] `android:usesCleartextTraffic="false"` ✅
- [x] `android:allowBackup="false"` ✅
- [x] Permissions su minimalne i opravdane ✅
- [x] MainActivity `exported="true"` ✅
- [x] FileProvider `exported="false"` ✅

### 5. Assets
- [x] Launcher ikone u svim veličinama ✅
- [x] Privacy Policy link radi ✅
- [x] Terms of Use link radi ✅

### 6. Firebase
- [x] Crashlytics integriran ✅
- [x] Analytics integriran ✅
- [x] google-services.json postavljen ✅

---

## 📦 Build Release APK/AAB

### Korak 1: Kreiraj Release Keystore
```bash
cd apps/android
keytool -genkey -v -keystore gatalinka-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias gatalinka
```

**VAŽNO:** 
- Spremi keystore na sigurno mjesto
- Spremi lozinku u password manager
- **NE COMMITAJ** keystore u git!

### Korak 2: Dodaj Signing Config u build.gradle.kts
Dodaj u `android { }` blok:
```kotlin
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
        // ... postojeća konfiguracija
    }
}
```

### Korak 3: Build Release AAB
```bash
cd apps/android
./gradlew bundleRelease
```

AAB će biti u: `app/build/outputs/bundle/release/app-release.aab`

---

## 🚀 Google Play Console Upload

### Korak 1: Priprema
1. Idi na [Google Play Console](https://play.google.com/console)
2. Odaberi app "Gatalinka" (ili kreiraj novi)
3. Idi u **Production** → **Create new release**

### Korak 2: Upload AAB
1. Klikni **Upload** i odaberi `app-release.aab`
2. Čekaj da se procesira (može potrajati 10-30 min)

### Korak 3: Release Notes
Dodaj release notes (hrvatski):
```
Prva verzija aplikacije Gatalinka!

✨ Funkcionalnosti:
- Gatanje iz šalice kave s AI analizom
- Dnevna poruka i simboli
- Gatanje za druge
- Povijest čitanja
- Google Sign-In prijava

🔮 Otkrij svoju sudbinu u šalici kafe!
```

### Korak 4: Content Rating
- Odgovori na pitanja o sadržaju
- Preporuka: **3+** (za sve)

### Korak 5: Privacy Policy
- URL: `https://deki1804.github.io/gatalinka/privacy-policy.html`
- Provjeri da link radi!

### Korak 6: App Content
- **App access:** Odgovori na pitanja
- **Data safety:** 
  - Email adresa (kolekcija, autentifikacija)
  - Datum rođenja (kolekcija, personalizacija)
  - Spol (kolekcija, personalizacija)
  - Slike (privremeno, AI analiza)

### Korak 7: Store Listing
- **App name:** Gatalinka
- **Short description:** Otkrivaj sudbinu u šalici kafe
- **Full description:** (detaljni opis)
- **Screenshots:** (dodaj minimalno 2)
- **Icon:** (512x512px)
- **Feature graphic:** (1024x500px)

### Korak 8: Closed Testing
1. Idi u **Testing** → **Closed testing**
2. Klikni **Create new release**
3. Upload isti AAB
4. Dodaj testere (email adrese)
5. Klikni **Review release** → **Start rollout to Closed testing**

---

## ⚠️ VAŽNE NAPOMENE

1. **Keystore je KRITIČAN** - bez njega ne možeš update-ati app!
2. **VersionCode mora rasti** - svaki novi release mora imati veći versionCode
3. **Testiraj release build** prije uploada
4. **Privacy Policy mora biti javno dostupan** prije releasea
5. **Screenshots su obavezni** za Store listing

---

## 📝 Nakon Releasea

- [ ] Prati Crashlytics za crashove
- [ ] Prati Analytics za korisničke metrike
- [ ] Pripremi feedback od testera
- [ ] Planiraj sljedeći release (versionCode = 2)

---

## 🔗 Korisni Linkovi

- [Google Play Console](https://play.google.com/console)
- [Privacy Policy](https://deki1804.github.io/gatalinka/privacy-policy.html)
- [Terms of Use](https://deki1804.github.io/gatalinka/terms-of-use.html)
- [Firebase Console](https://console.firebase.google.com/project/gatalinka-230f9)
