# Alternativni način: Dobij Web Client ID bez Google Cloud Console

## Problem
Projekat se ne vidi u Google Cloud Console.

## Rješenje: Omogući Google Cloud API kroz Firebase

### Korak 1: Aktiviraj Google Cloud API
1. Idi na Firebase Console
2. Project Settings (zupčanik)
3. Scroll dolje do sekcije **"Your apps"**
4. Vidiš li **"Web apps"** sekciju?
   - Ako DA: tvoj Web Client ID je možda već tu
   - Ako NE: kreiraj Web app ponovo (ako već nisi)

### Korak 2: Provjeri Google Cloud API
1. U Firebase Console, idi na **"APIs & Services"** ili **"Cloud Console"** (možda u meniju)
2. Ili direktno: https://console.cloud.google.com/apis/library?project=gatalinka-230f9
3. To će otvoriti Google Cloud Console za tvoj projekat

### Korak 3: Ako i dalje ne radi - koristi API Key
Umjesto Web Client ID, možemo koristiti API Key koji već postoji u `google-services.json`:
- API Key: `AIzaSyDbFwSVVfp40kGJbzcVxR-abgilds8Q31Q`

Ali za Google Sign In, trebamo OAuth Client ID, ne API Key.

## Najbolje rješenje: Link Firebase sa Google Cloud

1. Idi na: https://console.cloud.google.com/
2. Klikni na dropdown za projekat (gore lijevo)
3. Ako ne vidiš "gatalinka-230f9", klikni **"NEW PROJECT"**
4. Projekt ID: `gatalinka-230f9`
5. Ili probaj **"Select a project"** → **"Browse"** → traži "gatalinka"

## Još jednostavnije: Koristi postojeći OAuth Client

Možda već postoji OAuth client. Probaj:

1. Idi na: https://console.cloud.google.com/apis/credentials?project=gatalinka-230f9
2. Ako vidiš neke credentials, provjeri da li postoji "OAuth 2.0 Client ID"

