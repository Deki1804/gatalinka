# 📱 Instrukcije za Android Studio

## 1. Instalacija nove verzije aplikacije

### Što znači "nova verzija aplikacije"?
Kada mijenjamo kod u Android aplikaciji, moramo **ponovno izgraditi (build)** i **instalirati** aplikaciju na telefon/emulator. To je "nova verzija".

### Koraci za instalaciju:

#### Opcija A: Preko Android Studio (preporučeno)
1. **Otvorite projekt u Android Studio**
   - Otvorite folder `F:\Projekti\Gatalinka\apps\android` u Android Studio

2. **Povežite telefon ili pokrenite emulator**
   - Spojite telefon USB kablom i omogućite USB debugging
   - ILI pokrenite Android emulator iz Android Studio

3. **Izgradite i instalirajte aplikaciju**
   - Kliknite na zeleni **▶️ Run** gumb (Run 'app') u gornjem desnom kutu
   - ILI pritisnite `Shift + F10` (Windows) ili `Ctrl + R` (Mac)
   - Android Studio će automatski:
     - Izgraditi aplikaciju (build)
     - Instalirati je na telefon/emulator
     - Pokrenuti aplikaciju

4. **Ako aplikacija već postoji na telefonu**
   - Android Studio će automatski zamijeniti staru verziju novom
   - Ne morate ništa brisati

#### Opcija B: Preko terminala (PowerShell)
```powershell
# Navigirajte u android folder
cd F:\Projekti\Gatalinka\apps\android

# Instalirajte aplikaciju (build + install)
.\gradlew.bat installDebug
```

---

## 2. Filtriranje Logcata u Android Studio

### Problem:
Logcat prikazuje **milijune linija** - sve logove sa sistema, drugih aplikacija, itd.

### Rješenje: Filtrirajte logove

#### Korak 1: Otvorite Logcat
- U Android Studio, kliknite na tab **"Logcat"** na dnu ekrana
- Ako ne vidite Logcat, idite na: `View` → `Tool Windows` → `Logcat`

#### Korak 2: Postavite filter po paketu (package)
1. U Logcat prozoru, pronađite polje **"Filter"** (lijevo gore)
2. Unesite:
   ```
   package:com.gatalinka.app
   ```
3. Pritisnite Enter

**Rezultat:** Vidjet ćete samo logove iz Gatalinka aplikacije!

#### Korak 3: Dodatno filtriranje po log levelu
U filter polju možete kombinirati:
```
package:com.gatalinka.app level:DEBUG
```
Ovo prikazuje samo DEBUG logove iz vaše aplikacije.

**Dostupni log leveli:**
- `VERBOSE` - sve (najviše)
- `DEBUG` - debug poruke (preporučeno)
- `INFO` - informacije
- `WARN` - upozorenja
- `ERROR` - greške

#### Korak 4: Filtriranje po tagu (najbolje za naš slučaj!)
Umjesto paketa, možete filtrirati po **tagu** (ime loga):

```
tag:ReadingViewModel
```

**Najkorisniji tagovi za naš problem:**
- `tag:ReadingViewModel` - svi logovi iz ViewModela
- `tag:ReadCupScreen` - logovi iz ReadCupScreen
- `tag:ReadingResultScreen` - logovi iz ReadingResultScreen
- `tag:FirebaseFunctionsService` - logovi iz Firebase servisa

**Kombinacija više tagova:**
```
tag:ReadingViewModel | tag:ReadCupScreen | tag:ReadingResultScreen
```

#### Korak 5: Spremite filter za budućnost
1. Kliknite na ikonu **"+"** pored filter polja
2. Unesite ime filtera (npr. "Gatalinka Debug")
3. Unesite filter: `package:com.gatalinka.app level:DEBUG`
4. Kliknite "OK"
5. Sada možete odabrati ovaj filter iz padajuće liste

---

## 3. Provjera da li nova verzija radi

### Nakon instalacije:
1. **Pokrenite aplikaciju** na telefonu/emulatoru
2. **Otvorite Logcat** u Android Studio
3. **Postavite filter:** `package:com.gatalinka.app level:DEBUG`
4. **Pokušajte čitati šalicu** u aplikaciji
5. **Provjerite logove** - trebali biste vidjeti:
   - `ReadCupScreen: LaunchedEffect triggered...`
   - `ReadingViewModel: startReading called...`
   - `ReadingViewModel: State set to Loading`
   - `FirebaseFunctionsService: Function call successful...`
   - `ReadingViewModel: State updated to Success`
   - `ReadCupScreen: Success state observed!`

### Ako ne vidite logove:
- Provjerite da li je filter ispravno postavljen
- Provjerite da li je aplikacija pokrenuta
- Provjerite da li je telefon/emulator povezan
- Pokušajte restartirati Logcat (ikonica refresh)

---

## 4. Brzi savjeti

### Ukloni sve logove i počni ispočetka:
U Logcat prozoru, kliknite na ikonu **"Clear"** (čistilo) da obrišete sve stare logove.

### Automatski scroll:
Provjerite da li je **"Auto Scroll"** uključen (ikonica sa strelicom prema dolje) - automatski će prikazivati nove logove.

### Export logova:
Ako želite spremiti logove u datoteku:
1. Desni klik u Logcat prozoru
2. Odaberite "Export to Text File"
3. Spremite datoteku

---

## 5. Najbolji filter za naš problem

**Preporučeni filter:**
```
package:com.gatalinka.app level:DEBUG | level:ERROR | level:WARN
```

Ili još bolje, samo naši tagovi:
```
tag:ReadCupScreen | tag:ReadingViewModel | tag:ReadingResultScreen | tag:FirebaseFunctionsService
```

---

## ✅ Provjera lista

- [ ] Aplikacija je instalirana na telefon/emulator
- [ ] Logcat je otvoren u Android Studio
- [ ] Filter je postavljen na `package:com.gatalinka.app`
- [ ] Auto Scroll je uključen
- [ ] Aplikacija je pokrenuta
- [ ] Pokušali ste čitati šalicu
- [ ] Vidite logove u Logcatu

---

**Ako i dalje imate problema, pošaljite mi screenshot Logcata sa filterom!**

