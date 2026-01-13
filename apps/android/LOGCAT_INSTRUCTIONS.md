# Kako koristiti Logcat filter u Android Studio

## Brzi način - Text Filter

1. Otvori **Logcat** u Android Studio (dolje u prozoru)
2. U **filter polju** (gore lijevo u Logcat prozoru) unesi:
   ```
   Google Sign-In | API_CALL | AuthViewModel | GoogleSignInHelper | LoginScreen
   ```
3. Pritisni Enter

## Napredni način - Tag Filter

1. Otvori **Logcat** u Android Studio
2. Klikni na **ikonu filtera** (lijevo od search polja) ili pritisni `Ctrl+F` / `Cmd+F`
3. U **Log Tag** polju unesi:
   ```
   LoginScreen|GoogleSignInHelper|AuthViewModel|API_CALL|FirebaseFunctionsService
   ```
4. Klikni **OK**

## Što ćeš vidjeti

### Google Sign-In logovi:
- `🔍 Google Sign-In button clicked` - kada klikneš na gumb
- `🔍 Google Sign-In: Web Client ID = ...` - koji Web Client ID se koristi
- `✅ Pokretanje Google Sign-In flow-a...` - flow se pokreće
- `✅ Launching Google Sign-In intent...` - intent se šalje
- `🔍 Google Sign-In launcher callback triggered` - callback se poziva
- `🔍 getSignInResult returned: ...` - rezultat parsiranja
- `✅ Calling signInWithGoogleIdToken` - poziv Firebase autentifikacije
- `✅ Uspješna Google prijava: ...` - uspješna prijava
- `❌ Greška pri Google prijavi: ...` - greška

### API pozivi:
- `🔥 API_CALL === readCup CALL #... ===` - poziv za čitanje šalice
- `🔥 API_CALL === getDailyReading CALL #... ===` - poziv za dnevno čitanje
- Stack trace koji pokazuje gdje se poziva

## Troubleshooting

### Ako ne vidiš logove:
1. Provjeri da je app u **DEBUG** modu (ne Release)
2. Provjeri da je **Log Level** postavljen na **Verbose** ili **Debug**
3. Provjeri da je **Show only selected application** isključeno
4. Pokušaj sa **text filterom** umjesto tag filtera

### Ako vidiš previše logova:
- Koristi specifičniji filter:
  ```
  Google Sign-In | API_CALL
  ```

