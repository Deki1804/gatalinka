# Kompletan vodič za Google Sign In setup

## Korak 1: Dodaj SHA-1 Fingerprint ✅

1. Idite na Firebase Console → **Project Settings** (zupčanik)
2. Scroll do **Your apps** sekcije
3. Kliknite na Android app (`com.gatalinka.app`)
4. Scroll do **SHA certificate fingerprints**
5. Kliknite **Add fingerprint**
6. Zalijepi: `AB:76:ED:3E:0C:48:E3:DA:CA:17:E1:97:B9:10:C9:98:85:FF:CB:73`
7. Klikni **Save**

## Korak 2: Dobij Web Client ID

### Opcija A: Ako već postoji Web aplikacija

1. Idi na **Authentication** → **Sign-in method** → **Google**
2. U **Web SDK configuration** sekciji, traži **Web client ID**
3. Kopiraj taj ID (izgleda kao: `xxxxx-xxxxx.apps.googleusercontent.com`)

### Opcija B: Ako NEMA Web aplikacije (kreiraj je)

1. Idi na **Project Settings** → **Your apps**
2. Klikni **Add app** → **Web** (ikonica `</>`)
3. Unesi app nickname: "Gatalinka Web" (ili bilo što)
4. **NEMOJ** čekirati "Also set up Firebase Hosting"
5. Klikni **Register app**
6. **NEMOJ** kopirati config kod, samo zatvori taj modal
7. Sada idi na **Authentication** → **Sign-in method** → **Google**
8. U **Web SDK configuration** sekciji ćeš vidjeti **Web client ID**
9. Kopiraj taj ID

## Korak 3: Dodaj Web Client ID u aplikaciju

1. Otvori `apps/android/app/src/main/res/values/strings.xml`
2. Nađi liniju:
   ```xml
   <string name="default_web_client_id" translatable="false">YOUR_WEB_CLIENT_ID_HERE</string>
   ```
3. Zamijeni `YOUR_WEB_CLIENT_ID_HERE` sa pravim Web Client ID-om (koji si kopirao)

Primjer:
```xml
<string name="default_web_client_id" translatable="false">123456789-abcdefghijklmnop.apps.googleusercontent.com</string>
```

## Korak 4: Testiraj

1. Pokreni aplikaciju
2. Idi na Login ekran
3. Klikni "Nastavi sa Google"
4. Trebao bi se otvoriti Google Sign In dialog
5. Odaberi Google account
6. Trebao bi se automatski prijaviti

## Troubleshooting

### "Google Sign In nije konfiguriran" error
- Provjeri da li si dodao Web Client ID u `strings.xml`
- Provjeri da li je SHA-1 fingerprint dodan u Firebase Console

### "DEVELOPER_ERROR" ili "10:" error
- Provjeri da li je SHA-1 fingerprint točno dodan
- Može potrajati nekoliko minuta dok se promjene propagiraju
- Pokušaj restartati aplikaciju

### Web Client ID nije vidljiv
- Kreiraj Web aplikaciju u Firebase (Opcija B iznad)
- Može potrajati nekoliko minuta dok se pojavi

## Napomene

- **Web aplikacija** u Firebase nije obavezna za Android app, ali je potrebna za dobivanje Web Client ID-a
- **SHA-1 fingerprint** mora biti točan - provjeri svaki znak
- Ako testiraš na **fizičkom uređaju**, trebat će ti SHA-1 od debug keystore-a (što već imamo)
- Za **production release**, trebat će ti i SHA-1 od release keystore-a (dodat ćemo kasnije)

