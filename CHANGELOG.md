# Changelog

Tous les changements notables de ce projet seront documentés dans ce fichier.

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/),
et ce projet adhère au [Semantic Versioning](https://semver.org/lang/fr/).

## [Non publié]

### Ajouté
- Documentation CHANGELOG.md pour suivre l'historique des versions
- Documentation CONTRIBUTING.md pour faciliter les contributions
- Fichier .editorconfig pour standardiser le formatage du code
- Templates GitHub pour les issues et pull requests
- Workflow GitHub Actions pour CI/CD automatisé

### Corrigé
- Version hardcodée dans SettingsScreen.kt remplacée par BuildConfig.VERSION_NAME
- Race condition dans TutorialManager.kt avec ajout de transaction atomique
- Traductions manquantes dans QuestionCreateEditScreen.kt
- Traductions manquantes dans FallacyCatalogScreen.kt
- Logs System.err remplacés par Timber dans ShareHelper.kt
- exitProcess() remplacé par recreate() pour restart plus propre de l'application

### Amélioré
- Stabilité des opérations de suppression de données du tutoriel
- Cohérence de l'internationalisation (i18n) à travers l'application
- Gestion des erreurs avec logging structuré

## [1.4.1] - 2025-11-08

### Corrigé
- Bugs d'affichage et d'UX des sophismes
- Corrections diverses de l'interface utilisateur

## [1.4.0] - 2025-11-08

### Ajouté
- Fonctionnalités CRUD complètes pour les sophismes
- Option pour ré-autoriser le microphone dans les paramètres
- Amélioration de l'exemple du sophisme de la provenance

### Supprimé
- Avertissement éthique redondant

## [1.3.0] - Date précédente

### Ajouté
- Support complet de l'internationalisation (français et anglais)
- Catalogue de 30 sophismes logiques
- Recherche full-text (FTS4) sur 7 entités
- Mode débat avec cartes révision
- Export PDF, Markdown et JSON

### Amélioré
- Architecture Clean avec MVVM
- Sécurité avec validation robuste des entrées
- Performance avec indexation stratégique

## [1.2.0] - Date précédente

### Ajouté
- Bibliothèque de modèles d'arguments
- Tutoriel intégré avec données de démonstration
- Saisie vocale multilingue
- Mode sombre complet
- Thème Material 3 avec couleurs dynamiques

## [1.1.0] - Date précédente

### Ajouté
- Gestion des sources bibliographiques
- Questions socratiques
- Tags personnalisables
- Statistiques d'utilisation

## [1.0.0] - Date initiale

### Ajouté
- Création et gestion de sujets de débat
- Affirmations avec structure hiérarchique
- Contre-arguments (réfutations)
- Preuves avec différents types
- Base de données Room avec migrations
- Interface Jetpack Compose

---

## Types de changements

- **Ajouté** pour les nouvelles fonctionnalités
- **Modifié** pour les changements dans les fonctionnalités existantes
- **Déprécié** pour les fonctionnalités bientôt supprimées
- **Supprimé** pour les fonctionnalités supprimées
- **Corrigé** pour les corrections de bugs
- **Sécurité** pour les vulnérabilités corrigées
- **Amélioré** pour les améliorations de performance ou de code
