# Admin Minecraft — Plugin + Panel Web

Système d'administration pour Paper 26.1.x avec panel React et base MariaDB 10.6.

## Documentation des plugins

| Plugin | Documentation |
|--------|---------------|
| AdminPlugin (in-game) | [plugin/README.md](plugin/README.md) |
| SlimeCapture | [slime-capture/README.md](slime-capture/README.md) |
| Panel web & déploiement | [DEPLOY.md](DEPLOY.md) |
| **Versions des plugins** | `gradle.properties` + `scripts/bump-version.ps1` |

## Structure

```
Admin/
├── plugin/          # Plugin Paper (Java) — voir plugin/README.md
├── slime-capture/   # Plugin capture de slimes — voir slime-capture/README.md
├── api/             # API REST Spring Boot
├── web/             # Panel React (Vite)
├── docker-compose.yml
└── luckperms-groups.yml
```

## Prérequis

- Java 25+ (requis pour Paper 26.1.x ; Gradle Wrapper télécharge le JDK si besoin)
- Gradle Wrapper inclus (`gradlew`) — aucune installation Gradle requise
- Node.js 20+
- MariaDB 10.6 (ou Docker)
- Paper 26.1.x
- LuckPerms (recommandé)

## 1. Base de données

```bash
docker compose up -d
```

MariaDB sera accessible sur `localhost:3306` :
- Base : `minecraft_admin`
- User : `admin` / `adminpassword`

## Déploiement du panel

Voir **[DEPLOY.md](DEPLOY.md)** pour la mise en ligne (Docker, local, VPS).

```powershell
copy .env.example .env
docker compose up -d --build
# Panel : http://localhost:8080  (admin / admin123)
```

## 2. API (panel backend) — développement

```bash
cd api
.\gradlew.bat bootRun
```

L'API démarre sur `http://localhost:8080`. Flyway applique automatiquement le schéma.

**Compte par défaut** (créé au premier démarrage si aucun compte n'existe) :
- Identifiant : `admin`
- Mot de passe : `admin123`
- Rôle : FONDATEUR

> Changez ce mot de passe immédiatement en production.

Configuration : `api/src/main/resources/application.yml`

## 3. Panel web (React)

```bash
cd web
npm install
npm run dev
```

Panel accessible sur `http://localhost:5173` (proxy API automatique).

## 4. Plugin Minecraft

```bash
cd plugin
.\gradlew.bat build
```

Copiez `plugin/build/libs/AdminPlugin-1.0.0.jar` dans le dossier `plugins/` de votre serveur Paper.

Configuration : `plugins/AdminPlugin/config.yml`

### Mode stockage (`storage.type`)

| Valeur | Comportement |
|--------|--------------|
| `AUTO` | MariaDB si disponible, sinon fichier JSON local (défaut) |
| `MARIADB` | Base de données ; repli JSON si MariaDB indisponible |
| `JSON` | Toutes les données dans `plugins/AdminPlugin/data.json` — **aucune base requise** |

En mode JSON, le plugin fonctionne **sans MariaDB ni panel web**. La sync web est automatiquement désactivée (pas de crash).

Alignez les identifiants MariaDB et les clés IP avec l'API si vous utilisez le panel.

## 5. LuckPerms

Importez les groupes depuis `luckperms-groups.yml` ou assignez manuellement les permissions `admin.*`.

## Fonctionnalités

### In-game
- `/ban`, `/tempban`, `/unban`, `/banlist`, `/baninfo`
- `/mute`, `/tempmute`, `/unmute`, `/mutelist`, `/muteinfo`
- `/kick`, `/warn`, `/warns`
- `/ignore`, `/unignore`, `/ignorelist`
- `/msg`, `/r`, `/socialspy`
- `/note`, `/history`, `/check`

### Panel web
- Dashboard (stats, bans récents)
- Liste de tous les joueurs connectés au moins une fois
- Fiche joueur avec historiques (bans, mutes, kicks, warns, notes, sessions)
- Actions : ban, unban, mute, unmute, warn, note
- IP visible uniquement pour le rôle **FONDATEUR**

## Sync web → serveur

Les sanctions posées depuis le panel sont écrites en base puis traitées par le plugin toutes les 5 secondes (`pending_actions`).

## Sécurité

- IP hashées + chiffrées (AES-GCM) — clés dans `config.yml` / `application.yml`
- JWT pour l'authentification panel
- Changez `jwt.secret`, `ip.hash-salt`, `ip.encryption-key` en production
