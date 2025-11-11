# 📊 STATUT DES CORRECTIONS - ArguMentor

**Date** : 2025-11-15
**Branche** : work

---

## ✅ CORRECTIONS EFFECTUÉES

### 🔴 Critiques (8/8 complétées)

1. ✅ **Type incohérent pour fallacy**
   - `Rebuttal.fallacyTag` → `fallacyIds: List<String>` (Migration 5→6 + refactoring complet)
   - Commit: `54736fa`

2. ✅ **Méthodes en double dans DAOs**
   - `getEvidenceForClaim` remplacée par `getEvidencesByClaimIdSync`
   - `getRebuttalsForClaim` remplacée par `getRebuttalsByClaimIdSync`
   - Commit: `f9ae28a`

3. ✅ **Nommage DAO cohérent**
   - `getAllEvidence*` → `getAllEvidences*`
   - Commit: `6452e47`

4. ✅ **Ordre de tri uniformisé**
   - DAOs `Evidence`, `Question`, `Source` triés par `updatedAt DESC` (requêtes standard et FTS).

5. ✅ **Support FTS généralisé**
   - Tables `topics_fts`, `evidences_fts`, `tags_fts` + fallbacks LIKE.
   - Repositories appuyés sur `searchWithFtsFallback`.

6. ✅ **Ordre tri FTS Sources**
   - Requête FTS `SourceDao` ordonnée par `updatedAt DESC`.

7. ✅ **Méthodes observe*ById disponibles**
   - Implémentées sur Evidence, Question, Rebuttal + intégration repository.

8. ✅ **Timestamps hérités différenciés**
   - Migration 1→2 séquence désormais `createdAt` et `updatedAt` pour chaque enregistrement legacy (`DatabaseMigrations.MIGRATION_1_2`).

---

## 📋 PROCHAINES ÉTAPES RECOMMANDÉES

1. **Exporter les schémas Room actualisés**
   - Lancer `./gradlew :app:kspDebugKotlin` sur un poste disposant du SDK Android pour rafraîchir `app/schemas/` après les migrations 6→9.

2. **Automatiser le suivi documentaire**
   - Aligner `AUDIT_REPORT.md` / `CORRECTIONS_STATUS.md` via une tâche CI ou un script afin d'éviter les écarts entre code et rapports.

---

**Statut général** : ✅ Corrections critiques appliquées. Reste à outiller l'export Room et l'automatisation documentaire.
