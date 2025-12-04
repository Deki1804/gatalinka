# Mini koraci: Kako dobiti Web Client ID

## Korak 1: Otvori Authentication

1. U **lijevom meniju** (tamo gdje vidiš "Project Overview", "App Hosting", "Authentication")
2. Klikni na **"Authentication"** (ikonica plamena/ključa)

## Korak 2: Otvori Google Sign In settings

1. Kad si u Authentication, vidiš sekciju "Sign-in providers" ili "Sign-in method"
2. Klikni na **"Google"** (ikonica slova "G" u krug)
3. Trebao bi se otvoriti prozor sa Google Sign In postavkama

## Korak 3: Pronađi Web Client ID

1. U tom prozoru, scroll dolje dok ne vidiš sekciju **"Web SDK configuration"**
2. Tu bi trebao vidjeti polje **"Web client ID"**
3. Ako **VIDIŠ** tekst u tom polju (npr. `123456789-xxxxx.apps.googleusercontent.com`):
   - **KOPIRAJ** taj tekst
   - **GOTOVO!** - idi na Korak 4

4. Ako je polje **PRAZNO** ili piše "Not set":
   - Trebaš kreirati Web aplikaciju - idi na Korak 3B

## Korak 3B: Kreiraj Web aplikaciju (ako je polje prazno)

1. Zatvori prozor za Google Sign In (X ili Cancel)
2. Idi na **Project Settings** (zupčanik u lijevom gornjem kutu)
3. U sekciji "Your apps", klikni na **"Add app"** (plavi gumb)
4. Izaberi **Web** (ikonica `</>`)
5. Unesi app nickname: **"Gatalinka Web"** (ili bilo što)
6. **NEMOJ** čekirati "Also set up Firebase Hosting"
7. Klikni **"Register app"**
8. U sljedećem prozoru, **ZATVORI** ga (ne treba ti config kod)
9. Sada se vrati na: **Authentication** → **Sign-in method** → **Google**
10. Scroll do **"Web SDK configuration"**
11. Sada bi trebao vidjeti **Web client ID** u polju
12. **KOPIRAJ** ga

## Korak 4: Dodaj Web Client ID u aplikaciju

1. Otvori u editoru: `apps/android/app/src/main/res/values/strings.xml`
2. Nađi liniju:
   ```xml
   <string name="default_web_client_id" translatable="false">YOUR_WEB_CLIENT_ID_HERE</string>
   ```
3. Zamijeni `YOUR_WEB_CLIENT_ID_HERE` sa Web Client ID-om koji si kopirao

Primjer:
```xml
<string name="default_web_client_id" translatable="false">123456789-abcdefghijklmnop.apps.googleusercontent.com</string>
```

## Korak 5: Gotovo!

Nakon što dodaš Web Client ID u `strings.xml`, Google Sign In će raditi!

