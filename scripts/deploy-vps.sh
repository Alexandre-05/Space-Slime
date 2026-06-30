#!/usr/bin/env bash
# Déploiement panel sur VPS (MariaDB Minestrator)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f .env ]]; then
  echo "Fichier .env introuvable. Copiez .env.example :"
  echo "  cp .env.example .env && nano .env"
  exit 1
fi

echo "==> Build et démarrage du panel..."
docker compose up -d --build

echo ""
echo "Panel démarré. Vérifiez : docker compose logs -f panel"
echo "Login par défaut : admin / admin123 (à changer immédiatement)"
