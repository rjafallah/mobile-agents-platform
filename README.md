# Plateforme d'agents mobiles en Java

Plateforme minimale d'**agents mobiles** en Java : un agent est un objet sérialisable qui **se déplace d'un serveur à un autre avec son code et son état**, s'exécute localement sur chaque nœud, puis revient à son point de départ avec le résultat.

Projet réalisé en binôme dans le cadre du cours de Données Réparties (ENSEEIHT, 2025-2026). Il reproduit les expérimentations de l'article *A Performance Evaluation of the Mobile Agent Paradigm* (L. Ismail, D. Hagimont, OOPSLA 1999).

**Auteurs** : Mohamed Amine Rja-fallah, Abdelhamid El Amrani

---

## Principe

Au lieu de faire plusieurs allers-retours réseau entre un client et des serveurs, on envoie **l'agent vers les données** : il fait son traitement sur place et ne ramène que le résultat.

```
     App-node (:2000)               Server-node (:2001)           Server-node (:2002)
 ┌─────────────────────┐        ┌──────────────────────┐      ┌──────────────────────┐
 │ Agent (état + code) │ move() │  exécute main()      │ move │  exécute main()      │
 │                     │ ─────▶ │  utilise les services│ ───▶ │  utilise les services│
 │ résultat final  ◀───┼────────┼──────────────────────┼──────┼── back()             │
 └─────────────────────┘        └──────────────────────┘      └──────────────────────┘
```

## Fonctionnement de la plateforme

| Composant | Rôle |
|-----------|------|
| `Agent` / `AgentImpl` | Interface et implémentation de base d'un agent : `init`, `move(Node)`, `back()`, `main()` |
| `AgentServer` | Serveur multi-thread qui reçoit et exécute les agents. Deux modes : **server-node** (attend des agents) et **app-node** (lance un agent) |
| `JarClassLoader` | Charge dynamiquement les classes d'un agent à partir d'un JAR reçu par le réseau |
| `AgentInputStream` | Désérialisation qui résout les classes via le `JarClassLoader` |
| `AgentMessage` | Message transporté lors d'une migration : JAR + agent sérialisé |
| `Node` | Adresse d'un nœud (hôte, port) |
| `MoveException` | Erreur de migration |

Lors d'un `move()`, le serveur sérialise l'agent, y joint le JAR contenant son code et l'envoie au nœud cible. Celui-ci recharge les classes avec le `JarClassLoader`, désérialise l'agent et rappelle sa méthode `main()`. Chaque agent fonctionne comme une machine à états : `main()` est rappelée à chaque arrivée sur un nœud et l'attribut `state` indique la suite du trajet.

Chaque nœud possède un **serveur de noms** local qui expose des services aux agents (par exemple l'annuaire des hôtels).

## Agents fournis

| Agent | Description |
|-------|-------------|
| `Hello` | Démo : visite deux nœuds, puis revient et notifie l'application |
| `HotelAgent` | **Collecte de données** : va chez le serveur d'hôtels, puis chez l'annuaire téléphonique, fait la jointure sur place et revient avec le résultat |
| `CompresserAgent` | **Compression à la source** : transporte un document vers le serveur, le compresse là-bas et ramène le résultat |

## Structure du projet

```
.
├── rapport.pdf                      # Rapport avec les résultats de performance
└── code/
    ├── src/
    │   ├── plateforme/              # Cœur de la plateforme
    │   └── apps/                    # JAR des agents + dossier out/ (sorties)
    ├── AgentUtilisés/               # Sources des agents (Hello, Hotel, Compresser)
    ├── run-server.sh                # Lance un server-node
    ├── run-app.sh                   # Lance un app-node avec un agent
    ├── update-hoteltar.sh           # Reconstruit hotel.jar
    └── update-compressertar.sh      # Reconstruit compresser.jar
```

## Prérequis 

- JDK 17 ou plus récent (testé avec JDK 21)
- Bash (Linux, macOS, ou WSL / Git Bash sous Windows)

## Lancer le projet

Toutes les commandes se lancent **depuis le dossier `code/`** (les agents utilisent des chemins relatifs).

```bash
git clone https://github.com/rjafallah/mobile-agents-platform.git
cd mobile-agents-platform/code
chmod +x *.sh
```

**1. Démarrer deux server-nodes** (un terminal chacun) : la plateforme est compilée automatiquement.

```bash
./run-server.sh 2001
./run-server.sh 2002
```

**2. Lancer un agent depuis un app-node** (troisième terminal)

```bash
# Format : ./run-app.sh <port> <jar> <classe principale>
./run-app.sh 2000 hello.jar Hello
./run-app.sh 2000 hotel.jar HotelAgent
./run-app.sh 2000 compresser.jar CompresserAgent
```

Le résultat de la compression est écrit dans `src/apps/out/doc.out`.

**3. (Optionnel) Reconstruire les JAR après modification d'un agent**

```bash
./update-hoteltar.sh
./update-compressertar.sh
```

## Résultats attendus

| Commande | Sortie finale attendue |
|----------|------------------------|
| `hello.jar Hello` | `[Hello] I am back, notify lock` puis `[App-node localhost:2000] done.` |
| `hotel.jar HotelAgent` | Liste des hôtels avec leur numéro de téléphone, puis `done.` |
| `compresser.jar CompresserAgent` | Tailles avant/après compression, temps total, fichier `out/doc.out` écrit |

## Résultats de performance

Mesures détaillées dans [`rapport.pdf`](rapport.pdf).

**Collecte de données** : l'agent remplace de multiples allers-retours par un seul trajet.

| Ville | Nombre d'hôtels | Temps (ms) |
|-------|-----------------|------------|
| Lyon | 1 | 68 |
| Marseille | 3 | 72 |
| Bordeaux | 5 | 76 |
| Paris | 8 | 81 |

**Compression à la source** : le volume transféré est divisé par deux.

| Taille initiale | Taille compressée | Taux | Temps (ms) |
|-----------------|-------------------|------|------------|
| 250 octets | 136 octets | 54 % | 85 |
| 500 octets | 252 octets | 50 % | 92 |
| 1 Ko | 504 octets | 49 % | 105 |
| 1 Mo | ~500 Ko | 49 % | 180 |

**Conclusion** : pour de petites données, le coût de la migration (sérialisation + envoi du code) l'emporte. L'intérêt du paradigme apparaît avec un grand nombre d'éléments à traiter ou de gros volumes de données.

## Limites

- Les adresses des nœuds sont codées en dur (`localhost`, ports 2001/2002) dans les agents.
- Pas de sécurité : la désérialisation Java d'objets reçus du réseau n'est pas sûre en dehors d'un cadre pédagogique.
- Le JAR est renvoyé à chaque migration (pas de cache de code côté destinataire).

## Technologies

Java (sockets TCP, threads, sérialisation, `ClassLoader` personnalisé), scripts Bash.
