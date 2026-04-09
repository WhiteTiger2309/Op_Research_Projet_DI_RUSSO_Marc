# Rapport (LaTeX)

## Compiler
Depuis la racine du dépôt :

```bash
cd rapport
pdflatex -interaction=nonstopmode rapport_projet.tex
pdflatex -interaction=nonstopmode rapport_projet.tex
```

Le fichier PDF généré est `rapport_projet.pdf`.

## Notes
- La classe `rapport.cls` est reprise du dossier `rapport_exemple/`.
- Les logos sont chargés depuis `rapport_exemple/img/` via `\graphicspath{...}` dans `rapport_projet.tex`.
