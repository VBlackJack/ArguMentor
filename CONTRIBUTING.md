# Guide de Contribution à ArguMentor

Merci de votre intérêt pour contribuer à ArguMentor ! Ce guide vous aidera à soumettre des contributions de qualité.

## Table des matières

- [Code de conduite](#code-de-conduite)
- [Comment contribuer](#comment-contribuer)
- [Configuration de l'environnement](#configuration-de-lenvironnement)
- [Standards de code](#standards-de-code)
- [Processus de Pull Request](#processus-de-pull-request)
- [Guide des messages de commit](#guide-des-messages-de-commit)
- [Tests](#tests)
- [Documentation](#documentation)

## Code de conduite

En participant à ce projet, vous acceptez de maintenir un environnement respectueux et inclusif pour tous les contributeurs.

## Comment contribuer

### Rapporter des bugs

Avant de créer un rapport de bug :
- Vérifiez qu'il n'existe pas déjà dans les [Issues](https://github.com/VBlackJack/ArguMentor/issues)
- Collectez des informations détaillées sur le problème

Incluez dans votre rapport :
- Une description claire et concise du bug
- Les étapes pour reproduire le problème
- Le comportement attendu vs le comportement observé
- Des captures d'écran si pertinent
- Votre environnement (version Android, appareil, version de l'app)
- Les logs pertinents

### Proposer des fonctionnalités

Avant de proposer une nouvelle fonctionnalité :
- Vérifiez qu'elle n'a pas déjà été proposée
- Assurez-vous qu'elle correspond à la philosophie du projet

Incluez dans votre proposition :
- Une description claire de la fonctionnalité
- La motivation et les cas d'usage
- Des exemples d'implémentation si possible
- Des maquettes d'interface si applicable

### Améliorer la documentation

La documentation peut toujours être améliorée ! N'hésitez pas à :
- Corriger des fautes de frappe
- Clarifier des explications
- Ajouter des exemples
- Traduire la documentation

## Configuration de l'environnement

### Prérequis

- **JDK 17** ou supérieur
- **Android Studio** Hedgehog (2023.1.1) ou plus récent
- **Git** pour le contrôle de version
- Un appareil Android (physique ou émulateur) avec **API 24+** (Android 7.0+)

### Installation

1. **Cloner le dépôt** :
   ```bash
   git clone https://github.com/VBlackJack/ArguMentor.git
   cd ArguMentor
   ```

2. **Ouvrir dans Android Studio** :
   - Fichier → Open → Sélectionner le dossier ArguMentor
   - Attendre la synchronisation Gradle

3. **Configurer l'émulateur** ou connecter un appareil physique

4. **Lancer l'application** :
   ```bash
   ./gradlew installDebug
   ```

Consultez [BUILD_GUIDE.md](BUILD_GUIDE.md) pour plus de détails.

## Standards de code

### Langage et style

- **Langage** : Kotlin 100%
- **Style** : Suivre les [conventions Kotlin officielles](https://kotlinlang.org/docs/coding-conventions.html)
- **Analyse statique** : Le projet utilise Detekt configuré dans `app/config/detekt/detekt.yml`

### Principes d'architecture

ArguMentor suit l'architecture **Clean Architecture + MVVM** :

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│  (UI - Compose + ViewModels)        │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│         Domain Layer                │
│  (Use Cases - implicite)            │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│         Data Layer                  │
│  (Repositories + DAOs + Models)     │
└─────────────────────────────────────┘
```

**Respectez ces principes** :
- Séparation des préoccupations
- Injection de dépendances via Hilt
- Réactivité avec Kotlin Flow
- Immutabilité avec data classes
- Null safety strict

### Conventions de nommage

- **Fichiers** : PascalCase (ex: `TopicRepository.kt`)
- **Classes** : PascalCase (ex: `class TopicViewModel`)
- **Fonctions** : camelCase (ex: `fun getTopic()`)
- **Variables** : camelCase (ex: `val topicId`)
- **Constantes** : SCREAMING_SNAKE_CASE (ex: `const val MAX_LENGTH = 5000`)
- **Resources** : snake_case (ex: `R.string.topic_title`)

### Documentation

- **KDoc** obligatoire pour :
  - Toutes les fonctions publiques
  - Les classes et interfaces
  - Les paramètres complexes

Exemple :
```kotlin
/**
 * Loads a topic by its ID with all related entities.
 *
 * @param topicId The unique identifier of the topic
 * @return Flow emitting the topic or null if not found
 */
fun getTopicById(topicId: String): Flow<Topic?>
```

### Internationalisation (i18n)

- **Jamais de strings hardcodés** dans le code UI
- Tous les textes doivent être dans :
  - `app/src/main/res/values/strings.xml` (français)
  - `app/src/main/res/values-en/strings.xml` (anglais)

### Sécurité

- Valider toutes les entrées utilisateur
- Utiliser `ValidationUtils` pour les URLs, chemins, etc.
- Ne jamais logger d'informations sensibles
- Utiliser Timber (pas `println` ou `printStackTrace`)

## Processus de Pull Request

### Avant de soumettre

1. **Créer une branche** depuis `main` :
   ```bash
   git checkout -b feature/ma-fonctionnalite
   ```
   ou
   ```bash
   git checkout -b fix/mon-bug-fix
   ```

2. **Faire vos modifications** en suivant les standards

3. **Tester localement** :
   ```bash
   ./gradlew test           # Tests unitaires
   ./gradlew detekt         # Analyse statique
   ./gradlew build          # Build complet
   ```

4. **Committer vos changements** (voir section suivante)

5. **Pousser votre branche** :
   ```bash
   git push origin feature/ma-fonctionnalite
   ```

### Soumettre la Pull Request

1. Aller sur GitHub et créer une Pull Request
2. Remplir le template de PR (sera ajouté automatiquement)
3. Attendre la review et les checks CI/CD
4. Répondre aux commentaires de review
5. Une fois approuvée, la PR sera mergée par un mainteneur

### Checklist de PR

- [ ] Le code suit les standards du projet
- [ ] Les tests passent (`./gradlew test`)
- [ ] Detekt ne rapporte aucune erreur (`./gradlew detekt`)
- [ ] La documentation est à jour (KDoc, README, etc.)
- [ ] Les strings sont internationalisés
- [ ] Les commits suivent le format conventionnel
- [ ] Aucun code commenté ou debug n'a été laissé
- [ ] Les changements sont décrits dans CHANGELOG.md

## Guide des messages de commit

Nous suivons le format [Conventional Commits](https://www.conventionalcommits.org/) :

### Format

```
<type>(<scope>): <description>

[corps optionnel]

[footer optionnel]
```

### Types

- **feat**: Nouvelle fonctionnalité
- **fix**: Correction de bug
- **docs**: Changements de documentation
- **style**: Formatage, point-virgules manquants, etc. (pas de changement de code)
- **refactor**: Refactoring sans changement de fonctionnalité
- **perf**: Amélioration de performance
- **test**: Ajout ou correction de tests
- **build**: Changements du système de build ou dépendances
- **ci**: Changements de configuration CI
- **chore**: Autres changements (mise à jour de version, etc.)

### Exemples

```bash
feat(topic): add ability to archive topics

fix(database): resolve race condition in TutorialManager
Wraps deletion operations in atomic transaction

docs(readme): update build instructions for Windows

style(ui): format TopicDetailScreen with proper indentation

refactor(repository): extract duplicate code into extension function

test(claim): add unit tests for claim validation logic
```

### Scope

Le scope indique quelle partie du projet est affectée :
- `topic`, `claim`, `evidence`, `rebuttal`, `question`, `source`, `tag`, `fallacy`
- `database`, `repository`, `viewmodel`, `ui`
- `export`, `import`, `search`, `tutorial`
- `i18n`, `security`, `performance`

## Tests

### Tests unitaires

Les tests unitaires sont dans `app/src/test/` :

```bash
./gradlew test
```

**Couverture de tests attendue** : 70%+ pour :
- Repositories
- ViewModels
- Utils (ValidationUtils, FingerprintUtils, etc.)
- Entities (logique métier)

### Tests instrumentés

Les tests instrumentés sont dans `app/src/androidTest/` :

```bash
./gradlew connectedAndroidTest
```

Couvrent :
- DAOs Room
- Interactions UI complexes
- Migrations de base de données

### Écrire de bons tests

```kotlin
@Test
fun `getTopic returns topic when exists`() = runTest {
    // Given
    val topic = Topic(id = "1", title = "Test Topic")
    database.topicDao().insert(topic)

    // When
    val result = repository.getTopic("1").first()

    // Then
    assertThat(result).isEqualTo(topic)
}
```

## Documentation

### README

Le fichier README.md principal doit être maintenu à jour pour :
- Les nouvelles fonctionnalités majeures
- Les changements de dépendances importantes
- Les modifications de configuration

### KDoc

Générer la documentation Kotlin :

```bash
./gradlew dokkaHtml
```

La documentation sera dans `app/build/dokka/html/`

### Guides spécifiques

- [BUILD_GUIDE.md](BUILD_GUIDE.md) : Instructions de build
- [GUIDE_DEMARRAGE.md](GUIDE_DEMARRAGE.md) : Guide de démarrage rapide
- [IMPORT_JSON_GUIDE.md](IMPORT_JSON_GUIDE.md) : Format JSON d'import/export
- [TESTING_RECOMMENDATIONS.md](TESTING_RECOMMENDATIONS.md) : Stratégie de tests

## Questions ?

- Ouvrir une [Discussion](https://github.com/VBlackJack/ArguMentor/discussions)
- Consulter la [Documentation](https://github.com/VBlackJack/ArguMentor/wiki)
- Contacter les mainteneurs via Issues

---

**Merci de contribuer à ArguMentor ! 🎉**

Votre travail aide à améliorer la pensée critique et l'analyse de débats pour tous.
