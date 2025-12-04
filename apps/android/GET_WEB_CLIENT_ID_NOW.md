# KREIRAJ WEB CLIENT ID - KORAK PO KORAK

## ✅ Korak 1: Konfiguriraj OAuth Consent Screen

1. U **lijevom meniju**, klikni na **"OAuth consent screen"** (ispod "Credentials")
2. Ili klikni na plavi gumb **"Configure consent screen"** u žutoj poruci gore
3. Odaberi **"External"** (za javne aplikacije)
4. Klikni **"Create"**

### Unesi podatke:
1. **App name**: `Gatalinka`
2. **User support email**: Odaberi `larrydj@gmail.com` (ili tvoj email)
3. **Developer contact information**: `larrydj@gmail.com`
4. Klikni **"Save and Continue"**

### Scopes (korak 2):
1. Klikni **"Add or Remove Scopes"**
2. Traži i odaberi:
   - `.../auth/userinfo.email`
   - `.../auth/userinfo.profile`
3. Klikni **"Update"**
4. Klikni **"Save and Continue"**

### Test users (korak 3):
1. Možeš ostaviti prazno za sada
2. Klikni **"Save and Continue"**

### Summary (korak 4):
1. Provjeri sve podatke
2. Klikni **"Back to Dashboard"**

## ✅ Korak 2: Kreiraj OAuth 2.0 Client ID

1. Vrati se na **"Credentials"** (lijevi meni)
2. Klikni **"+ CREATE CREDENTIALS"** (gore)
3. Izaberi **"OAuth client ID"**

### Postavke:
1. **Application type**: Izaberi **"Web application"**
2. **Name**: `Gatalinka Web Client`
3. **Authorized redirect URIs**: 
   - Dodaj: `https://gatalinka-230f9.firebaseapp.com/__/auth/handler`
4. Klikni **"Create"**

## ✅ Korak 3: Kopiraj Client ID

U prozoru koji se otvori, kopiraj **"Your Client ID"** (ne Client Secret!)

To je tvoj **Web Client ID**! 🎉

## ✅ Korak 4: Dodaj u kod

Kada dobiješ Client ID, reci mi ga i ja ću ga dodati u `strings.xml`!

