# Guide de Création de Fichiers JSON pour ArguMentor

## Vue d'ensemble

Ce guide explique comment créer des fichiers JSON compatibles avec le système d'import/export d'ArguMentor. Le format JSON suit le schéma version 1.0 et permet d'importer des sujets, affirmations, réfutations, preuves, questions, sources et tags.

---

## Structure Globale

Un fichier JSON valide doit avoir la structure suivante :

```json
{
  "schemaVersion": "1.0",
  "exportedAt": "2025-01-15T10:30:00Z",
  "app": "ArguMentor",
  "topics": [ ... ],
  "claims": [ ... ],
  "rebuttals": [ ... ],
  "evidences": [ ... ],
  "questions": [ ... ],
  "sources": [ ... ],
  "tags": [ ... ]
}
```

### Champs de l'en-tête

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `schemaVersion` | String | ✅ Oui | Doit être `"1.0"` |
| `exportedAt` | String (ISO 8601) | ✅ Oui | Date/heure d'export au format ISO 8601 |
| `app` | String | ✅ Oui | Nom de l'application (`"ArguMentor"`) |
| `topics` | Array | ✅ Oui | Liste des sujets (peut être vide `[]`) |
| `claims` | Array | ✅ Oui | Liste des affirmations (peut être vide `[]`) |
| `rebuttals` | Array | ✅ Oui | Liste des réfutations (peut être vide `[]`) |
| `evidences` | Array | ✅ Oui | Liste des preuves (peut être vide `[]`) |
| `questions` | Array | ✅ Oui | Liste des questions (peut être vide `[]`) |
| `sources` | Array | ✅ Oui | Liste des sources (peut être vide `[]`) |
| `tags` | Array | ✅ Oui | Liste des tags (peut être vide `[]`) |

---

## 1. Topics (Sujets)

Un sujet représente un thème de débat.

### Structure

```json
{
  "id": "topic_001",
  "title": "La Trinité dans le christianisme",
  "summary": "Débat sur la doctrine de la Trinité",
  "posture": "neutre_critique",
  "tags": ["tag_theologie", "tag_christianisme"],
  "createdAt": "2025-01-15T10:00:00Z",
  "updatedAt": "2025-01-15T10:00:00Z"
}
```

### Champs

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `id` | String | ✅ Oui | Identifiant unique du sujet |
| `title` | String | ✅ Oui | Titre du sujet |
| `summary` | String | ✅ Oui | Description/résumé du sujet |
| `posture` | String | ✅ Oui | Position vis-à-vis du sujet (voir valeurs autorisées) |
| `tags` | Array[String] | ❌ Non | Liste d'IDs de tags associés |
| `createdAt` | String (ISO 8601) | ❌ Non | Date de création (générée automatiquement si absente) |
| `updatedAt` | String (ISO 8601) | ❌ Non | Date de mise à jour (générée automatiquement si absente) |

### Valeurs autorisées pour `posture`

| Valeur | Description |
|--------|-------------|
| `"neutre"` | Position neutre |
| `"neutre_critique"` | Neutre mais avec analyse critique |
| `"sceptique"` | Position sceptique |
| `"opposant"` | Opposition au sujet |
| `"comparatif_academique"` | Analyse comparative académique |

**⚠️ Important** : Les valeurs sont insensibles à la casse. `"Neutre"`, `"NEUTRE"` ou `"neutre"` sont tous valides.

---

## 2. Claims (Affirmations)

Une affirmation représente un argument ou une thèse liée à un ou plusieurs sujets.

### Structure

```json
{
  "id": "claim_001",
  "text": "La Bible enseigne la doctrine de la Trinité",
  "stance": "pro",
  "strength": "high",
  "topics": ["topic_001"],
  "createdAt": "2025-01-15T10:00:00Z",
  "updatedAt": "2025-01-15T10:00:00Z",
  "claimFingerprint": "bible_enseigne_doctrine_trinite"
}
```

### Champs

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `id` | String | ✅ Oui | Identifiant unique de l'affirmation |
| `text` | String | ✅ Oui | Texte complet de l'affirmation |
| `stance` | String | ✅ Oui | Position de l'affirmation (voir valeurs autorisées) |
| `strength` | String | ✅ Oui | Force de l'argument (voir valeurs autorisées) |
| `topics` | Array[String] | ✅ Oui | Liste d'IDs de sujets auxquels appartient cette affirmation |
| `createdAt` | String (ISO 8601) | ✅ Oui | Date de création |
| `updatedAt` | String (ISO 8601) | ✅ Oui | Date de mise à jour |
| `claimFingerprint` | String | ❌ Non | Empreinte pour détecter les doublons (générée automatiquement si absente) |

### Valeurs autorisées pour `stance`

| Valeur | Description |
|--------|-------------|
| `"pro"` | Argument en faveur |
| `"con"` | Argument contre |
| `"neutral"` | Position neutre/factuelle |

### Valeurs autorisées pour `strength`

| Valeur | Description |
|--------|-------------|
| `"weak"` | Argument faible |
| `"med"` ou `"medium"` | Argument de force moyenne |
| `"high"` ou `"strong"` | Argument fort |

**⚠️ Important** : Les valeurs sont insensibles à la casse.

---

## 3. Rebuttals (Réfutations)

Une réfutation conteste ou critique une affirmation.

### Structure

```json
{
  "id": "rebuttal_001",
  "claimId": "claim_001",
  "text": "Cette interprétation ignore le contexte historique du Ier siècle",
  "fallacyTag": "Anachronisme",
  "createdAt": "2025-01-15T10:00:00Z",
  "updatedAt": "2025-01-15T10:00:00Z"
}
```

### Champs

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `id` | String | ✅ Oui | Identifiant unique de la réfutation |
| `claimId` | String | ✅ Oui | ID de l'affirmation réfutée |
| `text` | String | ✅ Oui | Texte de la réfutation |
| `fallacyTag` | String | ❌ Non | Tag identifiant un sophisme/erreur logique |
| `createdAt` | String (ISO 8601) | ✅ Oui | Date de création |
| `updatedAt` | String (ISO 8601) | ✅ Oui | Date de mise à jour |

---

## 4. Evidences (Preuves)

Une preuve soutient une affirmation avec des données factuelles.

### Structure

```json
{
  "id": "evidence_001",
  "claimId": "claim_001",
  "type": "citation",
  "content": "Jean 1:1 - 'Au commencement était la Parole, et la Parole était avec Dieu, et la Parole était Dieu'",
  "sourceId": "source_001",
  "quality": "high",
  "createdAt": "2025-01-15T10:00:00Z"
}
```

### Champs

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `id` | String | ✅ Oui | Identifiant unique de la preuve |
| `claimId` | String | ✅ Oui | ID de l'affirmation soutenue |
| `type` | String | ✅ Oui | Type de preuve (voir valeurs autorisées) |
| `content` | String | ✅ Oui | Contenu de la preuve |
| `sourceId` | String | ❌ Non | ID de la source associée |
| `quality` | String | ✅ Oui | Qualité de la preuve (voir valeurs autorisées) |
| `createdAt` | String (ISO 8601) | ✅ Oui | Date de création |

### Valeurs autorisées pour `type`

| Valeur | Description |
|--------|-------------|
| `"citation"` | Citation textuelle |
| `"statistic"` | Donnée statistique |
| `"study"` | Étude/recherche |
| `"expert"` | Témoignage d'expert |
| `"example"` | Exemple concret |
| `"anecdote"` | Anecdote |

### Valeurs autorisées pour `quality`

| Valeur | Description |
|--------|-------------|
| `"low"` | Qualité faible |
| `"medium"` | Qualité moyenne |
| `"high"` | Qualité élevée |

---

## 5. Questions

Les questions servent à clarifier ou approfondir un sujet ou une affirmation.

### Structure

```json
{
  "id": "question_001",
  "targetId": "topic_001",
  "text": "Quels sont les passages bibliques qui soutiennent ou contredisent la Trinité ?",
  "kind": "clarification",
  "createdAt": "2025-01-15T10:00:00Z"
}
```

### Champs

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `id` | String | ✅ Oui | Identifiant unique de la question |
| `targetId` | String | ✅ Oui | ID du sujet ou affirmation ciblé |
| `text` | String | ✅ Oui | Texte de la question |
| `kind` | String | ✅ Oui | Type de question (voir valeurs autorisées) |
| `createdAt` | String (ISO 8601) | ✅ Oui | Date de création |

### Valeurs autorisées pour `kind`

| Valeur | Description |
|--------|-------------|
| `"clarification"` | Question de clarification |
| `"challenge"` | Question de contestation |
| `"followup"` | Question de suivi |
| `"evidence"` | Question sur les preuves |

---

## 6. Sources

Les sources documentent l'origine des preuves et citations.

### Structure

```json
{
  "id": "source_001",
  "title": "Bible Louis Segond 1910",
  "citation": "Bible LSG, Jean 1:1",
  "url": "https://www.bible.com/fr/bible/93/JHN.1.1.LSG",
  "publisher": "Société Biblique de France",
  "date": "1910",
  "reliabilityScore": 0.95,
  "notes": "Traduction française classique",
  "createdAt": "2025-01-15T10:00:00Z"
}
```

### Champs

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `id` | String | ✅ Oui | Identifiant unique de la source |
| `title` | String | ✅ Oui | Titre de la source |
| `citation` | String | ❌ Non | Citation formelle (style académique) |
| `url` | String | ❌ Non | URL de la source en ligne |
| `publisher` | String | ❌ Non | Éditeur/auteur |
| `date` | String | ❌ Non | Date de publication |
| `reliabilityScore` | Number (0.0-1.0) | ❌ Non | Score de fiabilité (0 = non fiable, 1 = très fiable) |
| `notes` | String | ❌ Non | Notes supplémentaires |
| `createdAt` | String (ISO 8601) | ✅ Oui | Date de création |

---

## 7. Tags

Les tags permettent de catégoriser les sujets.

### Structure

```json
{
  "id": "tag_theologie",
  "label": "Théologie",
  "color": "#4CAF50"
}
```

### Champs

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `id` | String | ✅ Oui | Identifiant unique du tag |
| `label` | String | ✅ Oui | Libellé du tag (affiché dans l'interface) |
| `color` | String | ❌ Non | Couleur au format hexadécimal (#RRGGBB) |

---

## Relations Entre Entités

### Hiérarchie des dépendances

```
Tags (indépendants)
  ↓
Topics (peuvent référencer des Tags)
  ↓
Claims (doivent référencer au moins un Topic)
  ↓
├─ Rebuttals (réfutent une Claim)
├─ Evidences (soutiennent une Claim)
└─ Questions (ciblent un Topic ou une Claim)
     ↑
  Sources (peuvent être référencées par Evidences)
```

### Règles importantes

1. **Sources et Tags** peuvent être importés sans dépendances
2. **Topics** peuvent référencer des Tags qui n'existent pas encore (les IDs sont stockés)
3. **Claims** DOIVENT référencer au moins un Topic existant
4. **Rebuttals** doivent référencer une Claim existante via `claimId`
5. **Evidences** doivent référencer une Claim existante via `claimId`
6. **Questions** doivent référencer un Topic ou une Claim via `targetId`

---

## Détection des Doublons

ArguMentor utilise plusieurs mécanismes pour éviter les doublons lors de l'import :

### 1. Doublons exacts (par ID)

Si un élément avec le même `id` existe déjà :
- Pour **Topics** : mise à jour si `updatedAt` de l'import est plus récent
- Pour **Claims** : mise à jour si `updatedAt` de l'import est plus récent
- Pour **Rebuttals** : mise à jour si `updatedAt` de l'import est plus récent
- Pour **Tags, Sources, Evidences, Questions** : ignoré (considéré comme doublon)

### 2. Doublons par empreinte (fingerprint)

Pour les **Claims**, une empreinte textuelle (`claimFingerprint`) est générée automatiquement si absente. Si deux claims ont la même empreinte, le second est considéré comme doublon.

### 3. Quasi-doublons (similarité)

Lors de l'import, vous pouvez définir un **seuil de similarité** (par défaut 90%). ArguMentor compare :
- Les **Claims** ayant au moins un Topic en commun
- Les **Rebuttals** liées à la même Claim

Si la similarité textuelle dépasse le seuil, l'élément est marqué comme quasi-doublon et placé en révision.

---

## Format des Dates (ISO 8601)

Toutes les dates doivent suivre le format **ISO 8601** :

```
YYYY-MM-DDTHH:MM:SSZ
```

### Exemples valides

```json
"2025-01-15T10:30:00Z"           // UTC
"2025-01-15T10:30:00+01:00"      // UTC+1 (Paris)
"2025-01-15T10:30:00.123Z"       // Avec millisecondes
```

### Génération facile

En JavaScript :
```javascript
new Date().toISOString()
// "2025-01-15T10:30:00.123Z"
```

En Python :
```python
from datetime import datetime
datetime.utcnow().isoformat() + 'Z'
# "2025-01-15T10:30:00.123456Z"
```

---

## Exemple Complet

Voici un exemple minimal mais complet :

```json
{
  "schemaVersion": "1.0",
  "exportedAt": "2025-01-15T10:30:00Z",
  "app": "ArguMentor",
  "topics": [
    {
      "id": "topic_001",
      "title": "La Trinité",
      "summary": "Doctrine chrétienne de la Trinité",
      "posture": "neutre_critique",
      "tags": []
    }
  ],
  "claims": [
    {
      "id": "claim_001",
      "text": "La Bible enseigne explicitement la Trinité",
      "stance": "pro",
      "strength": "medium",
      "topics": ["topic_001"],
      "createdAt": "2025-01-15T10:00:00Z",
      "updatedAt": "2025-01-15T10:00:00Z"
    }
  ],
  "rebuttals": [
    {
      "id": "rebuttal_001",
      "claimId": "claim_001",
      "text": "Le mot 'Trinité' n'apparaît jamais dans la Bible",
      "createdAt": "2025-01-15T10:00:00Z",
      "updatedAt": "2025-01-15T10:00:00Z"
    }
  ],
  "evidences": [
    {
      "id": "evidence_001",
      "claimId": "claim_001",
      "type": "citation",
      "content": "Matthieu 28:19 - 'baptisez-les au nom du Père, du Fils et du Saint-Esprit'",
      "quality": "high",
      "createdAt": "2025-01-15T10:00:00Z"
    }
  ],
  "questions": [],
  "sources": [],
  "tags": []
}
```

---

## Bonnes Pratiques

### ✅ À faire

1. **Utiliser des IDs uniques et descriptifs** : `"topic_trinite"` plutôt que `"t1"`
2. **Fournir des dates cohérentes** : `createdAt` ≤ `updatedAt`
3. **Structurer hiérarchiquement** : Créer les Topics avant les Claims
4. **Documenter les sources** : Toujours renseigner les sources pour les preuves
5. **Utiliser des enums en minuscules** : Plus lisible et compatible
6. **Valider le JSON** : Utiliser un validateur JSON avant l'import

### ❌ À éviter

1. **IDs dupliqués** : Chaque ID doit être unique dans sa catégorie
2. **Références cassées** : Ne pas référencer un Topic/Claim inexistant
3. **Dates invalides** : Respecter strictement le format ISO 8601
4. **Enums inventés** : Utiliser uniquement les valeurs autorisées
5. **Claims sans Topic** : Toujours lier une Claim à au moins un Topic
6. **JSON mal formaté** : Virgules manquantes, guillemets oubliés, etc.

---

## Validation du JSON

Avant d'importer, vérifiez votre JSON :

### 1. Validité syntaxique

Utilisez un outil comme [jsonlint.com](https://jsonlint.com/) ou :

```bash
# Avec Python
python -m json.tool votre_fichier.json

# Avec Node.js
node -e "JSON.parse(require('fs').readFileSync('votre_fichier.json'))"
```

### 2. Checklist de validation

- [ ] `schemaVersion` est `"1.0"`
- [ ] Toutes les dates sont au format ISO 8601
- [ ] Toutes les valeurs d'enum sont valides
- [ ] Tous les IDs référencés existent
- [ ] Aucun ID n'est dupliqué dans sa catégorie
- [ ] Les Claims ont au moins un Topic
- [ ] Les Rebuttals référencent une Claim existante
- [ ] Les Evidences référencent une Claim existante

---

## Résolution de Problèmes

### Erreur : "Unsupported schema version"

**Cause** : `schemaVersion` n'est pas `"1.0"`
**Solution** : Corriger en `"schemaVersion": "1.0"`

### Erreur : "Invalid enum value"

**Cause** : Valeur d'enum non reconnue (ex: `"posture": "invalid"`)
**Solution** : Utiliser uniquement les valeurs autorisées listées dans ce guide

### Erreur : "Stream Closed"

**Cause** : Problème de lecture du fichier (résolu dans la version actuelle)
**Solution** : Mettre à jour l'application vers la dernière version

### Avertissement : "Quasi-doublon détecté"

**Cause** : Texte similaire à un élément existant (≥ seuil de similarité)
**Solution** : Vérifier manuellement si c'est vraiment un doublon ou ajuster le seuil

### Éléments ignorés lors de l'import

**Cause** : ID déjà existant ou empreinte identique
**Solution** : Vérifier les IDs et les contenus textuels pour éviter les doublons

---

## Support et Contribution

Pour toute question ou suggestion concernant ce format JSON :

1. Vérifier ce guide en premier
2. Consulter les exemples fournis (`ArguMentor_corpus_*.json`)
3. Ouvrir une issue sur le dépôt GitHub du projet

---

**Version du document** : 1.0
**Dernière mise à jour** : 2025-01-15
