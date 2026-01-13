# Google Sign-In Troubleshooting - Status Code 10

## Problem
Status code 10 (DEVELOPER_ERROR) se javlja iako:
- ✅ SHA-1 je dodan u Firebase Console
- ✅ Web Client ID je ispravan
- ✅ Package name se poklapa

## Mogući uzroci

### 1. SHA-1 nije točan za debug build
U Firebase Console imaš dva SHA-1:
- `ab:76:ed:3e:0c:48:e3:da:ca:17:e1:97:b9:10:c9:98:85:ff:cb:73` (debug)
- `d6:21:19:22:2c:47:3b:0a:27:bb:65:c9:44:19:5a:c5:86:c1:4a:86` (release)

**Provjeri:**
1. Ako pokrećeš **debug build**, treba biti dodan **debug SHA-1**
2. Ako pokrećeš **release build**, treba biti dodan **release SHA-1**

### 2. SHA-1 nije propagirao
Nakon dodavanja SHA-1 u Firebase Console, može potrajati **5-10 minuta** dok se promjene propagiraju.

**Rješenje:**
1. Sačekaj 5-10 minuta
2. Restartaj app
3. Pokušaj ponovo

### 3. Google Sign-In nije omogućen u Google Cloud Console
Firebase koristi Google Cloud Console u pozadini. Možda treba provjeriti Google Cloud Console postavke.

**Provjeri:**
1. Otvori [Google Cloud Console](https://console.cloud.google.com/)
2. Odaberi projekt **gatalinka-230f9**
3. Idi na **APIs & Services** → **Credentials**
4. Provjeri da postoji **OAuth 2.0 Client ID** za Android
5. Provjeri da je **SHA-1** dodan u Android client

### 4. Web Client ID nije povezan s Android client-om
Web Client ID i Android client moraju biti u istom OAuth consent screen projektu.

**Provjeri:**
1. Firebase Console → Authentication → Sign-in method → Google
2. Provjeri da je **Web Client ID** isti kao u `strings.xml`
3. Ako nije, ažuriraj `strings.xml`

## Rješenje - Korak po korak

### Korak 1: Provjeri SHA-1 za debug build
```powershell
cd apps/android
.\get-sha1.ps1
```

Ili ručno:
```powershell
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

### Korak 2: Provjeri da je SHA-1 dodan u Firebase
1. Firebase Console → Project Settings → Your apps → Android app
2. Provjeri da postoji SHA-1: `ab:76:ed:3e:0c:48:e3:da:ca:17:e1:97:b9:10:c9:98:85:ff:cb:73`
3. Ako nema, dodaj ga

### Korak 3: Provjeri Google Cloud Console
1. [Google Cloud Console](https://console.cloud.google.com/) → Projekt **gatalinka-230f9**
2. **APIs & Services** → **Credentials**
3. Pronađi **OAuth 2.0 Client IDs**
4. Provjeri da postoji **Android client** s package name `com.gatalinka.app`
5. Provjeri da je **SHA-1** dodan u Android client

### Korak 4: Sačekaj propagaciju
1. Sačekaj **5-10 minuta** nakon dodavanja SHA-1
2. Restartaj app
3. Pokušaj ponovo

### Korak 5: Provjeri Web Client ID
1. Firebase Console → Authentication → Sign-in method → Google
2. Provjeri **Web Client ID** u "Web SDK configuration"
3. Provjeri da se poklapa s `strings.xml`:
   ```xml
   <string name="default_web_client_id">253930174034-f9su74ckmll14gaqelpo9ng046lnvjpq.apps.googleusercontent.com</string>
   ```

## Alternativno rješenje - Rekreiraj OAuth Client

Ako ništa ne pomaže, možda treba rekreirati OAuth client:

1. **Google Cloud Console** → **APIs & Services** → **Credentials**
2. **Delete** postojeći Android OAuth client
3. **Firebase Console** → **Project Settings** → **Your apps** → **Android app**
4. **Remove** app
5. **Add** app ponovo s istim package name-om
6. **Download** novi `google-services.json`
7. **Add** SHA-1 fingerprint
8. **Wait** 5-10 minuta
9. **Restart** app

## Debug logovi

Ako i dalje ne radi, provjeri logove:
```
tag:LoginScreen | tag:GoogleSignInHelper | tag:AuthViewModel
```

Trebao bi vidjeti:
- `🔍 Google Sign-In button clicked`
- `✅ Pokretanje Google Sign-In flow-a...`
- `❌ ApiException: statusCode=10` - ovo je problem

