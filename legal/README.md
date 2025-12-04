# Legal dokumenti za Gatalinka

Ovaj folder sadrži sve pravne dokumente potrebne za objavu aplikacije na Play Store.

## 📄 Fajlovi

### Markdown verzije (za GitHub)
- `PRIVACY_POLICY.md` - Politika privatnosti (Markdown format)
- `TERMS_OF_USE.md` - Uvjeti korištenja (Markdown format)

### HTML verzije (za GitHub Pages)
- `privacy-policy.html` - Politika privatnosti (HTML format za web)
- `terms-of-use.html` - Uvjeti korištenja (HTML format za web)

## 🚀 Kako postaviti na GitHub Pages

### Opcija 1: Zaseban repository za legal dokumente

1. Kreiraj novi GitHub repository (npr. `gatalinka-legal`)
2. Upload HTML fajlove u root repository-ja
3. Idi na Settings → Pages
4. Source: Deploy from a branch → main branch → / (root)
5. Save
6. URL će biti: `https://[tvoj-username].github.io/gatalinka-legal/privacy-policy.html`

### Opcija 2: U postojećem projektu (ako imaš web folder)

1. Ako već imaš GitHub repository za Gatalinka projekt
2. Kreiraj `docs` folder u root-u
3. Kopiraj HTML fajlove u `docs` folder
4. Idi na Settings → Pages
5. Source: Deploy from a branch → main branch → /docs
6. Save
7. URL će biti: `https://[tvoj-username].github.io/gatalinka/docs/privacy-policy.html`

### Opcija 3: Gh-pages branch (napredno)

1. Kreiraj `gh-pages` branch
2. Upload HTML fajlove u root gh-pages branch-a
3. GitHub automatski će hostati na: `https://[tvoj-username].github.io/[repo-name]/privacy-policy.html`

## ✏️ Prije objave - Ažuriraj kontakt e-mail

**VAŽNO**: Prije nego što postaviš fajlove na GitHub, ažuriraj kontakt e-mail u svim fajlovima:

1. Otvori sve 4 fajla (2x .md i 2x .html)
2. Pronađi: `[Tvoj kontakt e-mail za Play Store]`
3. Zamijeni s tvojim stvarnim e-mailom (npr. `contact@gatalinka.com` ili `tvoj-email@gmail.com`)

## 🔗 Korištenje u Play Store

Kada postaviš HTML fajlove na GitHub Pages, dobit ćeš URL-ove poput:
- Privacy Policy: `https://[username].github.io/[repo]/privacy-policy.html`
- Terms of Use: `https://[username].github.io/[repo]/terms-of-use.html`

Te URL-ove uneseš u Play Console kada kreiraš aplikaciju.

## 📝 Napomene

- HTML fajlovi imaju custom styling koji odgovara dizajnu aplikacije (tamna pozadina, zlatni tekst)
- Markdown fajlovi su za lako čitanje na GitHubu
- Svi datumi su postavljeni na 2024-12-19 - ažuriraj ako mijenjaš dokumente kasnije

## ✅ Provjera

Nakon postavljanja na GitHub Pages, provjeri:
- [ ] URL-ovi su dostupni u browseru
- [ ] Kontakt e-mail je ažuriran u svim fajlovima
- [ ] Tekst je čitljiv i ispravan
- [ ] Play Store može pristupiti URL-ovima (testiraj u incognito modu)

---

**Pomoć?** Ako imaš problema s postavljanjem, provjeri [GitHub Pages dokumentaciju](https://docs.github.com/en/pages).


