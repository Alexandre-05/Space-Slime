# AdminPlugin — Documentation

Plugin d'administration pour **Paper 26.1.x** : sanctions in-game, messages privés, notes staff, et synchronisation avec le panel web (optionnel).

| | |
|---|---|
| **Version** | 1.0.0 |
| **API Paper** | 26.1 |
| **Java requis** | 25+ |
| **Dépendance optionnelle** | LuckPerms |
| **Auteur** | alex96x2 |

---

## Installation

1. Compilez le plugin (voir [Compilation](#compilation)) ou utilisez le JAR déjà buildé.
2. Copiez `build/libs/AdminPlugin-1.0.0.jar` dans le dossier `plugins/` du serveur.
3. Démarrez le serveur une première fois pour générer `plugins/AdminPlugin/config.yml`.
4. Configurez le stockage (MariaDB ou JSON) — voir [Configuration](#configuration).
5. Redémarrez ou rechargez si nécessaire.

> Pour le **panel web** (dashboard, sanctions à distance), consultez le [README principal](../README.md) et [DEPLOY.md](../DEPLOY.md) à la racine du projet.

---

## Compilation

```powershell
cd plugin
.\gradlew.bat shadowJar
```

JAR produit : `plugin/build/libs/AdminPlugin-<version>.jar`

### Version

Fichier `gradle.properties` — incrément : `.\scripts\bump-version.ps1 -Plugin admin -Part patch`

Nouveau plugin : copier `scripts/plugin-gradle.properties.template` vers `<plugin>/gradle.properties`.

---

## Configuration

Fichier : `plugins/AdminPlugin/config.yml`

### Stockage (`storage.type`)

| Valeur | Description |
|--------|-------------|
| `AUTO` | MariaDB si disponible, sinon JSON local (**défaut**) |
| `MARIADB` | Base de données ; repli JSON si MariaDB indisponible |
| `JSON` | Fichier `data.json` uniquement — **aucune BDD requise** |

En mode `JSON`, la sync avec le panel web est **désactivée** automatiquement.

### Base de données (`database`)

```yaml
database:
  host: localhost
  port: 3306
  database: minecraft_admin
  username: admin
  password: adminpassword
  pool-size: 10
```

Les identifiants doivent correspondre à ceux de l'API si vous utilisez le panel.

### IP (`ip`)

- `store: true` — enregistre les IP des joueurs (hashées + chiffrées).
- `hash-salt` et `encryption-key` — **à changer en production**.

### Sync panel (`sync`)

| Option | Défaut | Description |
|--------|--------|-------------|
| `poll-interval-seconds` | 5 | Fréquence de lecture des actions web en attente |
| `expiration-check-seconds` | 60 | Vérification des sanctions expirées |

### Messages (`messages`)

- `prefix` — préfixe MiniMessage (ex. `<#C62828>[Admin] `).
- `ban-broadcast` — annoncer les bans au serveur.
- `msg-sound` — son à la réception d'un MP.

### Warns automatiques (`warns`)

```yaml
warns:
  auto-ban-threshold: 0    # 0 = désactivé
  auto-ban-duration: "1d"
```

---

## Format des durées

Utilisé pour `/ban`, `/tempban`, `/mute`, `/tempmute`, etc.

| Format | Exemple | Signification |
|--------|---------|---------------|
| `Xs` | `30s` | secondes |
| `Xm` | `70m` | minutes |
| `Xh` | `3h` | heures |
| `Xd` | `7d` | jours |
| `Xw` | `2w` | semaines |
| `Xy` | `1y` | années |
| `perm` / `permanent` | — | sanction permanente |

Exemples : `/tempban Steve 2h Triche` · `/mute Alex 30m Spam`

---

## Commandes in-game

### Bannissement

| Commande | Usage | Permission |
|----------|-------|------------|
| `/ban` | `/ban <joueur> [durée] [raison]` | `admin.ban` |
| `/tempban` | `/tempban <joueur> <durée> [raison]` | `admin.tempban` |
| `/unban` | `/unban <joueur\|uuid>` | `admin.unban` |
| `/banlist` | `/banlist [page]` | `admin.banlist` |
| `/baninfo` | `/baninfo <joueur>` | `admin.banlist` |

### Mute

| Commande | Usage | Permission |
|----------|-------|------------|
| `/mute` | `/mute <joueur> [durée] [raison]` | `admin.mute` |
| `/tempmute` | `/tempmute <joueur> <durée> [raison]` | `admin.tempmute` |
| `/unmute` | `/unmute <joueur>` | `admin.unmute` |
| `/mutelist` | `/mutelist [page]` | `admin.mutelist` |
| `/muteinfo` | `/muteinfo <joueur>` | `admin.mutelist` |

### Modération

| Commande | Usage | Permission |
|----------|-------|------------|
| `/kick` | `/kick <joueur> [raison]` | `admin.kick` |
| `/warn` | `/warn <joueur> [raison]` | `admin.warn` |
| `/warns` | `/warns <joueur>` | `admin.warns` |
| `/note` | `/note <joueur> <texte>` | `admin.note` |
| `/history` | `/history <joueur>` | `admin.history` |
| `/check` | `/check <joueur>` | `admin.check` |

### Messages privés

| Commande | Usage | Permission |
|----------|-------|------------|
| `/msg` | `/msg <joueur> <message>` | `admin.msg` |
| `/m`, `/tell`, `/w` | alias de `/msg` | `admin.msg` |
| `/r`, `/reply` | `/r <message>` | `admin.reply` |
| `/socialspy` | `/socialspy` | `admin.socialspy` |

### Ignore

| Commande | Usage | Permission |
|----------|-------|------------|
| `/ignore` | `/ignore <joueur>` | `admin.ignore` |
| `/unignore` | `/unignore <joueur>` | `admin.ignore` |
| `/ignorelist` | `/ignorelist` | `admin.ignore` |

---

## Permissions

### Staff

| Permission | Description | Défaut |
|------------|-------------|--------|
| `admin.ban` | Bannir | OP |
| `admin.tempban` | Ban temporaire | OP |
| `admin.unban` | Débannir | OP |
| `admin.banlist` | Liste / info bans | OP |
| `admin.mute` | Muter | OP |
| `admin.tempmute` | Mute temporaire | OP |
| `admin.unmute` | Démuter | OP |
| `admin.mutelist` | Liste / info mutes | OP |
| `admin.kick` | Expulser | OP |
| `admin.warn` | Avertir | OP |
| `admin.warns` | Voir les warns | OP |
| `admin.note` | Notes staff | OP |
| `admin.history` | Historique sanctions | OP |
| `admin.check` | Infos joueur | OP |
| `admin.socialspy` | Espionner les MP | OP |
| `admin.notify` | Notifications modération | OP |
| `admin.view.ip` | Voir les IP (fondateur) | personne |

### Joueurs

| Permission | Description | Défaut |
|------------|-------------|--------|
| `admin.msg` | Envoyer des MP | tous |
| `admin.reply` | Répondre aux MP | tous |
| `admin.ignore` | Ignorer des joueurs | tous |

### Bypass

| Permission | Description | Défaut |
|------------|-------------|--------|
| `admin.bypass.ban` | Contourner les bans | personne |
| `admin.bypass.mute` | Contourner les mutes | personne |

### Groupes LuckPerms suggérés

Un modèle est fourni dans [`luckperms-groups.yml`](../luckperms-groups.yml) : `helper` → `moderator` → `admin` → `fondateur`.

---

## Panel web (optionnel)

Le panel React permet de gérer les joueurs et sanctions depuis un navigateur.

- **Compte par défaut** : `admin` / `admin123` (à changer immédiatement).
- **Rôle fondateur** : seul rôle voyant les IP en clair.
- Les sanctions posées depuis le panel sont appliquées in-game via la table `pending_actions` (poll toutes les 5 s).

Déploiement : voir [DEPLOY.md](../DEPLOY.md).

---

## Fichiers générés

```
plugins/AdminPlugin/
├── config.yml      # Configuration principale
├── messages.yml    # Textes in-game (MiniMessage)
└── data.json       # Données locales (mode JSON ou repli)
```

---

## Sécurité — checklist production

- [ ] Changer `ip.hash-salt` et `ip.encryption-key` dans `config.yml`
- [ ] Aligner les clés IP avec `application.yml` de l'API
- [ ] Changer le mot de passe panel et `jwt.secret`
- [ ] Ne pas exposer MariaDB sans pare-feu / IP autorisées
