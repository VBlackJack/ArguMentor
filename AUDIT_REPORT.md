# 🔍 RAPPORT D'ANALYSE DE CODE - ArguMentor

**Date d'audit** : 2025-11-15
**Version auditée** : 1.2.0 (versionCode: 3)
**Branche** : work
**Analyseur** : Expert Architecte Logiciel Senior

---

## 📋 RÉSUMÉ EXÉCUTIF

- **Langage détecté** : Kotlin 100% (1.9.20)
- **Type d'application** : Application mobile native Android (minSdk 24, targetSdk 34)
- **Framework principal** : Jetpack Compose + Material Design 3
- **Architecture** : Clean Architecture + MVVM
- **Score global** : **8.8**/10
- **Priorité d'action** : **🟡 MOYENNE** (optimisations ponctuelles possibles, pas de blocant sécurité)

### Vue d'ensemble

L'application ArguMentor a intégré l'essentiel des recommandations de sécurité et de performance du précédent audit. La chaîne de build Android applique désormais l'obfuscation, les sauvegardes système sont neutralisées, les algorithmes sensibles sont bornés et les migrations Room couvrent toutes les fonctionnalités FTS requises. Le socle reste moderne et cohérent (Compose, Hilt, Room, Coroutines) avec une architecture modulaire claire. Les actions restantes concernent surtout la maintenance continue (schémas Room exportés, migrations historiques à raffiner).

---

## ✅ CONTRÔLES DE CONFORMITÉ VALIDÉS

### 1. Obfuscation et réduction d'APK actives
- **Fichier** : `app/build.gradle.kts`
- **Constat** : le build type `release` active `isMinifyEnabled = true` et `isShrinkResources = true`, couplé au profil ProGuard optimisé.
- **Impact** : logique métier protégée, binaire plus compact.

### 2. Sauvegardes système désactivées
- **Fichier** : `app/src/main/AndroidManifest.xml`
- **Constat** : `android:allowBackup="false"` avec règles d'extraction et de sauvegarde explicites.
- **Impact** : prévention de l'exfiltration ADB / sauvegarde involontaire des données sensibles.

### 3. Algorithme de similarité borné
- **Fichier** : `app/src/main/java/com/argumentor/app/util/FingerprintUtils.kt`
- **Constat** : Levenshtein plafonné à 5 000 caractères, complexité mémoire optimisée O(min(n, m)), gestion des dépassements par exception.
- **Impact** : atténuation des attaques DoS via import de JSON volumineux, meilleure tenue mémoire.

### 4. Chaîne d'import/export robuste
- **Fichier** : `app/src/main/java/com/argumentor/app/data/repository/ImportExportRepository.kt`
- **Constat** : anti-duplication combinant identifiants, empreintes (`FingerprintUtils`) et similarité textuelle avant insertion.
- **Impact** : import JSON fiable même avec jeux de données déjà présents.

### 5. Versionning applicatif aligné
- **Fichier** : `app/build.gradle.kts`
- **Constat** : `versionName = "1.2.0"` / `versionCode = 3` reflétés dans les écrans applicatifs.
- **Impact** : cohérence produit / store / documentation.

### 6. Migrations Room complètes (1→9)
- **Fichier** : `app/src/main/java/com/argumentor/app/data/local/DatabaseMigrations.kt`
- **Constat** : migrations `MIGRATION_6_7`, `MIGRATION_7_8`, `MIGRATION_8_9` créent les tables FTS (topics, evidences, tags) et pré-remplissent les données ; `ALL_MIGRATIONS` référence l'ensemble.
- **Impact** : compatibilité ascendante, recherche plein texte unifiée.

### 7. Ordres de tri uniformisés
- **Fichiers** : DAO `EvidenceDao`, `QuestionDao`, `SourceDao`
- **Constat** : requêtes principales et FTS ordonnées par `updatedAt DESC`.
- **Impact** : expérience utilisateur cohérente sur les listes et la recherche.

### 8. Observabilité fine dans les DAO
- **Fichiers** : `EvidenceDao`, `QuestionDao`, `RebuttalDao`
- **Constat** : méthodes `observe*ById` exposées et réutilisées côté repository.
- **Impact** : écrans Compose alimentés par Flow réactif sur chaque entité.

### 9. Fallback FTS/LIKE mutualisé
- **Fichiers** : `RepositoryExtensions.kt` + repositories métiers
- **Constat** : helper `searchWithFtsFallback` centralise la dégradation contrôlée, toutes les recherches l'utilisent.
- **Impact** : résilience des recherches face aux requêtes FTS invalides.

---

## 🔎 POINTS DE VIGILANCE RESTANTS

1. **Migration 1→2 : timestamps uniques souhaitables**
   - Les colonnes `createdAt` / `updatedAt` sont initialisées avec une valeur identique lors de la migration. Pour refléter l'historique réel, générer des timestamps différenciés par enregistrement.

2. **Export des schémas Room**
   - Re-générer les JSON d'`app/schemas` via `./gradlew :app:kspDebugKotlin` sur un poste équipé du SDK Android afin de conserver un historique complet des migrations.

---

### Conclusion
Les failles critiques signalées par le précédent audit sont désormais corrigées dans la base de code. Le socle Android répond aux attentes de production (sécurité, performances, UX). Il reste conseillé de finaliser la migration historique des timestamps et de mettre à jour régulièrement les exports Room pour conserver une traçabilité complète.
