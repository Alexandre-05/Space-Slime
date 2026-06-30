# SlimeCapture — Documentation

Capture des slimes à l'aspirateur, inspiré de **Slime Rancher**. Les slimes capturés sont stockés dans le réservoir de l'aspirateur, transférables en **capsules**, puis transformés en **gel** via l'**extracteur de gelée**.

| | |
|---|---|
| **Version** | 1.0.0 |
| **API Paper** | 26.1 |
| **Java requis** | 25+ |
| **Auteur** | alex96x2 |

---

## Installation

1. Compilez le plugin (voir [Compilation](#compilation)).
2. Copiez `build/libs/SlimeCapture-1.0.0.jar` dans `plugins/` du serveur Paper **26.1.x**.
3. Démarrez le serveur pour générer `plugins/SlimeCapture/config.yml`.
4. En jeu : `/slimecapture gun` pour obtenir l'aspirateur, `/sc machine` pour l'extracteur.

---

## Compilation

```powershell
cd slime-capture
.\gradlew.bat build
```

JAR produit : `slime-capture/build/libs/SlimeCapture-<version>.jar`

### Version

Fichier `gradle.properties` :

```properties
version=1.0.0
```

```powershell
.\scripts\bump-version.ps1 -Plugin slime-capture -Part patch
```

## Retours en jeu

- **Chat** : activation / désactivation de l'aspirateur.
- **Barre d'action** (au-dessus de la barre d'XP) : captures et relâchements, cumulés puis effacés après **2 s** d'inactivité (`actionbar.idle-seconds`).

---

## Utilisation en jeu

### 1. Obtenir l'aspirateur

```
/slimecapture gun
```

Alias de la commande : `/sc`, `/slimerancher`, `/sr`

### 2. Capturer des slimes

1. Tenez l'**Aspirateur à Slimes** (Bâton de Blaze) en **main principale**.
2. **Clic droit** → active l'aspiration (portée 8 blocs, visez un slime).
3. Le slime est **aspiré** vers vous (particules + sons).
4. À courte distance, il est capturé et stocké dans le **réservoir** de l'aspirateur.
5. **Re-clic droit** → désactive l'aspiration.
6. **Clic gauche** → relâche un slime dans la direction visée.

Le tooltip de l'aspirateur affiche : `Réservoir : X / 100` (capacité configurable).

> Aucun item n'est ajouté à l'inventaire lors de la capture.

### 3. Vérifier le réservoir

```
/sc status
```

### 4. Créer des capsules

Quand le réservoir est plein ou que vous voulez transférer des slimes :

```
/sc capsule          # vide tout le réservoir en capsules
/sc capsule 10       # met 10 slimes en capsule(s)
```

Les **Capsules de Slimes** (fiole de miel custom) apparaissent dans l'inventaire. Chaque capsule contient jusqu'à **20 slimes** et s'insère dans l'extracteur.

### 5. Extracteur de gelée

```
/sc machine
```

Alias : `/sc extracteur`

1. **Clic droit** sur un bloc avec l'extracteur (Lodestone) → pose un fumoir marqué.
2. **Clic droit** sur le fumoir → ouvre l'interface.
3. Déposez :
   - **Capsule de slimes** (slot central)
   - **Carburant** (charbon, charcoal, bloc de charbon) → alimente un réservoir virtuel
   - **Nourriture** (blé, sucre, bloc de foin) → alimente un réservoir virtuel
4. L'extraction tourne même interface fermée (~5 s par cycle).
5. **Clic gauche** sur le bouton « Stockage gel » → retire le gel dans l'inventaire (vérifie la place disponible).

Les réservoirs carburant / nourriture / gel affichent leur niveau dans les **tooltips** des slots dédiés.

| Élément | Détail |
|---------|--------|
| Coût par cycle | 1 unité carburant + 1 unité nourriture |
| Gel produit | 1 à 3 aléatoirement par cycle |
| Perte de slime | 35 % de chance par cycle (configurable) |
| 1 charbon | 16 unités de carburant (16 cycles) |
| 1 blé | 8 unités de nourriture (8 cycles) |

Casser le fumoir rend l'extracteur, le contenu des slots et le gel stocké.

---

## Rendement par taille de slime

| Taille slime (`getSize()`) | Slimes stockés |
|----------------------------|----------------|
| 1 (petit) | 1 |
| 2 | 2 |
| 4 (moyen) | 4 |
| 8 (gros) | 8 |

Configurable dans `config.yml` → section `yields`.

---

## Commandes

| Commande | Description | Permission |
|----------|-------------|------------|
| `/slimecapture gun` | Reçoit un aspirateur | `slimecapture.gun` |
| `/slimecapture gun <joueur>` | Donne un aspirateur à un joueur | `slimecapture.gun.others` |
| `/slimecapture capsule [qté]` | Crée des capsules depuis le réservoir | `slimecapture.use` |
| `/slimecapture machine [joueur]` | Reçoit un extracteur de gelée | `slimecapture.machine.give` |
| `/slimecapture machine <joueur>` | Donne un extracteur à un joueur | `slimecapture.machine.give.others` |
| `/slimecapture status` | Affiche le niveau du réservoir | `slimecapture.use` |

**Alias** : `/sc`, `/slimerancher`, `/sr` — sous-commande machine : `/sc extracteur`

---

## Permissions

| Permission | Description | Défaut |
|------------|-------------|--------|
| `slimecapture.use` | Utiliser l'aspirateur et les commandes capsule/status | tous |
| `slimecapture.gun` | Recevoir l'aspirateur | OP |
| `slimecapture.gun.others` | Donner l'aspirateur à un autre joueur | OP |
| `slimecapture.machine.place` | Placer un extracteur | tous |
| `slimecapture.machine.use` | Ouvrir un extracteur | tous |
| `slimecapture.machine.give` | Recevoir un extracteur | OP |
| `slimecapture.machine.give.others` | Donner un extracteur à un autre joueur | OP |

---

## Items personnalisés

### Aspirateur à Slimes

| | |
|---|---|
| **Matériau** | Bâton de Blaze |
| **Stockage** | Données PDC sur l'item (`gun_slime_count`) |
| **Capacité** | 100 slimes (défaut) |

L'aspirateur conserve ses slimes même après déconnexion (données sur l'item).

### Capsule de Slimes

| | |
|---|---|
| **Matériau** | Fiole de miel |
| **Contenu max** | 20 slimes par capsule |
| **Empilage** | 1 capsule par slot (non empilable) |
| **Usage** | Insertion dans l'extracteur |

### Extracteur de Gelée

| | |
|---|---|
| **Matériau** | Lodestone |
| **Bloc posé** | Fumoir (marqué PDC) |
| **Usage** | Transformation slimes → gel |

### Gel de Slime

| | |
|---|---|
| **Matériau** | Boule de slime |
| **Identification** | PDC `slime_gel` |
| **Obtention** | Production par l'extracteur, retrait via l'interface |

---

## Configuration

Fichier : `plugins/SlimeCapture/config.yml`

### Aspiration (`vacuum`)

| Option | Défaut | Description |
|--------|--------|-------------|
| `range` | 8.0 | Portée de visée (blocs) |
| `capture-distance` | 1.6 | Distance de capture |
| `pull-strength` | 0.42 | Force d'aspiration |
| `session-ticks` | 100 | Durée max d'une session (ticks) |
| `tick-interval` | 2 | Intervalle entre chaque tick d'aspiration |

### Stockage (`storage`)

| Option | Défaut | Description |
|--------|--------|-------------|
| `gun-capacity` | 100 | Capacité max du réservoir |
| `canister-max-stack` | 20 | Slimes max par capsule |

### Extracteur (`machine`)

| Option | Défaut | Description |
|--------|--------|-------------|
| `process-duration-ticks` | 100 | Durée d'un cycle (~5 s) |
| `fuel-storage-max` | 256 | Capacité réservoir carburant |
| `food-storage-max` | 256 | Capacité réservoir nourriture |
| `gel-storage-max` | 512 | Capacité stockage gel |
| `fuel-cost-per-batch` | 1 | Unités carburant / cycle |
| `food-cost-per-batch` | 1 | Unités nourriture / cycle |
| `gel-min` / `gel-max` | 1 / 3 | Gel produit par cycle |
| `slime-loss-chance` | 0.35 | Probabilité de retirer 1 slime |
| `fuel-units` / `food-units` | voir config | Unités par item déposé |

### Messages (`messages`)

Tous les messages supportent le format **MiniMessage**. Placeholders : `{amount}`, `{current}`, `{capacity}`, `{remaining}`, `{player}`.

---

## API pour les développeurs

Classe publique : `fr.alex96x2.slimecapture.api.SlimeMachineAPI`

```java
boolean isSlimeCanister(ItemStack item);
int getCanisterAmount(ItemStack item);
ItemStack consumeFromCanister(ItemStack canister, int amount);
boolean isSlimeGel(ItemStack item);
```

---

## Fichiers générés

```
plugins/SlimeCapture/
├── config.yml      # Configuration et messages
└── machines.yml    # Données des extracteurs placés
```

---

## Dépannage

| Problème | Solution |
|----------|----------|
| « Réservoir plein » | `/sc capsule` pour créer des capsules |
| « Tenez l'aspirateur en main principale » | L'aspirateur doit être en slot actif pour capsule/status |
| Le slime n'est pas capturé | Vérifiez la ligne de vue, la portée (8 blocs) et que le réservoir n'est pas plein |
| L'extraction ne démarre pas | Vérifiez capsule + réservoirs carburant/nourriture (tooltips), pas seulement les slots vides |
| Gel non retiré | Vérifiez la place dans l'inventaire, puis clic sur « Stockage gel » |
| Compilation échoue (Java 25) | Gradle télécharge le JDK via Foojay si `settings.gradle.kts` est configuré |
