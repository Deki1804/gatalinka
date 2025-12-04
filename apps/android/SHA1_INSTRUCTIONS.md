# Upute za dobivanje SHA-1 fingerprinta

SHA-1 fingerprint je potreban za Google Sign In funkcionalnost u Firebase.

## Tvoj SHA-1 hash (već pronađen):
```
AB:76:ED:3E:0C:48:E3:DA:CA:17:E1:97:B9:10:C9:98:85:FF:CB:73
```

## Windows (PowerShell)

### Automatski (preporučeno):
1. Otvori PowerShell u `apps/android` direktorijumu
2. Pokreni: `.\get-sha1.ps1`
3. Kopiraj SHA-1 hash koji se prikaže

### Ručno:
Otvori PowerShell ili Command Prompt i pokreni:
```powershell
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

## Linux/Mac

### Automatski:
1. Otvori Terminal u `apps/android` direktorijumu
2. Daj dozvole: `chmod +x get-sha1.sh`
3. Pokreni: `./get-sha1.sh`
4. Kopiraj SHA-1 hash

### Ručno:
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

## Dodavanje u Firebase Console

1. Idite na [Firebase Console](https://console.firebase.google.com/)
2. Odaberite projekat **gatalinka-230f9**
3. Kliknite na zupčanik (⚙️) → **Project settings**
4. Scroll do **Your apps** sekcije
5. Kliknite na Android app (`com.gatalinka.app`)
6. Scroll do **SHA certificate fingerprints**
7. Kliknite **Add fingerprint**
8. Zalijepi SHA-1 hash (bez `SHA1:` prefiksa, samo hex vrijednost)
9. Klikni **Save**

## Primjer SHA-1 formata

Trebao bi izgledati ovako:
```
A1:B2:C3:D4:E5:F6:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12
```

**Napomena:** Ako nemaš debug keystore još, pokreni aplikaciju jednom u Android Studio-u i on će automatski kreirati keystore.

## Za production build

Kada budeš spreman za release, trebat će ti i SHA-1 od release keystore-a. To ćemo dodati kasnije.

