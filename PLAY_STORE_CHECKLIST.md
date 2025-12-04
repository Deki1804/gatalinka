# Play Store Checklist - Gatalinka

## ✅ Korak 1: Priprema dokumentacije

### 1.1. Privacy Policy URL
- [ ] Kreiraj GitHub repository za legal dokumente (ili koristi postojeći)
- [ ] Upload `legal/privacy-policy.html` na GitHub
- [ ] Omogući GitHub Pages za repository
- [ ] URL će biti: `https://[tvoj-username].github.io/[repo-name]/privacy-policy.html`
- [ ] **Zapiši URL ovdje**: `___________________________`

### 1.2. Terms of Use URL
- [ ] Upload `legal/terms-of-use.html` na GitHub
- [ ] URL će biti: `https://[tvoj-username].github.io/[repo-name]/terms-of-use.html`
- [ ] **Zapiši URL ovdje**: `___________________________`

### 1.3. Ažuriraj kontakt e-mail
- [ ] Otvori `legal/PRIVACY_POLICY.md` i zamijeni `[Tvoj kontakt e-mail za Play Store]` s tvojim e-mailom
- [ ] Otvori `legal/TERMS_OF_USE.md` i zamijeni `[Tvoj kontakt e-mail za Play Store]` s tvojim e-mailom
- [ ] Otvori `legal/privacy-policy.html` i zamijeni `[Tvoj kontakt e-mail za Play Store]` s tvojim e-mailom
- [ ] Otvori `legal/terms-of-use.html` i zamijeni `[Tvoj kontakt e-mail za Play Store]` s tvojim e-mailom

---

## ✅ Korak 2: Play Console Setup

### 2.1. Kreiraj aplikaciju u Play Console
- [ ] Idi na [Google Play Console](https://play.google.com/console)
- [ ] Klikni "Create app"
- [ ] Unesi:
  - **App name**: Gatalinka
  - **Default language**: Croatian (Hrvatski)
  - **App or game**: App
  - **Free or paid**: Free
  - **Declarations**: Privacy Policy (obavezno), Terms of Service (opcionalno)

### 2.2. Store listing
- [ ] **Short description** (80 znakova):
  ```
  AI gatanje iz šalice kave - zabavno i mistično iskustvo za osobni uvid i refleksiju.
  ```
- [ ] **Full description** (4000 znakova):
  ```
  Gatalinka je aplikacija za zabavno gatanje iz šalice kave, gdje AI analizira talog i generira personalizirana tumačenja na temelju tvog horoskopskog znaka.
  
  ✨ ZNAČAJKE:
  • AI analiza taloga iz šalice kave
  • Personalizirana gatanja na temelju horoskopskog znaka
  • Različiti rituali čitanja (Instant, Duboko)
  • Povijest čitanja - spremi svoja gatanja
  • Gatanje za druge - unesi podatke prijatelja
  • Dnevna poruka i mantra
  • Škola čitanja - nauči više o simbolima
  
  🔮 KAKO RADI:
  1. Fotkaj svoju šalicu kave odozgo
  2. AI analizira talog i simbole
  3. Dobij personalizirano gatanje za ljubav, posao, novac i zdravlje
  4. Spremi čitanje i pregledavaj kasnije
  
  ⚠️ VAŽNO:
  Aplikacija je isključivo za zabavu. Ne donosi važne životne odluke na temelju rezultata.
  
  Za korisnike starije od 16 godina.
  ```
- [ ] **App icon**: Upload `apps/android/app/src/main/ic_launcher-playstore.png` (512x512px)
- [ ] **Feature graphic**: 1024x500px (opcionalno, ali preporučeno)
- [ ] **Screenshots**: 
  - Minimum 2 za telefon (16:9 ili 9:16)
  - Preporučeno: 4-8 screenshotova
  - Prikaži: Home screen, čitanje šalice, rezultate, profil

### 2.3. Privacy Policy
- [ ] U "Privacy Policy" polje unesi URL iz koraka 1.1
- [ ] Play Store će automatski validirati URL

### 2.4. Content rating
- [ ] Klikni "Start questionnaire"
- [ ] Odgovori na pitanja:
  - **Category**: Entertainment / Lifestyle
  - **Does your app contain user-generated content?**: Yes (slike šalica)
  - **Does your app allow users to communicate or share content?**: No
  - **Does your app contain ads?**: No
  - **Does your app allow in-app purchases?**: No
  - **Age group**: 16+ (zbog uvjeta korištenja)
- [ ] Dobit ćeš rating (vjerojatno "Everyone" ili "Teen")

---

## ✅ Korak 3: Data Safety (OBAVEZNO!)

### 3.1. Data collection
- [ ] Idi na "Data safety" sekciju
- [ ] Odgovori na pitanja:

**What data does your app collect or share?**
- [ ] **Personal info**:
  - Email address: ✅ Collected, Account management
  - Name: ✅ Collected (optional), App functionality
  - Date of birth: ✅ Collected, App functionality
  - Gender: ✅ Collected, App functionality
  
- [ ] **Photos and videos**:
  - Photos: ✅ Collected, App functionality (slike šalica za AI analizu)
  - **Important**: Označi "This data is not shared with third parties"
  - **Important**: Označi "This data is deleted when the user requests account deletion"

### 3.2. Data security
- [ ] **How is user data encrypted in transit?**: Data is encrypted in transit using HTTPS
- [ ] **Does your app allow users to request data deletion?**: Yes (u postavkama profila)

### 3.3. Data sharing
- [ ] **Does your app share data with third parties?**: 
  - Yes, but only with:
    - Google Firebase (za autentifikaciju i pohranu)
    - AI servis (za analizu slika)
  - **Purpose**: App functionality
  - **Data types**: Email, personal info, photos
  - **Is this data shared for advertising purposes?**: No
  - **Is this data shared for analytics purposes?**: No

---

## ✅ Korak 4: App content

### 4.1. Target audience
- [ ] **Primary target audience**: 16+ years
- [ ] **Content rating**: Prema rezultatu iz koraka 2.4

### 4.2. Ads
- [ ] **Does your app contain ads?**: No

### 4.3. In-app purchases
- [ ] **Does your app offer in-app purchases?**: No

### 4.4. Sensitive permissions
- [ ] **Camera**: Required for taking photos of coffee cup
- [ ] **Storage/Photos**: Required for selecting photos from gallery
- [ ] **Internet**: Required for Firebase and AI analysis

---

## ✅ Korak 5: Release

### 5.1. Create release
- [ ] Idi na "Production" → "Create new release"
- [ ] Upload AAB (Android App Bundle) fajl:
  ```bash
  ./gradlew bundleRelease
  ```
  - Fajl će biti u: `apps/android/app/build/outputs/bundle/release/app-release.aab`
- [ ] **Release name**: 1.0.0 (prva verzija)
- [ ] **Release notes** (Hrvatski):
  ```
  Prva verzija Gatalinka aplikacije!
  
  ✨ Značajke:
  • AI gatanje iz šalice kave
  • Personalizirana tumačenja
  • Povijest čitanja
  • Gatanje za druge
  • Dnevna poruka i mantra
  ```

### 5.2. Review and publish
- [ ] Provjeri sve informacije
- [ ] Klikni "Start rollout to Production"
- [ ] Play Store će pregledati aplikaciju (obično 1-3 dana)
- [ ] Dobit ćeš e-mail kada je aplikacija odobrena ili ako treba nešto popraviti

---

## 📝 Napomene

### Važno za prvu verziju:
1. **Privacy Policy URL mora biti dostupan** prije nego što pošalješ aplikaciju na review
2. **Data Safety forma je obavezna** - Play Store neće objaviti app bez nje
3. **Screenshots su obavezni** - minimum 2, preporučeno 4-8
4. **App icon mora biti 512x512px** - već imaš `ic_launcher-playstore.png`

### Ako dobiješ odbijanje:
- Play Store će ti poslati detaljne razloge
- Najčešći razlozi:
  - Privacy Policy URL nije dostupan
  - Data Safety forma nije ispunjena
  - Screenshots nedostaju
  - App icon nije ispravne veličine

### Support:
- Ako imaš pitanja, Play Store ima dobru dokumentaciju
- Također možeš kontaktirati Play Console support

---

**Sretno s objavom! 🚀**

