# Mise en ligne du panel Admin

Le panel (React + API) est servi sur **un seul port** (8080 par défaut). La base de données est **MariaDB Minestrator** (partagée avec le plugin Minecraft).

---

## Docker (recommandé)

### Prérequis

- [Docker](https://docs.docker.com/get-docker/) sur le VPS ou la machine locale
- Compte Minestrator : hôte SQL, base, utilisateur, mot de passe
- **IP du VPS autorisée** dans le panel Minestrator (accès distant MySQL)

### Configuration

```bash
cp .env.example .env
nano .env   # DB_HOST, DB_NAME, DB_USER, DB_PASSWORD, JWT_SECRET, CORS_ORIGINS
```

| Variable | Exemple |
|----------|---------|
| `DB_HOST` | `sql4.minestrator.com` |
| `DB_NAME` | `minesr_ekt3j1SK` |
| `DB_USER` | `minesr_ekt3j1SK` |
| `DB_PASSWORD` | mot de passe Minestrator |
| `PANEL_BIND` | `0.0.0.0` (accès direct) ou `127.0.0.1` (derrière Nginx) |
| `CORS_ORIGINS` | `https://admin.votredomaine.fr` ou `http://IP:8080` |

### Lancement

```bash
docker compose up -d --build
```

Ou via le script :

```bash
chmod +x scripts/deploy-vps.sh
./scripts/deploy-vps.sh
```

### Accès

- Accueil : `http://IP:8080/`
- Panel : `http://IP:8080/dashboard/`
- Login par défaut : `admin` / `admin123` (à changer immédiatement)

### Commandes utiles

```bash
docker compose logs -f panel
docker compose ps
docker compose down
docker compose build --no-cache && docker compose up -d   # rebuild forcé
```

### HTTPS avec Nginx (optionnel)

`PANEL_BIND=127.0.0.1` si Nginx proxy vers le panel :

```nginx
server {
    listen 443 ssl;
    server_name admin.votredomaine.fr;

    ssl_certificate     /etc/letsencrypt/live/admin.votredomaine.fr/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/admin.votredomaine.fr/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

Mettez à jour `.env` : `CORS_ORIGINS=https://admin.votredomaine.fr`

---

## Sans Docker (développement local)

### Prérequis

- Java 21+ (API Spring Boot), Node.js 20+
- Accès à la BDD Minestrator (ou variables pointant vers votre instance)

### Étapes

```powershell
copy .env.example .env
# Éditez .env avec vos identifiants Minestrator

.\scripts\deploy-local.ps1
.\scripts\start-panel.ps1
```

Panel accessible sur **http://localhost:8080/dashboard/**

---

## Plugin Minecraft (Minestrator)

Dans `plugins/AdminPlugin/config.yml` :

```yaml
storage:
  type: MARIADB

database:
  host: sql4.minestrator.com   # même hôte que DB_HOST
  port: 3306
  database: minesr_XXXXX
  username: minesr_XXXXX
  password: <identique à DB_PASSWORD>

ip:
  hash-salt: "<identique à IP_HASH_SALT>"
  encryption-key: "<identique à IP_ENCRYPTION_KEY>"
```

---

## Déploiement auto (GitHub Actions → Hostinger)

À chaque `git push` sur `main`, le panel est redéployé via [hostinger/deploy-on-vps](https://github.com/hostinger/deploy-on-vps).

Fichier : `.github/workflows/deploy-hostinger.yml`

### Configuration GitHub (une seule fois)

**Settings → Secrets and variables → Actions**

| Type | Nom | Valeur |
|------|-----|--------|
| Secret | `HOSTINGER_API_KEY` | Clé API Hostinger ([hpanel → API](https://hpanel.hostinger.com/profile/api)) |
| Variable | `HOSTINGER_VM_ID` | ID du VPS |
| Variable | `DB_HOST` | ex. `sql4.minestrator.com` |
| Variable | `DB_PORT` | `3306` |
| Variable | `DB_NAME` | ex. `minesr_XXXXX` |
| Variable | `DB_USER` | ex. `minesr_XXXXX` |
| Variable | `CORS_ORIGINS` | ex. `http://IP_DU_VPS:8080` |
| Secret | `DB_PASSWORD` | Mot de passe Minestrator |
| Secret | `JWT_SECRET` | Secret JWT (64+ caractères) |
| Secret | `IP_HASH_SALT` | Identique au plugin |
| Secret | `IP_ENCRYPTION_KEY` | Identique au plugin |

**Repo privé** : configurez une [deploy key SSH](https://www.hostinger.com/support/how-to-deploy-from-private-github-repository-on-hostinger-docker-manager/) si besoin.

### Déclenchement

- Automatique : `git push` sur `main`
- Manuel : GitHub → Actions → **Deploy to Hostinger** → **Run workflow**

Le `project-name` dans le workflow (`mc-admin-panel`) doit correspondre au projet dans le Docker Manager Hostinger.

> Sur Hostinger, si le panel ne se met pas à jour après un push, forcez un rebuild : supprimez le projet Docker ou lancez `docker compose build --no-cache` sur le VPS.

---

## Sécurité production

- [ ] Changer le mot de passe `admin` / `admin123` après la 1ère connexion
- [ ] Générer un `JWT_SECRET` long et aléatoire (64+ caractères)
- [ ] Aligner `IP_HASH_SALT` et `IP_ENCRYPTION_KEY` entre `.env` et le plugin
- [ ] Utiliser HTTPS (Nginx + Let's Encrypt) si exposé sur Internet
- [ ] Ne pas committer le fichier `.env`

---

## Dépannage

| Problème | Solution |
|----------|----------|
| Panel inaccessible | `docker compose ps` — vérifiez que `panel` est `Up` |
| Connexion BDD refusée | IP du VPS autorisée dans Minestrator, vérifiez `.env` |
| 404 sur `/dashboard/` | Rebuild forcé : `docker compose build --no-cache && docker compose up -d` |
| Login échoue | Compte par défaut `admin` / `admin123` au 1er démarrage |
| Plugin ne sync pas | `storage.type: MARIADB`, même BDD, clés IP identiques |
| Page blanche | Rebuild : `docker compose up -d --build` |
