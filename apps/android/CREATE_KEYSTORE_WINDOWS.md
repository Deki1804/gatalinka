# Kreiranje Release Keystore na Windows-u

## Problem: `keytool` nije u PATH-u

`keytool` dolazi s JDK-om, ali nije automatski u PATH-u na Windows-u.

---

## Rješenje 1: Koristi Android Studio JDK (PREPORUČENO)

Android Studio dolazi s vlastitim JDK-om koji se koristi za build. Možemo koristiti taj.

### Korak 1: Pronađi Android Studio JDK

Android Studio JDK se obično nalazi u:
- `C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe`
- `C:\Users\[USERNAME]\AppData\Local\Android\Sdk\jbr\bin\keytool.exe`
- `C:\Program Files\JetBrains\Android Studio\jbr\bin\keytool.exe`

### Korak 2: Koristi puni put do keytool

```powershell
cd C:\Users\Dejan\Projekti\gatalinka\apps\android

# Probaj jedan od ovih putova:
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -genkey -v -keystore gatalinka-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias gatalinka

# ILI ako je na drugoj lokaciji:
& "$env:LOCALAPPDATA\Android\Sdk\jbr\bin\keytool.exe" -genkey -v -keystore gatalinka-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias gatalinka
```

---

## Rješenje 2: Koristi Android Studio GUI (NAJLAKŠE)

1. Otvori **Android Studio**
2. Idi u **Build** → **Generate Signed Bundle / APK**
3. Odaberi **Android App Bundle**
4. Klikni **Create new...** (za keystore)
5. Popuni formu:
   - **Key store path:** `C:\Users\Dejan\Projekti\gatalinka\apps\android\gatalinka-release.jks`
   - **Password:** (unesi jaku lozinku)
   - **Key alias:** `gatalinka`
   - **Key password:** (isti kao Password)
   - **Validity:** `10000` (dana)
   - **Certificate:** Popuni svoje podatke
6. Klikni **OK**
7. **NE NASTAVLJAJ** build - samo je keystore kreiran!

---

## Rješenje 3: Instaliraj JDK i dodaj u PATH

### Korak 1: Preuzmi JDK
- Idi na [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) ili
- [OpenJDK](https://adoptium.net/)

### Korak 2: Instaliraj JDK
- Instaliraj JDK (npr. u `C:\Program Files\Java\jdk-21\`)

### Korak 3: Dodaj u PATH
1. Otvori **System Properties** → **Environment Variables**
2. U **System variables**, pronađi **Path**
3. Klikni **Edit**
4. Klikni **New**
5. Dodaj: `C:\Program Files\Java\jdk-21\bin`
6. Klikni **OK** na svim prozorima
7. **Restartaj PowerShell**

### Korak 4: Provjeri
```powershell
keytool -version
```

Ako vidiš verziju → ✅ Radi!

---

## Rješenje 4: Koristi Gradle Task (ALTERNATIVA)

Možemo kreirati Gradle task koji koristi JDK iz Gradle build-a.

Dodaj u `apps/android/app/build.gradle.kts`:

```kotlin
tasks.register<Exec>("createReleaseKeystore") {
    description = "Creates release keystore"
    group = "release"
    
    val keystoreFile = file("../gatalinka-release.jks")
    
    commandLine(
        "keytool",
        "-genkey",
        "-v",
        "-keystore", keystoreFile.absolutePath,
        "-keyalg", "RSA",
        "-keysize", "2048",
        "-validity", "10000",
        "-alias", "gatalinka"
    )
    
    doFirst {
        if (keystoreFile.exists()) {
            throw GradleException("Keystore already exists! Delete it first if you want to recreate.")
        }
    }
}
```

Zatim pokreni:
```powershell
cd C:\Users\Dejan\Projekti\gatalinka\apps\android
.\gradlew.bat createReleaseKeystore
```

**Problem:** Ovo možda neće raditi ako Gradle ne može pronaći JDK.

---

## 🎯 PREPORUČENO: Rješenje 2 (Android Studio GUI)

**Najlakše i najsigurnije** - koristi Android Studio GUI jer:
- Ne trebaš tražiti JDK
- Ne trebaš dodavati u PATH
- Vidiš sve opcije vizualno
- Android Studio automatski koristi pravi JDK

---

## Nakon kreiranja keystore-a

1. Provjeri da je keystore kreiran:
   ```powershell
   Test-Path "C:\Users\Dejan\Projekti\gatalinka\apps\android\gatalinka-release.jks"
   ```
   Trebao bi vratiti `True`

2. **SPREMI LOZINKU U PASSWORD MANAGER!**

3. Nastavi s `RELEASE_KEYSTORE_SETUP.md` korak 3 (dodavanje signing config)
