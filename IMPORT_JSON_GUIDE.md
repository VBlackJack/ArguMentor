# Guide de Création de Fichiers JSON pour ArguMentor

## Vue d'ensemble

Ce guide explique comment créer des fichiers JSON compatibles avec le système d'import/export d'ArguMentor. Le format JSON suit le schéma version 1.0 et permet d'importer des sujets, affirmations, réfutations, preuves, questions, sources et tags.

## 🎯 Pourquoi utiliser le format JSON ?

Le format JSON d'ArguMentor vous offre des possibilités puissantes :

### 📚 Cas d'usage principaux

1. **Sauvegarder votre travail**
   - Créez des backups de toutes vos analyses
   - Restaurez vos données en cas de changement d'appareil
   - Archivez vos projets terminés

2. **Partager des analyses**
   - Envoyez vos arguments à des collègues pour révision
   - Publiez des corpus d'arguments sur des sujets spécifiques
   - Collaborez avec une équipe sur un même débat

3. **Créer des ressources pédagogiques**
   - Professeurs : Préparez des exercices d'analyse critique
   - Formateurs : Créez des cas d'étude pour vos formations
   - Tuteurs : Partagez des exemples d'arguments bien structurés

4. **Migrer depuis d'autres outils**
   - Importez des données depuis Excel, Google Sheets, ou bases de données
   - Convertissez vos notes existantes en arguments structurés
   - Centralisez vos recherches dispersées

5. **Automatiser la création de contenu**
   - Générez des fichiers JSON par script
   - Intégrez ArguMentor dans votre workflow de recherche
   - Créez des templates réutilisables pour différents types d'analyses

### 💡 Exemple concret

Un chercheur en sciences politiques pourrait :
1. Exporter toutes ses analyses sur les débats électoraux en JSON
2. Partager ce fichier avec son laboratoire
3. Ses collègues importent le fichier et ajoutent leurs propres arguments
4. Le fichier enrichi est réimporté pour fusion des analyses

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
  "title": "Les chats comme animaux de compagnie",
  "summary": "Analyse des avantages et inconvénients des chats comme animaux domestiques",
  "posture": "neutral_critical",
  "tags": ["tag_animaux", "tag_comportement"],
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

| Valeur | Alias supportés | Description |
|--------|-----------------|-------------|
| `"neutral_critical"` | `"neutre_critique"`, `"neutre_critical"` | Neutre mais avec analyse critique (recommandé) |
| `"skeptical"` | `"sceptique"` | Position sceptique |
| `"academic_comparative"` | `"comparatif_academique"` | Analyse comparative académique |

**⚠️ Important** :
- Les valeurs canoniques (en anglais) sont recommandées pour la compatibilité maximale
- Les alias français sont supportés pour rétrocompatibilité
- Les valeurs sont insensibles à la casse
- Si la valeur n'est pas reconnue, `"neutral_critical"` sera utilisé par défaut

---

## 2. Claims (Affirmations)

Une affirmation représente un argument ou une thèse liée à un ou plusieurs sujets.

### Structure

```json
{
  "id": "claim_001",
  "text": "Les chats sont des animaux de compagnie plus indépendants que les chiens",
  "stance": "pro",
  "strength": "high",
  "topics": ["topic_001"],
  "createdAt": "2025-01-15T10:00:00Z",
  "updatedAt": "2025-01-15T10:00:00Z",
  "claimFingerprint": "chats_animaux_independants_chiens"
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

| Valeur | Alias supportés | Description |
|--------|-----------------|-------------|
| `"low"` | - | Argument faible |
| `"med"` | `"medium"` | Argument de force moyenne (recommandé: `"med"`) |
| `"high"` | - | Argument fort |

**⚠️ Important** :
- La valeur canonique pour moyenne est `"med"` (pas `"medium"`)
- Les valeurs sont insensibles à la casse
- Si la valeur n'est pas reconnue, `"med"` sera utilisé par défaut

---

## 3. Rebuttals (Réfutations)

Une réfutation conteste ou critique une affirmation.

### Structure

```json
{
  "id": "rebuttal_001",
  "claimId": "claim_001",
  "text": "Les chats peuvent développer de l'anxiété de séparation comme les chiens",
  "fallacyTag": null,
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
  "type": "study",
  "content": "Étude comportementale 2023 : Les chats peuvent passer 12-16h seuls sans stress, contrairement aux chiens",
  "sourceId": "source_001",
  "quality": "high",
  "createdAt": "2025-01-15T10:00:00Z",
  "updatedAt": "2025-01-15T10:00:00Z"
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
| `updatedAt` | String (ISO 8601) | ✅ Oui | Date de mise à jour |

### Valeurs autorisées pour `type`

| Valeur | Description |
|--------|-------------|
| `"study"` | Étude/recherche scientifique |
| `"stat"` | Donnée statistique |
| `"quote"` | Citation textuelle |
| `"example"` | Exemple concret |

**⚠️ Important** : Si la valeur n'est pas reconnue, `"example"` sera utilisé par défaut.

### Valeurs autorisées pour `quality`

| Valeur | Alias supportés | Description |
|--------|-----------------|-------------|
| `"low"` | - | Qualité faible |
| `"med"` | `"medium"` | Qualité moyenne (recommandé: `"med"`) |
| `"high"` | - | Qualité élevée |

**⚠️ Important** : La valeur canonique pour moyenne est `"med"` (pas `"medium"`).

---

## 5. Questions

Les questions servent à clarifier ou approfondir un sujet ou une affirmation.

### Structure

```json
{
  "id": "question_001",
  "targetId": "topic_001",
  "text": "Quels facteurs influencent le niveau d'indépendance des chats ?",
  "kind": "clarifying",
  "createdAt": "2025-01-15T10:00:00Z",
  "updatedAt": "2025-01-15T10:00:00Z"
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
| `updatedAt` | String (ISO 8601) | ✅ Oui | Date de mise à jour |

### Valeurs autorisées pour `kind`

| Valeur | Description |
|--------|-------------|
| `"socratic"` | Question socratique (méthode d'interrogation) |
| `"clarifying"` | Question de clarification |
| `"challenge"` | Question de contestation |
| `"evidence"` | Question sur les preuves |

**⚠️ Important** : Si la valeur n'est pas reconnue, `"clarifying"` sera utilisé par défaut.

---

## 6. Sources

Les sources documentent l'origine des preuves et citations.

### Structure

```json
{
  "id": "source_001",
  "title": "Journal of Feline Behavior",
  "citation": "Turner, D.C. (2023). Feline Independence and Attachment Patterns",
  "url": "https://www.felinebehavior.org/studies/2023",
  "publisher": "Association Internationale de Comportement Félin",
  "date": "2023",
  "reliabilityScore": 0.85,
  "notes": "Étude comportementale sur 500 chats domestiques",
  "createdAt": "2025-01-15T10:00:00Z",
  "updatedAt": "2025-01-15T10:00:00Z"
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
| `updatedAt` | String (ISO 8601) | ✅ Oui | Date de mise à jour |

---

## 7. Tags

Les tags permettent de catégoriser les sujets.

### Structure

```json
{
  "id": "tag_animaux",
  "label": "Animaux",
  "color": "#4CAF50",
  "createdAt": "2025-01-15T10:00:00Z",
  "updatedAt": "2025-01-15T10:00:00Z"
}
```

### Champs

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `id` | String | ✅ Oui | Identifiant unique du tag |
| `label` | String | ✅ Oui | Libellé du tag (affiché dans l'interface) |
| `color` | String | ❌ Non | Couleur au format hexadécimal (#RRGGBB) |
| `createdAt` | String (ISO 8601) | ✅ Oui | Date de création |
| `updatedAt` | String (ISO 8601) | ✅ Oui | Date de mise à jour |

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
- Pour **Topics, Claims, Rebuttals, Sources** : mise à jour si `updatedAt` de l'import est plus récent
- Pour **Tags, Evidences, Questions** : ignoré (considéré comme doublon)

**⚠️ Important sur les timestamps** :
- Les champs `createdAt` et `updatedAt` sont **obligatoires** pour Topics
- Si absents, l'import échouera avec un message d'erreur clair
- Cette validation garantit que les timestamps originaux sont préservés
- Lors d'une mise à jour, seul `updatedAt` est comparé (le plus récent gagne)

### 2. Doublons par empreinte (fingerprint)

Pour les **Claims**, **Rebuttals** et **Sources**, une empreinte textuelle est utilisée pour détecter les doublons :

#### Comment fonctionne le fingerprint ?

Le fingerprint est un **hash SHA-256** du texte normalisé. Le processus :

1. **Normalisation du texte** :
   - Conversion en minuscules
   - Suppression des accents (é → e, ô → o)
   - Suppression de toute ponctuation
   - Remplacement des espaces multiples par un seul espace
   - Suppression des espaces en début/fin

2. **Génération du hash** :
   - Hash SHA-256 du texte normalisé
   - Troncature aux 16 premiers caractères hexadécimaux
   - Résultat : chaîne de 16 caractères (ex: `"d24aedf8033ec528"`)

#### Exemples de fingerprints

| Texte original | Texte normalisé | Fingerprint |
|----------------|-----------------|-------------|
| `"La Bible enseigne explicitement la doctrine de la Trinité"` | `"la bible enseigne explicitement la doctrine de la trinite"` | `"d24aedf8033ec528"` |
| `"Le terme 'Trinité' n'apparaît jamais dans la Bible"` | `"le terme trinite napparait jamais dans la bible"` | `"a86ebf0262110db2"` |

#### Génération manuelle

Si vous créez un fichier JSON manuellement, vous pouvez :

**Option 1 : Laisser vide** (recommandé)
```json
"claimFingerprint": null
```
ArguMentor génèrera automatiquement le fingerprint lors de l'import.

**Option 2 : Générer vous-même**

En Python :
```python
import hashlib
import unicodedata
import string

def generate_fingerprint(text):
    # Normalisation NFD
    normalized = unicodedata.normalize('NFD', text)
    without_accents = ''.join(c for c in normalized if not unicodedata.combining(c))
    lowercase = without_accents.lower()

    # Suppression ponctuation
    translator = str.maketrans('', '', string.punctuation + '«»''""…–—')
    no_punctuation = lowercase.translate(translator)

    # Collapse whitespace
    normalized_text = ' '.join(no_punctuation.split())

    # SHA-256 hash
    hash_bytes = hashlib.sha256(normalized_text.encode('utf-8')).hexdigest()
    return hash_bytes[:16]

print(generate_fingerprint("La Bible enseigne la Trinité"))
# "d24aedf8033ec528"
```

**⚠️ Attention** : Les fingerprints dans `example_data.json` DOIVENT être des vrais hashes SHA-256, pas des chaînes lisibles comme `"bible_enseigne_doctrine_trinite"`. Sinon, la détection de doublons échouera.

### 3. Quasi-doublons (similarité)

ArguMentor utilise l'**algorithme de distance de Levenshtein** pour détecter les textes similaires.

#### Fonctionnement de la détection de similarité

1. **Normalisation** : Les textes sont normalisés (comme pour les fingerprints)
2. **Calcul de distance** : Distance d'édition (nombre de caractères à modifier)
3. **Ratio de similarité** : `1.0 - (distance / longueur_max)`
4. **Seuil** : Par défaut 0.90 (90% de similarité)

#### Scope de la comparaison

- **Claims** : Comparées seulement si elles partagent au moins un Topic en commun
- **Rebuttals** : Comparées seulement si elles réfutent la même Claim
- **Sources** : Pas de détection de similarité (uniquement par fingerprint)

#### Limites de performance

- Textes > 5000 caractères : Non comparés (retourne similarité = 0.0)
- Optimisation : Complexité O(min(m,n)) en espace

#### Exemple

```
Texte A: "Les chats sont indépendants"
Texte B: "Les chats sont très indépendants"
Distance: 5 caractères
Similarité: ~0.85 (85%)
Résultat: Considérés différents (< 90%)
```

Si le seuil était abaissé à 0.80 (80%), ils seraient détectés comme quasi-doublons.

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

## Edge Cases et Comportements Spéciaux

Cette section documente les comportements lors de situations particulières à l'import.

### 1. Références manquantes

#### Evidence avec sourceId inexistant

Si une Evidence référence un `sourceId` qui n'existe pas :
```json
{
  "id": "evidence_001",
  "claimId": "claim_001",
  "sourceId": "source_999",  // ❌ N'existe pas
  "content": "...",
  "type": "study",
  "quality": "high"
}
```

**Comportement** :
- L'import échoue avec le message : `"Evidence 'evidence_001': Referenced source 'source_999' not found. Import the source first or remove the sourceId."`
- L'Evidence n'est PAS importée
- Les autres entités valides sont importées normalement

**Solution** :
- Importer la Source d'abord
- Ou retirer le champ `sourceId` (le laisser vide/null)

#### Rebuttal avec claimId inexistant

```json
{
  "id": "rebuttal_001",
  "claimId": "claim_999",  // ❌ N'existe pas
  "text": "..."
}
```

**Comportement** :
- Erreur : `"Rebuttal 'rebuttal_001': Referenced claim 'claim_999' not found. Import the claim first or verify the claimId."`
- Le Rebuttal n'est PAS importé

#### Question avec targetId inexistant

```json
{
  "id": "question_001",
  "targetId": "topic_999",  // ❌ N'existe pas
  "text": "..."
}
```

**Comportement** :
- Erreur : `"Question 'question_001': Referenced target 'topic_999' not found. The targetId must reference an existing Topic or Claim. Import the target first or verify the targetId."`
- La Question n'est PAS importée

### 2. Validation des valeurs

#### reliabilityScore hors limites

```json
{
  "id": "source_001",
  "title": "...",
  "reliabilityScore": 150  // ❌ Doit être entre 0.0 et 1.0
}
```

**Comportement** :
- Erreur : `"Source 'source_001': reliabilityScore must be between 0.0 and 1.0, got 150"`
- La Source n'est PAS importée
- Valeurs invalides : nombres négatifs, > 1.0, NaN, Infinity

**Valeurs acceptables** : `0.0`, `0.5`, `0.85`, `1.0`, ou `null`

#### Timestamps manquants

Pour les Topics, `createdAt` et `updatedAt` sont **obligatoires** :

```json
{
  "id": "topic_001",
  "title": "...",
  "summary": "...",
  "posture": "neutral_critical",
  "createdAt": null,  // ❌ Obligatoire
  "updatedAt": null   // ❌ Obligatoire
}
```

**Comportement** :
- Erreur : `"Topic 'topic_001': createdAt is required for data integrity"`
- Le Topic n'est PAS importé

**Solution** : Toujours fournir des timestamps valides ISO 8601.

### 3. Comportement lors des mises à jour

#### Mise à jour basée sur timestamp

Quand un élément existe déjà (même `id`), la mise à jour dépend de `updatedAt` :

**Scénario 1 : Import plus récent**
```
DB existante:   { id: "claim_001", updatedAt: "2025-01-10T10:00:00Z" }
Import:         { id: "claim_001", updatedAt: "2025-01-15T10:00:00Z" }
Résultat: ✅ Mise à jour (import plus récent)
```

**Scénario 2 : Import plus ancien**
```
DB existante:   { id: "claim_001", updatedAt: "2025-01-15T10:00:00Z" }
Import:         { id: "claim_001", updatedAt: "2025-01-10T10:00:00Z" }
Résultat: ⏭️ Ignoré (DB plus récente)
```

**⚠️ Important** :
- La comparaison utilise `java.time.Instant` pour gérer correctement les fuseaux horaires
- Si le parsing échoue, fallback sur comparaison lexicographique de chaînes
- `createdAt` n'est JAMAIS modifié lors d'une mise à jour

### 4. Gestion des erreurs I/O

#### Permissions de fichier

Si l'application n'a pas les permissions d'écriture :

**Erreur** : `"Permission denied: /path/to/file.json. Check app permissions."`

**Solutions** :
- Utiliser Storage Access Framework (SAF) pour sélectionner un emplacement autorisé
- Vérifier les permissions dans Paramètres > Apps > ArguMentor > Permissions

#### Espace disque insuffisant

Si le disque est plein lors de l'export :

**Erreur** : `"I/O error during export: No space left on device. Check available storage space."`

**Solution** : Libérer de l'espace ou choisir un autre emplacement

#### Fichier corrompu

Si le JSON est mal formaté :

**Erreur** : `"com.google.gson.JsonSyntaxException: Expected ... but was ..."`

**Solutions** :
- Valider le JSON avec jsonlint.com
- Vérifier les virgules, guillemets et accolades
- S'assurer que le fichier est en UTF-8

### 5. Cascading Deletes (Suppressions en cascade)

⚠️ **ArguMentor ne fait PAS de suppression en cascade automatique lors des imports**.

Cependant, lors de la suppression manuelle dans l'app :

#### Suppression d'un Topic

Quand vous supprimez un Topic :
- Les **Claims** associées ne sont PAS supprimées
- Les Claims perdent seulement la référence à ce Topic dans leur liste `topics`
- Si une Claim n'a plus aucun Topic, elle devient orpheline (mais reste dans la DB)

#### Suppression d'une Claim

Quand vous supprimez une Claim :
- Les **Rebuttals** associées sont supprimées
- Les **Evidences** associées sont supprimées
- Les **Questions** ciblant cette Claim sont supprimées
- Les **Sources** référencées ne sont PAS supprimées

#### Suppression d'une Source

Quand vous supprimez une Source :
- Les **Evidences** qui y font référence perdent leur `sourceId`
- Les Evidences ne sont PAS supprimées

### 6. Ordre d'import recommandé

Pour éviter les erreurs de références, importez dans cet ordre :

1. **Tags** (indépendants)
2. **Sources** (indépendantes)
3. **Topics** (peuvent référencer Tags)
4. **Claims** (doivent référencer Topics)
5. **Rebuttals** (doivent référencer Claims)
6. **Evidences** (doivent référencer Claims, peuvent référencer Sources)
7. **Questions** (doivent référencer Topics ou Claims)

**Note** : ArguMentor importe automatiquement dans cet ordre, même si votre JSON les liste différemment.

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
      "title": "Les chats comme animaux de compagnie",
      "summary": "Analyse des avantages et inconvénients des chats comme animaux domestiques",
      "posture": "neutral_critical",
      "tags": [],
      "createdAt": "2025-01-15T09:00:00Z",
      "updatedAt": "2025-01-15T09:00:00Z"
    }
  ],
  "claims": [
    {
      "id": "claim_001",
      "text": "Les chats sont des animaux de compagnie plus indépendants que les chiens",
      "stance": "pro",
      "strength": "med",
      "topics": ["topic_001"],
      "createdAt": "2025-01-15T10:00:00Z",
      "updatedAt": "2025-01-15T10:00:00Z"
    }
  ],
  "rebuttals": [
    {
      "id": "rebuttal_001",
      "claimId": "claim_001",
      "text": "Les chats peuvent développer de l'anxiété de séparation comme les chiens",
      "createdAt": "2025-01-15T10:30:00Z",
      "updatedAt": "2025-01-15T10:30:00Z"
    }
  ],
  "evidences": [
    {
      "id": "evidence_001",
      "claimId": "claim_001",
      "type": "study",
      "content": "Étude comportementale 2023 : Les chats peuvent passer 12-16h seuls sans stress",
      "quality": "high",
      "createdAt": "2025-01-15T11:00:00Z",
      "updatedAt": "2025-01-15T11:00:00Z"
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
