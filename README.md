# Gatalinka

AI gatanje iz šalice kave — **zabavnog karaktera**.
Monorepo s Android appom, web-om, backendom i infrastrukturom.

## Struktura
- apps/android — Android (Jetpack Compose)
- apps/web — Web (Next.js/React) — kasnije
- services/ml — eksperimenti/modeli
- packages/shared-* — shared sheme i UI
- infra/firebase — Firebase (functions/config/rules/RC)
- infra/cloud — docker-compose, nginx, deploy skripte
- infra/cicd — GitHub Actions
- assets — brending, UI, sample fotke šalica
- docs — product/UX/roadmap
- legal — PRIVACY, TERMS, DISCLAIMER

## Node verzija (Firebase Functions)
Firebase Cloud Functions se razvijaju na **Node 20**.

- `infra/firebase/functions/.nvmrc` i `infra/firebase/functions/.node-version` su postavljeni na `20`
- Na Windowsu preporuka je [`nvm-windows`](https://github.com/coreybutler/nvm-windows):
  - `nvm install 20`
  - `nvm use 20`
