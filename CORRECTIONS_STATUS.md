# 📊 STATUT DES CORRECTIONS - ArguMentor

**Date** : 2025-11-11
**Branche** : claude/analyze-inconsistencies-011CV1qhT1JeZD5UchKJ2H56

---

## ✅ CORRECTIONS EFFECTUÉES

### 🔴 Critiques (3/8 complétées)

1. ✅ **Type incohérent pour fallacy** - COMPLÉTÉ
   - Rebuttal.fallacyTag: String? → fallacyIds: List<String>
   - Migration 5→6 créée
   - Tous les usages mis à jour
   - Commit: `54736fa`

2. ✅ **Méthodes en double dans DAOs** - COMPLÉTÉ
   - getEvidenceForClaim → getEvidencesByClaimIdSync
   - getRebuttalsForClaim → getRebuttalsByClaimIdSync
   - Commit: `f9ae28a`

3. ✅ **Nommage DAO incohérent** - COMPLÉTÉ
   - getAllEvidence → getAllEvidences
   - getAllEvidenceSync → getAllEvidencesSync
   - Commit: `6452e47`

4. ⏳ **Timestamp unique migrations** - PRIORITÉ HAUTE
   - À FAIRE: Améliorer migration 1→2 pour timestamps différenciés
   - Impact: Moyen (données existantes seulement)

5. ⏳ **Ordre de tri incohérent** - PRIORITÉ HAUTE
   - À FAIRE: Uniformiser tout en `updatedAt DESC`
   - Fichiers: Evidence/Question/SourceDao
   - Impact: Comportement UI

6. ⏳ **Support FTS incohérent** - PRIORITÉ HAUTE
   - À FAIRE: Ajouter FTS à Topic + Evidence
   - À FAIRE: Ajouter LIKE fallback à Source
   - À FAIRE: searchWithFtsFallback dans repositories
   - Impact: Fonctionnalité de recherche

7. ⏳ **Ordre tri FTS Sources** - PRIORITÉ MOYENNE
   - À FAIRE: Changer `ORDER BY title` → `ORDER BY createdAt DESC`
   - Fichier: SourceDao.kt:49

8. ⏳ **Méthodes observe manquantes** - PRIORITÉ MOYENNE
   - À FAIRE: Ajouter observeEvidenceById
   - À FAIRE: Ajouter observeQuestionById
   - À FAIRE: Ajouter observeRebuttalById

---

## 📋 CORRECTIONS RESTANTES DÉTAILLÉES

### 1. Uniformiser l'ordre de tri

**Fichiers à modifier :**

```kotlin
// EvidenceDao.kt - Changer ligne 9, 13, 16, 19, 29
ORDER BY createdAt DESC → ORDER BY updatedAt DESC

// QuestionDao.kt - Changer ligne 9, 13, 16, 19, 47, 55
ORDER BY createdAt DESC → ORDER BY updatedAt DESC

// SourceDao.kt - Changer ligne 9, 13
ORDER BY createdAt DESC → ORDER BY updatedAt DESC

// SourceDao.kt - Changer ligne 49 (FTS)
ORDER BY sources.title → ORDER BY sources.updatedAt DESC
```

**Avantage :** Comportement UI cohérent (items récemment modifiés apparaissent en premier)

---

### 2. Ajouter support FTS complet

#### A. Ajouter LIKE fallback à SourceDao

```kotlin
// SourceDao.kt - Ajouter après searchSourcesFts

/**
 * Fallback search using LIKE (for when FTS query contains invalid operators)
 */
@Query("""
    SELECT * FROM sources
    WHERE title LIKE '%' || :query || '%'
       OR citation LIKE '%' || :query || '%'
    ORDER BY updatedAt DESC
""")
fun searchSourcesLike(query: String): Flow<List<Source>>
```

#### B. Uniformiser SourceRepository

```kotlin
// SourceRepository.kt - Remplacer searchSources

fun searchSources(query: String): Flow<List<Source>> {
    return searchWithFtsFallback(
        query = query,
        ftsSearch = { sourceDao.searchSourcesFts(it) },
        likeSearch = { sourceDao.searchSourcesLike(it) }
    )
}
```

#### C. Ajouter FTS à TopicDao

Nécessite migration 6→7 :

```kotlin
// DatabaseMigrations.kt - Ajouter MIGRATION_6_7

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create FTS4 virtual table for topics
        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS `topics_fts`
            USING fts4(content=`topics`, title, summary)
        """)

        // Populate FTS table with existing data
        db.execSQL("""
            INSERT INTO topics_fts(docid, title, summary)
            SELECT rowid, title, summary FROM topics
        """)
    }
}
```

```kotlin
// TopicDao.kt - Ajouter méthodes FTS

@Query("""
    SELECT topics.* FROM topics
    JOIN topics_fts ON topics.rowid = topics_fts.rowid
    WHERE topics_fts MATCH :query
    ORDER BY updatedAt DESC
""")
fun searchTopicsFts(query: String): Flow<List<Topic>>

@Query("""
    SELECT * FROM topics
    WHERE title LIKE '%' || :query || '%'
       OR summary LIKE '%' || :query || '%'
    ORDER BY updatedAt DESC
""")
fun searchTopicsLike(query: String): Flow<List<Topic>>
```

```kotlin
// TopicRepository.kt - Mettre à jour searchTopics

fun searchTopics(query: String): Flow<List<Topic>> {
    return searchWithFtsFallback(
        query = query,
        ftsSearch = { topicDao.searchTopicsFts(it) },
        likeSearch = { topicDao.searchTopicsLike(it) }
    )
}
```

#### D. Ajouter FTS à EvidenceDao

Nécessite migration 7→8 :

```kotlin
// DatabaseMigrations.kt - Ajouter MIGRATION_7_8

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create FTS4 virtual table for evidences
        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS `evidences_fts`
            USING fts4(content=`evidences`, content)
        """)

        // Populate FTS table with existing data
        db.execSQL("""
            INSERT INTO evidences_fts(docid, content)
            SELECT rowid, content FROM evidences
        """)
    }
}
```

```kotlin
// EvidenceDao.kt - Ajouter méthodes FTS

@Query("""
    SELECT evidences.* FROM evidences
    JOIN evidences_fts ON evidences.rowid = evidences_fts.rowid
    WHERE evidences_fts MATCH :query
    ORDER BY updatedAt DESC
""")
fun searchEvidencesFts(query: String): Flow<List<Evidence>>

@Query("""
    SELECT * FROM evidences
    WHERE content LIKE '%' || :query || '%'
    ORDER BY updatedAt DESC
""")
fun searchEvidencesLike(query: String): Flow<List<Evidence>>
```

```kotlin
// EvidenceRepository.kt - Ajouter searchEvidences

fun searchEvidences(query: String): Flow<List<Evidence>> {
    return searchWithFtsFallback(
        query = query,
        ftsSearch = { evidenceDao.searchEvidencesFts(it) },
        likeSearch = { evidenceDao.searchEvidencesLike(it) }
    )
}
```

---

### 3. Ajouter méthodes observe manquantes

```kotlin
// EvidenceDao.kt - Ajouter

@Query("SELECT * FROM evidences WHERE id = :evidenceId")
fun observeEvidenceById(evidenceId: String): Flow<Evidence?>

// QuestionDao.kt - Ajouter

@Query("SELECT * FROM questions WHERE id = :questionId")
fun observeQuestionById(questionId: String): Flow<Question?>

// RebuttalDao.kt - Ajouter

@Query("SELECT * FROM rebuttals WHERE id = :rebuttalId")
fun observeRebuttalById(rebuttalId: String): Flow<Rebuttal?>
```

---

### 4. Ajouter méthodes de suppression manquantes

```kotlin
// EvidenceDao.kt - Ajouter

@Query("DELETE FROM evidences WHERE claimId = :claimId")
suspend fun deleteEvidencesByClaimId(claimId: String)

// RebuttalDao.kt - Ajouter

@Query("DELETE FROM rebuttals WHERE claimId = :claimId")
suspend fun deleteRebuttalsByClaimId(claimId: String)
```

---

### 5. Supprimer état dupliqué dans HomeViewModel

```kotlin
// HomeViewModel.kt - Supprimer ligne 23

// SUPPRIMER : private val _allTopics = MutableStateFlow<List<Topic>>(emptyList())

// Utiliser uniquement _uiState pour stocker les données
```

---

## 🎯 PRIORISATION DES CORRECTIONS RESTANTES

### Priorité HAUTE (à faire maintenant)
1. ✅ Uniformiser l'ordre de tri → 15 minutes
2. ✅ Ajouter LIKE fallback à Source → 5 minutes
3. ✅ Uniformiser SourceRepository → 2 minutes

### Priorité MOYENNE (peut attendre)
4. ⏳ Ajouter FTS à Topic + migration → 30 minutes
5. ⏳ Ajouter FTS à Evidence + migration → 30 minutes
6. ⏳ Ajouter méthodes observe → 10 minutes
7. ⏳ Ajouter méthodes suppression → 5 minutes

### Priorité BASSE (backlog)
8. ⏳ Supprimer état dupliqué HomeViewModel → 10 minutes
9. ⏳ Corriger timestamp migrations → 15 minutes

---

## 📈 STATISTIQUES

- **Incohérences totales identifiées** : 43
- **Incohérences corrigées** : 3 critiques
- **Incohérences restantes** : 40 (5 critiques, 18 majeures, 17 mineures)
- **Temps estimé corrections HAUTE priorité** : ~20 minutes
- **Temps estimé TOUTES corrections** : ~2-3 heures

---

## 🚀 PROCHAINES ÉTAPES

1. Faire corrections HAUTE priorité (ordre tri, FTS Source)
2. Créer commit pour chaque groupe de corrections
3. Tester que tout compile
4. Push vers la branche
5. Continuer avec priorité MOYENNE si temps disponible

---

**Document généré** : 2025-11-11
**Dernière mise à jour** : 2025-11-11
