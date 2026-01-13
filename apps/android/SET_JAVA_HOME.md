# Postavljanje JAVA_HOME na Windows-u

## Problem: `JAVA_HOME is not set`

Gradle treba Java da bi buildao app. Trebamo postaviti JAVA_HOME na lokaciju Java instalacije.

---

## Rješenje 1: Koristi Android Studio JDK (PREPORUČENO)

Android Studio dolazi s vlastitim JDK-om. Pronađi ga i postavi JAVA_HOME.

### Korak 1: Pronađi Android Studio JDK

Android Studio JDK se obično nalazi u:
- `C:\Program Files\Android\Android Studio\jbr`
- `C:\Users\[USERNAME]\AppData\Local\Android\Sdk\jbr`
- `C:\Program Files\JetBrains\Android Studio\jbr`

### Korak 2: Postavi JAVA_HOME u PowerShell

```powershell
# Probaj jedan od ovih (zamijeni s pravom lokacijom):
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
# ILI
$env:JAVA_HOME = "$env:LOCALAPPDATA\Android\Sdk\jbr"
# ILI
$env:JAVA_HOME = "C:\Program Files\JetBrains\Android Studio\jbr"
```

### Korak 3: Provjeri da radi

```powershell
$env:JAVA_HOME
java -version
```

Ako vidiš Java verziju → ✅ Radi!

---

## Rješenje 2: Koristi Gradle Wrapper s Android Studio JDK

Umjesto postavljanja JAVA_HOME, možemo koristiti `gradle.properties` da Gradle koristi Android Studio JDK.

### Korak 1: Pronađi Android Studio JDK put

Provjeri gdje je Android Studio instaliran i pronađi `jbr` folder.

### Korak 2: Dodaj u `gradle.properties`

Dodaj u `apps/android/gradle.properties`:

```properties
org.gradle.java.home=C:\\Program Files\\Android\\Android Studio\\jbr
```

**ILI** ako je na drugoj lokaciji:
```properties
org.gradle.java.home=C:\\Users\\Dejan\\AppData\\Local\\Android\\Sdk\\jbr
```

**VAŽNO:** Koristi `\\` umjesto `\` u Windows putovima!

---

## Rješenje 3: Instaliraj JDK i postavi JAVA_HOME trajno

### Korak 1: Preuzmi JDK
- Idi na [Adoptium OpenJDK](https://adoptium.net/)
- Preuzmi JDK 17 ili 21 (LTS verzije)
- Instaliraj (npr. u `C:\Program Files\Eclipse Adoptium\jdk-21.0.1+12\`)

### Korak 2: Postavi JAVA_HOME trajno

1. Otvori **System Properties** → **Environment Variables**
2. U **System variables**, klikni **New**
3. **Variable name:** `JAVA_HOME`
4. **Variable value:** `C:\Program Files\Eclipse Adoptium\jdk-21.0.1+12` (ili gdje si instalirao)
5. Klikni **OK**

### Korak 3: Dodaj u PATH

1. U **System variables**, pronađi **Path**
2. Klikni **Edit**
3. Klikni **New**
4. Dodaj: `%JAVA_HOME%\bin`
5. Klikni **OK** na svim prozorima

### Korak 4: Restartaj PowerShell

Zatvori i ponovno otvori PowerShell, zatim provjeri:
```powershell
java -version
```

---

## Rješenje 4: Koristi Android Studio za Build (NAJLAKŠE)

Umjesto command line-a, možeš buildati direktno iz Android Studio:

1. Otvori projekt u Android Studio
2. Idi u **Build** → **Generate Signed Bundle / APK**
3. Odaberi **Android App Bundle**
4. Odaberi postojeći keystore (`gatalinka-release.jks`)
5. Unesi lozinke
6. Klikni **Next**
7. Odaberi **release** build variant
8. Klikni **Create**

AAB će biti u: `app/build/outputs/bundle/release/app-release.aab`

---

## 🎯 PREPORUČENO: Rješenje 4 (Android Studio GUI)

**Najlakše** - Android Studio automatski koristi pravi JDK i ne trebaš postavljati JAVA_HOME.

---

## Provjera nakon postavljanja

```powershell
cd C:\Users\Dejan\Projekti\gatalinka\apps\android
$env:KEYSTORE_PASSWORD = "LaRryDJ123"
$env:KEY_PASSWORD = "LaRryDJ123"
.\gradlew.bat bundleRelease
```

Ako sve prođe bez greške → ✅ Spremno!
