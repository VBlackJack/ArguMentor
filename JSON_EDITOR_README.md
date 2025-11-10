# 📝 ArguMentor JSON Editor - Guide d'utilisation

## Vue d'ensemble

L'**ArguMentor JSON Editor** est un éditeur HTML5 complet et moderne qui permet de créer et éditer des fichiers JSON compatibles avec l'application ArguMentor directement depuis un ordinateur.

### 🎯 À quoi sert l'éditeur JSON ?

Créer des arguments structurés sur ordinateur est souvent plus confortable qu'sur mobile. L'éditeur JSON vous permet de :

- **📚 Préparer des corpus d'arguments** : Créez des bases de données complètes sur un sujet avant de les importer sur mobile
- **👥 Collaborer facilement** : Partagez des fichiers JSON avec vos collègues, étudiants ou pairs pour travailler ensemble
- **🎓 Enseigner la pensée critique** : Les professeurs peuvent créer des exercices d'analyse argumentative
- **💾 Sauvegarder et partager** : Exportez vos meilleures analyses pour les réutiliser ou les publier
- **✍️ Éditer confortablement** : Grand écran, clavier complet, copier-coller facile
- **🔄 Migrer des données** : Importer des arguments depuis d'autres sources (Excel, Google Sheets, etc.)

### 👤 Qui devrait utiliser l'éditeur JSON ?

- **Étudiants** : Préparer des exposés ou dissertations avec arguments structurés
- **Enseignants** : Créer des ressources pédagogiques pour cours de philosophie, débat, rhétorique
- **Chercheurs** : Structurer des analyses d'articles scientifiques ou thèses
- **Journalistes** : Organiser les arguments d'un sujet d'investigation
- **Toute personne** préférant travailler sur ordinateur plutôt que mobile

## 🚀 Fonctionnalités

### ✨ Interface Triple Mode
1. **👁️ Éditeur Visuel** - Interface intuitive avec formulaires pour chaque type d'entité
2. **💻 Éditeur Code** - Éditeur JSON brut avec coloration syntaxique
3. **📊 Aperçu** - Visualisation des données avec statistiques et validation

### 🎯 Gestion Complète des 7 Entités
- **Topics** 🎯 - Sujets de débat
- **Claims** 💬 - Affirmations/Arguments
- **Rebuttals** 🔄 - Réfutations
- **Evidences** 📚 - Preuves
- **Questions** ❓ - Questions
- **Sources** 🔗 - Sources documentaires
- **Tags** 🏷️ - Étiquettes de catégorisation

### ⚙️ Fonctionnalités Avancées
- ✅ **Validation en temps réel** - Vérification de la structure JSON et des relations entre entités
- 💾 **Import/Export** - Importation et exportation de fichiers JSON
- 🌙 **Mode sombre** - Interface adaptable pour le confort visuel
- ⌨️ **Raccourcis clavier** - Navigation rapide (Ctrl+S pour exporter, Ctrl+O pour importer)
- 📱 **Design responsive** - Fonctionne sur mobile, tablette et ordinateur
- 🎨 **Material Design 3** - Design moderne aligné avec l'application ArguMentor

## 📖 Comment utiliser

### Installation
Aucune installation requise ! Il suffit d'ouvrir le fichier `json-editor.html` dans un navigateur web moderne.

```bash
# Ouvrir avec votre navigateur par défaut
open json-editor.html  # macOS
xdg-open json-editor.html  # Linux
start json-editor.html  # Windows
```

Ou simplement double-cliquer sur le fichier `json-editor.html`.

### 1️⃣ Mode Éditeur Visuel

#### Créer une nouvelle entité
1. Cliquez sur une carte d'entité (Topics, Claims, etc.)
2. Cliquez sur le bouton "➕ Ajouter nouveau"
3. Remplissez le formulaire dans la popup
4. Cliquez sur "💾 Sauvegarder"

#### Éditer une entité existante
1. Sélectionnez le type d'entité
2. Cliquez sur l'icône ✏️ à côté de l'entité à éditer
3. Modifiez les champs dans la popup
4. Cliquez sur "💾 Sauvegarder"

#### Supprimer une entité
1. Sélectionnez le type d'entité
2. Cliquez sur l'icône 🗑️ à côté de l'entité à supprimer
3. Confirmez la suppression

### 2️⃣ Mode Éditeur Code

#### Éditer le JSON directement
1. Cliquez sur l'onglet "💻 Éditeur Code"
2. Modifiez le JSON dans la zone de texte
3. Cliquez sur "✨ Formater" pour formater le JSON
4. Cliquez sur "✅ Valider" pour vérifier la validité

Le mode code est utile pour :
- Modifications en masse
- Copier/coller depuis d'autres sources
- Édition avancée avec recherche/remplacement

### 3️⃣ Mode Aperçu

1. Cliquez sur l'onglet "📊 Aperçu"
2. Visualisez les statistiques et les données
3. Vérifiez les erreurs de validation

### 📥 Importer un fichier JSON

1. Cliquez sur "📥 Importer" dans l'en-tête
2. Sélectionnez votre fichier `.json`
3. Le fichier sera validé et chargé automatiquement
4. En cas d'erreurs, vous pourrez choisir de continuer ou annuler

### 📤 Exporter un fichier JSON

1. Cliquez sur "📤 Exporter" dans l'en-tête
2. Le fichier sera téléchargé automatiquement avec la date du jour
3. Nom du fichier : `ArguMentor_export_YYYY-MM-DD.json`

**Raccourci clavier** : `Ctrl+S` (ou `Cmd+S` sur Mac)

## 🎨 Personnalisation

### Mode Sombre
Cliquez sur le bouton "🌙 Mode Sombre" dans l'en-tête pour basculer entre le thème clair et sombre.

### Couleurs des Entités
Les couleurs suivent le design Material 3 d'ArguMentor :
- **Pro** : Vert (#2E7D32)
- **Con** : Rouge (#C62828)
- **Neutral** : Gris (#616161)
- **Qualité Haute** : Vert foncé (#1B5E20)
- **Qualité Moyenne** : Orange (#EF6C00)
- **Qualité Basse** : Rouge (#D32F2F)

## ✅ Validation

L'éditeur valide automatiquement :

### Validations Structurelles
- ✅ `schemaVersion` doit être `"1.0"`
- ✅ `exportedAt` doit être au format ISO 8601
- ✅ `app` doit être `"ArguMentor"`
- ✅ Tous les tableaux d'entités doivent exister

### Validations de Relations
- ✅ Les **Claims** doivent avoir au moins un **Topic**
- ✅ Les **Rebuttals** doivent référencer un **Claim** existant
- ✅ Les **Evidences** doivent référencer un **Claim** existant
- ✅ Les **Questions** doivent référencer un **Topic** ou **Claim** existant

### Validations d'Enums
Les valeurs suivantes sont validées :
- **Posture** : `neutre`, `neutre_critique`, `sceptique`, `opposant`, `comparatif_academique`
- **Stance** : `pro`, `con`, `neutral`
- **Strength** : `weak`, `medium`, `high`
- **Evidence Type** : `citation`, `statistic`, `study`, `expert`, `example`, `anecdote`
- **Quality** : `low`, `medium`, `high`
- **Question Kind** : `clarification`, `challenge`, `followup`, `evidence`

## 📋 Structure des Entités

### Topic
```json
{
  "id": "topic_001",
  "title": "Titre du sujet",
  "summary": "Résumé du sujet",
  "posture": "neutre_critique",
  "tags": ["tag_001"],
  "createdAt": "2025-01-15T10:00:00Z",
  "updatedAt": "2025-01-15T10:00:00Z"
}
```

### Claim
```json
{
  "id": "claim_001",
  "text": "Texte de l'affirmation",
  "stance": "pro",
  "strength": "high",
  "topics": ["topic_001"],
  "createdAt": "2025-01-15T10:00:00Z",
  "updatedAt": "2025-01-15T10:00:00Z",
  "claimFingerprint": "auto_generated"
}
```

### Rebuttal
```json
{
  "id": "rebuttal_001",
  "claimId": "claim_001",
  "text": "Texte de la réfutation",
  "fallacyTag": "Anachronisme",
  "createdAt": "2025-01-15T10:00:00Z",
  "updatedAt": "2025-01-15T10:00:00Z"
}
```

### Evidence
```json
{
  "id": "evidence_001",
  "claimId": "claim_001",
  "type": "citation",
  "content": "Contenu de la preuve",
  "sourceId": "source_001",
  "quality": "high",
  "createdAt": "2025-01-15T10:00:00Z"
}
```

### Question
```json
{
  "id": "question_001",
  "targetId": "topic_001",
  "text": "Texte de la question",
  "kind": "clarification",
  "createdAt": "2025-01-15T10:00:00Z"
}
```

### Source
```json
{
  "id": "source_001",
  "title": "Titre de la source",
  "citation": "Citation académique",
  "url": "https://example.com",
  "publisher": "Éditeur",
  "date": "2025",
  "reliabilityScore": 0.95,
  "notes": "Notes supplémentaires",
  "createdAt": "2025-01-15T10:00:00Z"
}
```

### Tag
```json
{
  "id": "tag_001",
  "label": "Théologie",
  "color": "#4CAF50"
}
```

## ⌨️ Raccourcis Clavier

| Raccourci | Action |
|-----------|--------|
| `Ctrl+S` / `Cmd+S` | Exporter le JSON |
| `Ctrl+O` / `Cmd+O` | Importer un JSON |
| `Escape` | Fermer la popup |

## 🔧 Dépannage

### Le JSON ne se charge pas
- Vérifiez que le fichier est un JSON valide
- Assurez-vous que `schemaVersion` est `"1.0"`
- Consultez les messages d'erreur de validation

### Les entités ne s'affichent pas
- Vérifiez que vous avez cliqué sur une carte d'entité
- Actualisez la page et réimportez votre JSON

### Erreur de validation
- Lisez attentivement le message d'erreur
- Consultez le guide `IMPORT_JSON_GUIDE.md` pour les formats requis
- Vérifiez les relations entre entités (Claims → Topics, Rebuttals → Claims, etc.)

## 🌟 Bonnes Pratiques

1. **Sauvegardez régulièrement** - Exportez votre JSON fréquemment
2. **Validez avant d'exporter** - Utilisez le mode Aperçu pour vérifier les erreurs
3. **Utilisez des IDs descriptifs** - Ex: `topic_trinity` plutôt que `t1`
4. **Créez les dépendances en premier** - Tags → Topics → Claims → Rebuttals/Evidences
5. **Testez l'import** - Après export, testez l'import dans l'application ArguMentor

## 📚 Ressources

- **Guide JSON complet** : `IMPORT_JSON_GUIDE.md`
- **Schéma JSON** : Version 1.0
- **Application ArguMentor** : Application Android native

## 🆘 Support

Pour toute question ou problème :
1. Consultez le `IMPORT_JSON_GUIDE.md`
2. Vérifiez les exemples de fichiers JSON dans le projet
3. Ouvrez une issue sur le dépôt GitHub

## 📝 Notes Techniques

### Compatibilité Navigateurs
- ✅ Chrome/Edge 90+
- ✅ Firefox 88+
- ✅ Safari 14+
- ✅ Opera 76+

### Performance
- Testé avec plus de 1000 entités
- Pas de limite de taille de fichier (dépend du navigateur)
- Validation instantanée

### Sécurité
- Toutes les données restent en local (pas de serveur)
- Pas de connexion internet requise
- Pas de collecte de données

---

**Version** : 1.0
**Dernière mise à jour** : 2025-11-10
**Auteur** : Développé pour ArguMentor
