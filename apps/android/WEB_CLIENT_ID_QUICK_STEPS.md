# Brzi koraci: Dobij Web Client ID

## Korak 1: Enable Google Sign In ✅
1. Na ekranu koji imaš otvoren, klikni na **toggle "Enable"** (siva ikonica)
2. Otvorit će se prozor sa Google postavkama

## Korak 2: Provjeri Web Client ID
U prozoru koji se otvori:
1. Scroll dolje do sekcije **"Web SDK configuration"**
2. Provjeri polje **"Web client ID"**:
   - Ako **VIDIŠ** tekst (npr. `123456789-xxxxx.apps.googleusercontent.com`) → **KOPIRAJ GA** i gotovo!
   - Ako je **PRAZNO** → nastavi na Korak 3

## Korak 3: Kreiraj Web aplikaciju (samo ako je polje prazno)

### 3.1: Otvori Project Settings
1. Klikni na **zupčanik** (⚙️) u lijevom gornjem kutu (Project Settings)
2. Scroll do sekcije **"Your apps"**

### 3.2: Dodaj Web aplikaciju
1. Klikni na plavi gumb **"Add app"** (gore desno u "Your apps" sekciji)
2. Izaberi **Web** (ikonica `</>`)
3. Unesi nickname: **"Gatalinka Web"** (može biti bilo što)
4. **NEMOJ** čekirati "Also set up Firebase Hosting"
5. Klikni **"Register app"**

### 3.3: Zatvori config prozor
1. U prozoru koji se otvori, **ZATVORI** ga (X ili Cancel)
2. Ne treba ti config kod

### 3.4: Vrati se na Google postavke
1. Idi na **Authentication** → **Sign-in method** → **Google**
2. Klikni na **"Enable"** (ako nije već enable-ano)
3. Scroll do **"Web SDK configuration"**
4. Sada bi trebao vidjeti **Web Client ID** u polju
5. **KOPIRAJ GA**

## Korak 4: Dodaj u kod

Kada dobiješ Web Client ID, reci mi ga i ja ću ga dodati, ili:

1. Otvori: `apps/android/app/src/main/res/values/strings.xml`
2. Nađi: `<string name="default_web_client_id" translatable="false">YOUR_WEB_CLIENT_ID_HERE</string>`
3. Zamijeni `YOUR_WEB_CLIENT_ID_HERE` sa Web Client ID-om

## Gotovo! 🎉

Nakon toga će Google Sign In raditi!

