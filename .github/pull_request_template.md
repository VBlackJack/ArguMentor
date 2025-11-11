# Pull Request

## 📝 Description

Décrivez clairement les changements apportés par cette PR.

Closes #(numéro de l'issue)

## 🔖 Type de changement

Cochez les cases pertinentes :

- [ ] 🐛 Bug fix (changement non-breaking qui corrige un problème)
- [ ] ✨ New feature (changement non-breaking qui ajoute une fonctionnalité)
- [ ] 💥 Breaking change (correction ou fonctionnalité qui casserait la compatibilité)
- [ ] 📝 Documentation (changements de documentation uniquement)
- [ ] 🎨 Style (formatage, point-virgules manquants, etc; pas de changement de code)
- [ ] ♻️ Refactoring (ni correction ni ajout de fonctionnalité)
- [ ] ⚡ Performance (amélioration des performances)
- [ ] ✅ Tests (ajout ou correction de tests)
- [ ] 🔧 Chore (mise à jour de dépendances, configuration, etc.)

## 🧪 Comment a été testé ?

Décrivez les tests que vous avez effectués pour vérifier vos changements.

- [ ] Tests unitaires
- [ ] Tests instrumentés
- [ ] Tests manuels sur émulateur
- [ ] Tests manuels sur appareil physique

**Configuration de test** :
- Appareil : [ex: Pixel 6 Emulator]
- Version Android : [ex: Android 14]
- Autres détails pertinents : [...]

## 📸 Captures d'écran (si applicable)

Ajoutez des captures d'écran pour montrer les changements visuels.

| Avant | Après |
|-------|-------|
| ![avant](url) | ![après](url) |

## ✅ Checklist

Avant de soumettre votre PR, vérifiez que :

### Code Quality
- [ ] Mon code suit les standards de style du projet
- [ ] J'ai effectué une auto-review de mon code
- [ ] J'ai commenté mon code, particulièrement dans les zones difficiles
- [ ] Mes changements ne génèrent pas de nouveaux warnings
- [ ] J'ai supprimé tout code commenté ou de debug
- [ ] Detekt ne rapporte aucune erreur (`./gradlew detekt`)

### Tests
- [ ] J'ai ajouté des tests qui prouvent que ma correction est efficace ou que ma fonctionnalité fonctionne
- [ ] Les tests unitaires existants passent localement (`./gradlew test`)
- [ ] Les tests instrumentés passent (si applicable) (`./gradlew connectedAndroidTest`)

### Documentation
- [ ] J'ai mis à jour la documentation correspondante (README, KDoc, etc.)
- [ ] J'ai ajouté des commentaires KDoc pour les nouvelles fonctions publiques
- [ ] J'ai mis à jour CHANGELOG.md avec mes changements

### Internationalisation
- [ ] Tous les textes UI sont internationalisés (pas de strings hardcodés)
- [ ] J'ai ajouté les traductions en français ET anglais dans `strings.xml`

### Database (si applicable)
- [ ] J'ai créé une migration Room si le schéma a changé
- [ ] J'ai testé la migration depuis la version précédente

### Sécurité
- [ ] J'ai validé toutes les entrées utilisateur
- [ ] Je n'ai pas introduit de vulnérabilités (injection SQL, XSS, etc.)
- [ ] Je n'ai pas exposé d'informations sensibles dans les logs

### Build
- [ ] Mon code compile sans erreurs (`./gradlew build`)
- [ ] J'ai testé sur plusieurs versions d'Android (si applicable)

## 🔗 Issues liées

Listez les issues liées à cette PR :

- Fixes #(numéro)
- Closes #(numéro)
- Related to #(numéro)

## 💬 Notes pour les reviewers

Ajoutez ici des notes spécifiques pour aider les reviewers :
- Points particuliers à examiner
- Zones de préoccupation
- Questions ouvertes

## 📋 Migration notes (si applicable)

Si cette PR introduit des breaking changes ou nécessite une migration :

```kotlin
// Exemple de migration pour les utilisateurs
```

---

**Merci pour votre contribution ! 🎉**
