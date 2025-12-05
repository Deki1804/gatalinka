# Firebase Authentication Setup

## Problem: "This operation is not allowed" greška

Ako vidite grešku **"This operation is not allowed. This may be because the given sign-in provider is disabled for this Firebase project"**, to znači da sign-in metode nisu omogućene u Firebase konzoli.

## Rješenje: Omogući sign-in metode u Firebase konzoli

### Korak 1: Otvori Firebase Console
1. Idi na [Firebase Console](https://console.firebase.google.com/)
2. Odaberi projekt **gatalinka-230f9**

### Korak 2: Omogući Email/Password sign-in
1. U lijevom meniju klikni na **Authentication** (Autentifikacija)
2. Klikni na tab **Sign-in method** (Metode prijave)
3. Pronađi **Email/Password** u listi
4. Klikni na **Email/Password**
5. Uključi **Enable** (Omogući)
6. Klikni **Save** (Spremi)

### Korak 3: Omogući Google sign-in
1. U istom tabu **Sign-in method**, pronađi **Google**
2. Klikni na **Google**
3. Uključi **Enable** (Omogući)
4. **Project support email** - ovo je email koji se koristi za Google OAuth konfiguraciju
   - Možeš koristiti svoj email (npr. `LarryDJ@gmail.com`)
   - Ili email povezan s Firebase projektom
   - Ovo je samo za Google OAuth konfiguraciju, ne utječe na korisnike
5. Klikni **Save** (Spremi)

**VAŽNO:** Nakon što omogućiš Google sign-in, moraš dodati **SHA-1 certificate fingerprint** za Android app!

### Korak 4: Dodaj SHA-1 Certificate Fingerprint (OBVEZNO za Google sign-in!)

**Ovo je kritično!** Bez SHA-1 fingerprinta, Google sign-in neće raditi na Androidu.

1. Idi u **Project Settings** (⚙️ ikona u gornjem lijevom kutu)
2. Scrollaj do **Your apps** sekcije
3. Pronađi Android app (`com.gatalinka.app`)
4. Klikni na **Add fingerprint** (ili **Add SHA certificate fingerprint**)
5. Dodaj **release SHA-1 fingerprint**:
   ```
   D6:21:19:22:2C:47:3B:0A:27:BB:65:C9:44:19:5A:C5:86:C1:4A:86
   ```
6. Također dodaj **debug SHA-1** (za testiranje):
   - Debug keystore se obično nalazi u `~/.android/debug.keystore`
   - Možeš dobiti debug SHA-1 sa: `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
7. Klikni **Save**

**Napomena:** Nakon dodavanja SHA-1, može potrajati nekoliko minuta dok Firebase ažurira konfiguraciju. Preporučujem da sačekaš 5-10 minuta prije testiranja.

### Korak 5: Provjeri Web Client ID
Provjeri da je **Web Client ID** ispravno postavljen u aplikaciji:
- Datoteka: `apps/android/app/src/main/res/values/strings.xml`
- Ključ: `default_web_client_id`
- Trebao bi biti: `253930174034-0vqk1021d09n0rbnkgtjvhvthqd5tcnq.apps.googleusercontent.com`

## Testiranje

Nakon što omogućiš sign-in metode:
1. Restartaj aplikaciju
2. Pokušaj se prijaviti sa email/password
3. Pokušaj se prijaviti sa Google

## Troubleshooting

### Ako Google sign-in i dalje ne radi:
1. **Provjeri SHA-1 fingerprint** (najčešći problem!)
   - Idi u **Project Settings** > **Your apps** > Android app
   - Provjeri da je SHA-1 dodan: `D6:21:19:22:2C:47:3B:0A:27:BB:65:C9:44:19:5A:C5:86:C1:4A:86`
   - Ako nije dodan, dodaj ga i sačekaj 5-10 minuta
   - **VAŽNO:** Ako koristiš debug build, dodaj i debug SHA-1!
2. Provjeri da je `google-services.json` ažuran
   - Nakon dodavanja SHA-1, možda trebaš re-downloadati `google-services.json` iz Firebase Console
3. Provjeri da je `default_web_client_id` ispravan u `strings.xml`
4. Provjeri da je **Project support email** postavljen u Google sign-in konfiguraciji
5. Restartaj aplikaciju nakon promjena

### Ako Email/Password i dalje ne radi:
1. Provjeri da je Email/Password **Enable** u Firebase Console
2. Provjeri da korisnik postoji u Firebase Authentication (ako se pokušava prijaviti)
3. Provjeri da je lozinka ispravna (ako se pokušava prijaviti)

## Logovi za debugging

Aplikacija sada loguje sve autentifikacijske greške u Logcat:
- Filter: `AuthViewModel`
- Traži poruke koje počinju sa `❌` za greške ili `✅` za uspješne prijave

