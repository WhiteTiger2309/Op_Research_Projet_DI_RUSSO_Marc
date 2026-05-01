# Projet Recherche Opérationnelle — Flots (Java)

Ce dépôt implémente **sans bibliothèques de flots** :
- Flot maximum (Edmonds–Karp / Ford-Fulkerson BFS) + extraction de **coupe minimale (min-cut)**
- Flot de **coût minimum** (Successive Shortest Path) en 2 variantes :
  - Variante A : Bellman-Ford (coûts négatifs)
  - Variante B : Dijkstra + renormalisation (potentiels / coûts réduits)
- Détection de **cycle négatif** dans le graphe résiduel

## Prérequis
- Java (JDK) 17+ recommandé (mais 11+ devrait fonctionner)

## Format d’entrée
- Ligne 1 : `n m s t [F]`
- Puis `m` lignes : `u v cap cost`
- `F` est optionnel :
  - si présent et mode `mincost-*` : on vise un flot total `F`
  - sinon : on pousse jusqu’à saturation (min-cost max-flow)

Indexation :
- Par défaut, le programme considère des sommets **0-based**.
- Si votre fichier utilise des sommets **1..n**, ajouter le flag `--one-based`.

Les identifiants de sommets affichés en sortie suivent la même convention.

Commentaires : lignes vides ou commençant par `#` ignorées.

## Compiler
Depuis la racine :

```bash
javac -d out src/*.java
```

## Exécuter
### Flot maximum + min-cut
```bash
java -cp out Main maxflow examples/maxflow1.txt
```

### Min-cost flow (Bellman-Ford)
```bash
java -cp out Main mincost-bf examples/mincostF1.txt
```

### Min-cost flow (Dijkstra + potentiels)
```bash
java -cp out Main mincost-dij examples/mincostF1.txt
```

### Bench (comparaison BF vs Dijkstra)
```bash
java -cp out Main bench examples/mincostF1.txt
```

## Sorties
- `TOTAL_FLOW`, `TOTAL_COST` (si applicable)
- Flot sur chaque arc original (`flow/cap`)
- `MIN_CUT_S` et `MIN_CUT_EDGES` (mode maxflow)
- `NEGATIVE_CYCLE_IN_RESIDUAL` (modes mincost)



