# Mise en ligne du panel Admin

Le panel (React + API) est servi sur **un seul port** (8080 par défaut). MariaDB stocke les données partagées avec le plugin Minecraft.

---

## Option 1 — Docker (recommandé)

### Prérequis
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installé

### Étapes

```powershell
# 1. Configuration
copy .env.example .env
# Éditez .env : mots de passe, JWT_SECRET, clés IP

# 2. Lancement
docker compose up -d --build

# 3. Accès
# Panel : http://localhost:8080
# Login  : admin / admin123  (changez immédiatement)
```

### Arrêt / logs

```powershell
docker compose logs -f panel    # logs du panel
docker compose down           # arrêt
docker compose up -d --build  # rebuild après modification
```

---

## Option 2 — Sans Docker (Windows local)

### Prérequis
- Java 25+ (plugin Paper 26.1.x), Java 21+ (API Spring Boot), Node.js 20+, MariaDB (ou `docker compose up -d mariadb` seul)

### Étapes

```powershell
# 1. MariaDB
docker compose up -d mariadb

# 2. Variables d'environnement (PowerShell)
$env:DB_HOST="localhost"
$env:DB_PASSWORD="adminpassword"
$env:JWT_SECRET="votre-secret-jwt-long-et-aleatoire"
$env:IP_HASH_SALT="change-me-in-production"
$env:IP_ENCRYPTION_KEY="change-me-32-chars-minimum-key!!"
$env:CORS_ORIGINS="http://localhost:8080"

# 3. Build + lancement
.\scripts\deploy-local.ps1
cd api
java -jar build\libs\AdminApi-1.0.0.jar --spring.profiles.active=prod
```

Panel accessible sur **http://localhost:8080**

---

## Option 3 — VPS (serveur distant)

### Sur le VPS (Linux)

```bash
git clone <votre-repo> admin-panel
cd admin-panel
cp .env.example .env
nano .env   # configurez les secrets et CORS_ORIGINS=https://admin.votredomaine.fr

docker compose up -d --build
```

### Ouvrir le port
- Pare-feu : autoriser le port `8080` (ou `PANEL_PORT`)
- Accès : `http://IP_DU_VPS:8080`

### HTTPS avec Nginx (recommandé en production)

Installez Nginx + Certbot sur le VPS, puis proxy vers le panel :

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

Mettez à jour `.env` :
```
CORS_ORIGINS=https://admin.votredomaine.fr
```

---

## Option 4 — VPS + MariaDB Minestrator (recommandé si MC hébergé chez Minestrator)

Le panel tourne sur votre VPS en Docker. La base reste sur **Minestrator** (partagée avec le plugin MC).

### Prérequis Minestrator

1. Récupérez dans le panel Minestrator : **hôte SQL**, **base**, **utilisateur**, **mot de passe**
2. Autorisez l'accès distant à MySQL depuis l'**IP publique du VPS** (section BDD / accès distant Minestrator)
3. Alignez les clés IP avec `plugins/AdminPlugin/config.yml`

### Sur le VPS

```bash
git clone <votre-repo> admin-panel
cd admin-panel

cp .env.prod.example .env
nano .env   # DB_HOST, DB_NAME, DB_USER, DB_PASSWORD, JWT_SECRET, CORS_ORIGINS

chmod +x scripts/deploy-vps.sh
./scripts/deploy-vps.sh
```

Ou manuellement :

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

### Variables `.env` importantes

| Variable | Exemple |
|----------|---------|
| `DB_HOST` | `sql4.minestrator.com` |
| `DB_NAME` | `minesr_ekt3j1SK` |
| `DB_USER` | `minesr_ekt3j1SK` |
| `DB_PASSWORD` | mot de passe Minestrator |
| `PANEL_BIND` | `127.0.0.1` (Nginx) ou `0.0.0.0` (accès direct) |
| `CORS_ORIGINS` | `https://admin.votredomaine.fr` |

### HTTPS (Nginx sur le VPS)

`PANEL_BIND=127.0.0.1` par défaut — le panel n'est pas exposé directement. Configurez Nginx comme en option 3, proxy vers `127.0.0.1:8080`.

### Plugin Minecraft (Minestrator)

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

### Commandes utiles

```bash
docker compose -f docker-compose.prod.yml logs -f panel
docker compose -f docker-compose.prod.yml down
docker compose -f docker-compose.prod.yml up -d --build   # après mise à jour
docker compose -f docker-compose.prod.yml ps
```

### Dépannage Minestrator

| Problème | Solution |
|----------|----------|
| Connexion BDD refusée | Vérifier IP du VPS autorisée dans Minestrator |
| Flyway baseline | Déjà géré (`baseline-on-migrate` + `baseline-version: 0`) |
| Plugin ne sync pas | `storage.type: MARIADB`, même BDD, clés IP identiques |

---

## Lier le plugin Minecraft

Le plugin et le panel **partagent MariaDB**. Dans `plugins/AdminPlugin/config.yml` :

```yaml
storage:
  type: MARIADB   # ou AUTO

database:
  host: IP_OU_HOSTNAME_MARIADB   # localhost si MC sur la même machine
  port: 3306
  database: minecraft_admin
  username: admin
  password: <meme mot de passe que DB_PASSWORD dans .env>

ip:
  hash-salt: "<identique à IP_HASH_SALT>"
  encryption-key: "<identique à IP_ENCRYPTION_KEY>"
```

| Où tourne MC ? | `database.host` |
|----------------|-----------------|
| Même machine que Docker | `localhost` |
| Autre machine | IP du serveur MariaDB |

> MariaDB doit être accessible depuis le serveur Minecraft (port 3306 ouvert si distant).

---

## Sécurité production

- [ ] Changer le mot de passe `admin` / `admin123` après la 1ère connexion
- [ ] Générer un `JWT_SECRET` long et aléatoire (64+ caractères)
- [ ] Changer tous les mots de passe dans `.env`
- [ ] Aligner `IP_HASH_SALT` et `IP_ENCRYPTION_KEY` entre `.env` et le plugin
- [ ] Utiliser HTTPS (Nginx + Let's Encrypt) si exposé sur Internet
- [ ] Ne pas exposer le port 3306 (MariaDB) publiquement

---

## Dépannage

| Problème | Solution |
|----------|----------|
| Panel inaccessible | `docker compose ps` — vérifiez que `panel` est `Up` |
| Erreur connexion BDD | Attendez que MariaDB soit healthy, vérifiez `.env` |
| Login échoue | Compte par défaut `admin` / `admin123` au 1er démarrage |
| Plugin ne sync pas | `storage.type: MARIADB`, même BDD, panel en ligne |
| Page blanche | Rebuild : `docker compose up -d --build` |
