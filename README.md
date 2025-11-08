# ArguMentor

**ArguMentor** est une application Android personnelle pour créer, organiser et consulter des sujets d'argumentation avec arguments, contre-arguments, questions socratiques et sources bibliographiques.

## 🎯 Objectif

Fournir un outil rigoureux pour structurer la pensée critique sur des sujets sensibles (religion, politique, santé, sciences) avec bienveillance et rigueur intellectuelle.

## ✨ Fonctionnalités MVP (v1.0)

### Core Features
- ✅ **CRUD complet** : Création/modification/suppression de Sujets, Affirmations, Contre-arguments, Preuves, Questions, Sources, Tags
- ✅ **Recherche plein-texte** : Recherche FTS (Full-Text Search) sur claims, rebuttals et questions
- ✅ **Liens croisés** : Un claim peut appartenir à plusieurs topics
- ✅ **Mode Débat** : Cartes recto/verso pour réviser arguments et contre-arguments
- ✅ **Import/Export JSON** : Format versionné (schema v1.0) avec anti-doublons intelligent
- ✅ **Bibliothèque de modèles** : Templates pour arguments doctrinaux, scientifiques, témoignages, etc.
- ✅ **Catalogue de sophismes** : 15+ fallacies cataloguées (ad hominem, straw man, post hoc, etc.)
- ✅ **Avertissement éthique** : Écran d'avertissement au premier lancement
- ✅ **Thème clair/sombre** : Support des deux thèmes

### Architecture Technique

#### Stack
- **Langage** : Kotlin
- **UI** : Jetpack Compose avec Material 3
- **Architecture** : MVVM (Model-View-ViewModel)
- **Injection de dépendances** : Hilt
- **Base de données** : Room avec FTS4 pour la recherche
- **Navigation** : Jetpack Navigation Compose
- **Concurrence** : Kotlin Coroutines + Flow
- **Tâches en arrière-plan** : WorkManager

#### Structure du projet
```
app/
├── data/
│   ├── constants/          # Catalogs (fallacies, templates)
│   ├── dto/                # Data Transfer Objects pour JSON
│   ├── local/              # Room database, DAOs, FTS
│   ├── model/              # Entités du domaine
│   └── repository/         # Couche d'accès aux données
├── di/                     # Modules Hilt
├── ui/
│   ├── navigation/         # Navigation Compose
│   ├── screens/            # Écrans Compose
│   └── theme/              # Thème Material 3
└── util/                   # Utilities (fingerprints, etc.)
```

### Modèle de données

#### Entités principales
- **Topic** : Sujet de discussion avec posture (neutre/sceptique/comparatif)
- **Claim** : Affirmation avec stance (pro/con/neutral) et strength (low/med/high)
- **Rebuttal** : Contre-argument lié à un claim, avec tag sophisme optionnel
- **Evidence** : Preuve (étude/stat/citation/exemple) avec quality rating
- **Question** : Question (socratique/clarification) liée à un topic ou claim
- **Source** : Source bibliographique avec score de fiabilité
- **Tag** : Étiquette pour catégoriser topics et claims

#### Format d'export JSON (v1.0)

```json
{
  "schemaVersion": "1.0",
  "exportedAt": "2025-11-08T13:00:00Z",
  "app": "ArguMentor",
  "topics": [...],
  "claims": [...],
  "rebuttals": [...],
  "evidences": [...],
  "questions": [...],
  "sources": [...],
  "tags": [...]
}
```

### Anti-Duplicate Logic

L'import utilise plusieurs stratégies :
1. **Correspondance exacte par ID** : Mise à jour si `updatedAt` plus récent
2. **Fingerprints** : Hash SHA-256 du texte normalisé (claims, rebuttals, sources)
3. **Similarité Levenshtein** : Détection des quasi-doublons (seuil configurable 0.85-0.95)
4. **Revue manuelle** : Items marqués `needs_review` en cas de conflit

#### Normalisation du texte
- Lowercase
- Suppression des accents (NFD decomposition)
- Suppression de la ponctuation Unicode
- Collapse des espaces multiples
- Trim

## 🚀 Installation & Build

### Prérequis
- Android Studio Hedgehog (2023.1.1) ou supérieur
- JDK 17
- Android SDK (minSdk 24, targetSdk 34)
- Gradle 8.2+

### Build
```bash
# Clone le repo
git clone https://github.com/VBlackJack/ArguMentor.git
cd ArguMentor

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

### Linting & Code Quality
```bash
# Detekt static analysis
./gradlew detekt

# Ktlint
./gradlew ktlintCheck
```

## 📖 Utilisation

### Premier lancement
1. Accepter l'avertissement éthique
2. Créer un premier sujet (bouton +)
3. Ajouter des affirmations, preuves, contre-arguments

### Import/Export
#### Export
- Menu → Import/Export → Export
- Fichier JSON sauvegardé dans Downloads/

#### Import
- Menu → Import/Export → Import
- Sélectionner fichier JSON
- Prévisualisation des changements (créations/mises à jour/doublons)
- Confirmer l'import

### Mode Débat
- Ouvrir un topic
- Menu → Mode Débat
- Swiper les cartes pour réviser

## 📚 Catalogues

### Sophismes (15 types)
- Ad Hominem
- Straw Man (Épouvantail)
- Appeal to Ignorance
- Post Hoc
- False Dilemma
- Begging the Question
- Slippery Slope
- Postdiction
- Cherry Picking
- Appeal to Authority/Tradition/Popularity
- Circular Reasoning
- Tu Quoque
- Hasty Generalization

### Templates (6 types)
1. **Affirmation Doctrinale** : Religion, philosophie, idéologie
2. **Argument d'Autorité** : Évaluation d'expert
3. **Fait Scientifique** : Études, expériences
4. **Témoignage** : Évaluation de fiabilité
5. **Comparatif Académique** : Comparaison systématique
6. **Affirmation Historique** : Événements historiques

## 🧪 Tests

### Unit Tests
- Repositories (CRUD, search, fingerprints)
- Import/Export (anti-doublons, conflits)
- FingerprintUtils (normalisation, Levenshtein)

### Tests instrumentés
- Navigation
- CRUD flows
- Search/FTS
- Import/Export avec Room

**Objectif de couverture** : ≥70%

## 🗺️ Roadmap

### v1.1 (Q2 2025)
- [ ] Dictée vocale (SpeechRecognizer)
- [ ] Scoring automatique de qualité de preuve
- [ ] Favoris/bookmarks
- [ ] Partage de topics individuels

### v2.0 (Q3 2025)
- [ ] OCR pour capturer textes papier (ML Kit)
- [ ] Détection assistée de sophismes (NLP)
- [ ] Chiffrement local (SQLCipher) + biométrie
- [ ] Synchro cloud chiffrée (Firebase)
- [ ] Assistant IA pour suggestions d'arguments

## 📄 Licence

**Apache License 2.0**

Copyright 2025 VBlackJack

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## 🤝 Contribution

Ce projet est actuellement en phase MVP et développement personnel. Les contributions externes ne sont pas encore acceptées. Pour signaler des bugs ou suggérer des fonctionnalités, merci d'ouvrir une issue.

## 📧 Contact

Pour toute question : [Ouvrir une issue](https://github.com/VBlackJack/ArguMentor/issues)

---

**Note** : Ce projet vise à promouvoir la pensée critique rigoureuse et le dialogue respectueux, même sur les sujets les plus sensibles.
