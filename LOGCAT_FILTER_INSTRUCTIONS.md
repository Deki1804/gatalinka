# Kako filtrirati logove u Android Studio

## Brzi način (preporučeno):

1. **Otvori Logcat** (dno ekrana u Android Studio)
2. **U filter polju upiši:**
   ```
   package:com.gatalinka.app
   ```
3. **Ili po tagovima:**
   ```
   tag:ReadingViewModel | tag:FirebaseFunctionsService | tag:ReadCupScreen | tag:ReadingResultScreen
   ```

## Detaljniji filter:

1. **Klikni na ikonu filtera** (🔍) u Logcat toolbaru
2. **Kreiraj novi filter:**
   - **Filter Name:** `Gatalinka Debug`
   - **Log Tag:** `ReadingViewModel|FirebaseFunctionsService|ReadCupScreen|ReadingResultScreen`
   - **Log Level:** `Debug` ili `Verbose`
   - **Package Name:** `com.gatalinka.app`
3. **Spremi filter** i odaberi ga iz dropdowna

## Još jednostavnije - samo naši tagovi:

U filter polju upiši:
```
ReadingViewModel:D FirebaseFunctionsService:D ReadCupScreen:D ReadingResultScreen:D
```

## Testiranje:

1. Pokreni aplikaciju (Run ▶ ili Debug 🐛)
2. Odaberi sliku šalice
3. Promatraj logove - trebale bi se prikazivati samo naše poruke

