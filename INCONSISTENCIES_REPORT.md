# 🔍 RAPPORT D'INCOHÉRENCES – ArguMentor

**Date d'analyse** : 2025-11-11  
**Branche analysée** : work  
**Analyste** : Équipe QA interne

---

## 📊 Résumé exécutif
- 🔴 **Incohérences critiques** : 0
- 🟠 **Incohérences majeures** : 0
- 🟡 **Incohérences mineures** : 1 (export automatique du schéma Room à finaliser sur un poste équipé du SDK Android)

Les divergences identifiées lors du précédent audit sur les champs `fallacyTag`/`fallacyIds`, la documentation JSON et les migrations Room ont été corrigées dans le dépôt.

---

## ✅ Correctifs confirmés

### 1. Alignement des modèles et des exports JSON
- `Claim` et `Rebuttal` exposent exclusivement le champ `fallacyIds` (`List<String>`).
- Les exemples (`example_data.json`) et les guides (`IMPORT_JSON_GUIDE.md`, `JSON_EDITOR_README.md`) reflètent le même format.
- L'éditeur web (`json-editor.html`) supporte désormais l'édition multi-sophismes, propose l'autocomplétion basée sur le catalogue et renseigne systématiquement `updatedAt` lors des créations/modifications.

### 2. Migrations Room complètes jusqu'à la version 9
- Ajout de `MIGRATION_8_9` pour créer la table FTS `tags_fts` et la pré-remplir.
- `DatabaseMigrations.ALL_MIGRATIONS` référence toutes les migrations 1 → 9.

### 3. Compatibilité ascendante lors de l'import JSON
- Les fichiers anciens contenant `fallacyTag` sont automatiquement convertis en `fallacyIds` lors de l'import dans l'éditeur web avec un avertissement dédié.

---

## 🔄 Points restant à exécuter (postes de build uniquement)
- Exporter les schémas Room (versions 5 → 9) en lançant `./gradlew :app:kspDebugKotlin` depuis un poste disposant du SDK Android (Build Tools 34, plate-forme API 34). L'environnement CI de l'audit n'inclut pas ces dépendances, l'opération est donc à relancer localement pour mettre à jour `app/schemas/...`.

---

## 📌 Recommandations
- Ajouter une vérification CI pour garantir que les exports Room sont régénérés dès qu'une migration ou un schéma change.
- Prévoir un rappel de maintenance trimestriel pour valider la liste des sophismes (`FallacyCatalog`) et synchroniser les exemples JSON si de nouveaux IDs apparaissent.

---

**Statut général** : ✅ Corrections appliquées côté code & documentation.  ⚠️ Étape de génération des schémas Room à rejouer sur un poste équipé du SDK Android afin de finaliser l'export automatique.
