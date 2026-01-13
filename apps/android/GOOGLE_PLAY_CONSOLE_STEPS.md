# Google Play Console - Korak po Korak Vodič

## 📋 Pre-Upload Checklist

- [ ] Release keystore kreiran (`gatalinka-release.jks`)
- [ ] Signing config dodan u `build.gradle.kts`
- [ ] Release AAB buildan (`app-release.aab`)
- [ ] Privacy Policy link radi: https://deki1804.github.io/gatalinka/privacy-policy.html
- [ ] Terms of Use link radi: https://deki1804.github.io/gatalinka/terms-of-use.html

---

## 🚀 Korak 1: Priprema Google Play Console

1. Idi na [Google Play Console](https://play.google.com/console)
2. Prijavi se sa svojim Google računom
3. Ako nemaš app, klikni **Create app**
   - **App name:** Gatalinka
   - **Default language:** Croatian (Hrvatski)
   - **App or game:** App
   - **Free or paid:** Free
   - Klikni **Create**

---

## 📦 Korak 2: Upload AAB (Production)

1. U lijevom meniju, klikni **Production**
2. Klikni **Create new release**
3. U sekciji **App bundles**, klikni **Upload**
4. Odaberi `app-release.aab` (iz `apps/android/app/build/outputs/bundle/release/`)
5. Čekaj da se upload završi (može potrajati 10-30 minuta)

---

## 📝 Korak 3: Release Notes

U polje **Release notes**, unesi:

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

---

## ✅ Korak 4: Review Release

1. Klikni **Review release** (dolje desno)
2. Provjeri sve informacije
3. **NE KLIKNI "Start rollout to production" JOS!**

**Zašto?** Prvo moraš popuniti sve obavezne informacije!

---

## 🔒 Korak 5: App Content

1. U lijevom meniju, klikni **Policy** → **App content**
2. Odgovori na sva pitanja:

### Age-based restrictions
- **Target age group:** 13+ (ili 16+)
- **Content rating:** Odgovori na pitanja o sadržaju

### Privacy Policy
- **Privacy Policy URL:** `https://deki1804.github.io/gatalinka/privacy-policy.html`
- Provjeri da link radi!

### Data Safety
- **Does your app collect or share any of the required user data types?**
  - Odaberi **Yes**
- **Data types:**
  - ✅ **Email address** (kolekcija, autentifikacija)
  - ✅ **Date of birth** (kolekcija, personalizacija)
  - ✅ **Gender** (kolekcija, personalizacija)
  - ✅ **Photos** (privremeno, AI analiza)
- Za svaki data type:
  - **Purpose:** Odaberi odgovarajuće (autentifikacija, personalizacija, itd.)
  - **Data collection:** Yes
  - **Data sharing:** No (ne dijeliš s trećim stranama)

---

## 🖼️ Korak 6: Store Listing

1. U lijevom meniju, klikni **Store presence** → **Main store listing**

### Obavezno popuni:

- **App name:** Gatalinka
- **Short description (80 znakova):**
  ```
  Otkrivaj sudbinu u šalici kafe s AI gatanjem
  ```
- **Full description (4000 znakova):**
  ```
  Gatalinka je aplikacija za gatanje iz šalice kave koja koristi AI tehnologiju za analizu taloga i simbolike.

  ✨ Funkcionalnosti:
  • Gatanje iz šalice - Fotografiraj svoju šalicu kave i dobit ćeš detaljnu analizu taloga
  • Dnevna poruka - Dobij dnevne savjete i simbolike bez fotografije
  • Gatanje za druge - Gataj za prijatelje i obitelj
  • Povijest čitanja - Pregledaj sva svoja prethodna čitanja
  • Personalizacija - Rezultati prilagođeni tvom znaku zodijaka i spolu

  🔮 Kako radi:
  1. Fotografiraj svoju šalicu kave s talogom
  2. AI analizira simboliku i talog
  3. Dobij detaljno čitanje s interpretacijom za ljubav, posao, novac i zdravlje

  ⚠️ Važno: Aplikacija je isključivo zabavnog karaktera i ne predstavlja profesionalni savjet.
  ```

### Screenshots (OBVEZNO!)

- **Phone:** Minimalno 2, preporučeno 4-8
  - Rezolucija: 320dp - 3840dp (širina)
  - Format: PNG ili JPEG
  - Stvori screenshot-e:
    1. Home screen
    2. Onboarding screen
    3. Camera/Image picker
    4. Reading result screen
    5. Daily reading screen

- **Tablet (opcijski):** Ako imaš tablet verziju

### Graphics

- **App icon (512x512px):** PNG, bez alpha kanala
- **Feature graphic (1024x500px):** PNG ili JPEG
  - Promo banner za Store listing

---

## 🧪 Korak 7: Closed Testing Setup

1. U lijevom meniju, klikni **Testing** → **Closed testing**
2. Klikni **Create new release**
3. Upload isti `app-release.aab`
4. Dodaj release notes (isti kao za Production)
5. Klikni **Review release**

### Dodaj Testere

1. U **Testers** sekciji, klikni **Create email list**
2. Unesi email adrese testera (jedan po liniju):
   ```
   tester1@example.com
   tester2@example.com
   ```
3. Klikni **Save**
4. Klikni **Start rollout to Closed testing**

---

## ✅ Korak 8: Final Review

Provjeri da si popunio:

- [ ] App bundle uploadan
- [ ] Release notes dodani
- [ ] Privacy Policy URL postavljen i radi
- [ ] Data Safety popunjen
- [ ] Store listing popunjen (name, description)
- [ ] Screenshots dodani (minimalno 2)
- [ ] App icon dodan (512x512px)
- [ ] Feature graphic dodan (1024x500px)
- [ ] Closed testing release kreiran
- [ ] Testeri dodani

---

## 🎯 Korak 9: Start Rollout

1. Idi u **Testing** → **Closed testing**
2. Provjeri da je release status **Ready to rollout**
3. Klikni **Start rollout to Closed testing**
4. Čekaj da se procesira (može potrajati nekoliko sati)

---

## 📧 Korak 10: Invite Testere

1. U **Testers** sekciji, klikni **Get link**
2. Kopiraj link
3. Pošalji link testerima
4. Testeri će moći preuzeti app preko linka

---

## ⚠️ VAŽNE NAPOMENE

1. **Review proces:** Google može tražiti 1-7 dana za review
2. **Ako imaš greške:** Google će ti poslati email s detaljima
3. **Update verzije:** Svaki novi release mora imati veći `versionCode`
4. **Keystore:** **NE IZGUBI** keystore - bez njega ne možeš update-ati app!

---

## 🔗 Korisni Linkovi

- [Google Play Console](https://play.google.com/console)
- [Privacy Policy](https://deki1804.github.io/gatalinka/privacy-policy.html)
- [Terms of Use](https://deki1804.github.io/gatalinka/terms-of-use.html)
- [Firebase Console](https://console.firebase.google.com/project/gatalinka-230f9)

---

## 📞 Support

Ako imaš problema:
1. Provjeri [Google Play Console Help](https://support.google.com/googleplay/android-developer)
2. Provjeri email za greške od Google-a
3. Provjeri da su svi linkovi (Privacy Policy, Terms) javno dostupni
