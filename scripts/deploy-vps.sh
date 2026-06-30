#!/usr/bin/env bash
# Déploiement panel sur VPS (MariaDB externe — Minestrator)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f .env ]]; then
  echo "Fichier .env introuvable. Copiez .env.prod.example :"
  echo "  cp .env.prod.example .env && nano .env"
  exit 1
fi

echo "==> Build et démarrage du panel (docker-compose.prod.yml)..."
docker compose -f docker-compose.prod.yml up -d --build

echo ""
echo "Panel démarré. Vérifiez : docker compose -f docker-compose.prod.yml logs -f panel"
echo "Login par défaut : admin / admin123 (à changer immédiatement)"
