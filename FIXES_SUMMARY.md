# 🔧 Popravke - Back Navigation i Personalizacija

## Problem 1: Back Navigation
**Problem:** Kada završiš čitanje i pritisneš back, vraća te na ekran gdje se čita umjesto na Home.

**Rješenje:** 
- Zamijenjeno `popBackStack(Routes.Home, inclusive = false)` s `popUpTo(0) { inclusive = true }`
- Dodano `while (nav.previousBackStackEntry != null) { nav.popBackStack() }` prije navigacije da se osigura da se sve očisti

**Lokacija:** `AppNavHost.kt` - sve 3 instance (ReadCupScreen onBack, ReadingResultScreen onBack, ReadingResultScreen onSave)

## Problem 2: Personalizacija za Gatanje za Druge
**Problem:** Kada gataš za gosta (npr. Lav), i dalje piše o Ovanu umjesto o Lavu.

**Rješenje:**
1. **ReadingForOthersViewModel.kt** - Popravljeno da se `customUserInput` pravilno postavlja kada se pozove `updateGender` ili `updateBirthdate`
2. **ReadCupScreen.kt** - Dodana provjera da se `customUserInput` koristi samo ako je potpuno postavljen (ima `zodiacSign`, `gender != Unspecified`, i `birthdate.isNotEmpty()`)
3. Dodano detaljno debug logiranje da se vidi što se šalje

**VAŽNO:** Provjeri u logcat-u (ako je DEBUG build) što se šalje:
```
ReadCupScreen: === DEBUG: Custom UserInput ===
ReadCupScreen: customUserInput.zodiacSign: Lav
ReadCupScreen: customUserInput.gender: Female
ReadCupScreen: Using userInput: zodiac=Lav, gender=Female
```

## Što provjeriti:

1. **U ReadingForOthersScreen** - kada uneseš datum i spol, provjeri da se `customUserInput` postavlja:
   - Klikni na "Fotkaj šalicu" 
   - Provjeri u logcat-u da li se `customUserInput` postavlja

2. **U ReadCupScreen** - provjeri u logcat-u da li se koristi `customUserInput` ili tvoj vlastiti `userInput`

3. **U FirebaseFunctionsService** - provjeri u logcat-u što se šalje u Cloud Function:
   ```
   FirebaseFunctionsService: Sending to API: zodiac=Lav, gender=Female, mode=instant
   ```

## Ako i dalje ne radi:

1. **Provjeri Cloud Function** - možda backend ne koristi `zodiacSign` i `gender` parametre pravilno
2. **Provjeri logcat** - u debug build-u ćeš vidjeti sve što se šalje
3. **Provjeri da li se `customUserInput` postavlja** - možda problem je u `ReadingForOthersScreen` gdje se pozivaju `updateBirthdate` i `updateGender`

## Napomena o Cloud Functions:

**JA NISAM DEPLOY-ao Cloud Functions** - to moraš ti napraviti na Firebase konzoli. Provjeri da li tvoja Cloud Function (`readCupCallable`) pravilno koristi `zodiacSign` i `gender` parametre iz `data` objekta.


