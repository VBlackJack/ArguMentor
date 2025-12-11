package com.argumentor.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.argumentor.app.data.model.getCurrentIsoTimestamp
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Database migrations for ArguMentor.
 */
object DatabaseMigrations {

    /**
     * Migration from version 1 to 2.
     *
     * Changes:
     * - Added createdAt and updatedAt columns to tags table
     * - Added updatedAt column to evidences table
     * - Added updatedAt column to sources table
     * - Added updatedAt column to questions table
     * - Topic.Posture enum values renamed (backward compatible via fromString)
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // SECURITY FIX: Use a safe constant default value for timestamps
            // The actual timestamps will be set by assignSequentialTimestamps() using parameterized queries
            val safeDefaultTimestamp = "2000-01-01T00:00:00Z"

            // Add timestamps to tags table
            db.execSQL("ALTER TABLE tags ADD COLUMN createdAt TEXT NOT NULL DEFAULT '$safeDefaultTimestamp'")
            db.execSQL("ALTER TABLE tags ADD COLUMN updatedAt TEXT NOT NULL DEFAULT '$safeDefaultTimestamp'")

            // Add updatedAt to evidences table
            db.execSQL("ALTER TABLE evidences ADD COLUMN updatedAt TEXT NOT NULL DEFAULT '$safeDefaultTimestamp'")

            // Add updatedAt to sources table
            db.execSQL("ALTER TABLE sources ADD COLUMN updatedAt TEXT NOT NULL DEFAULT '$safeDefaultTimestamp'")

            // Add updatedAt to questions table
            db.execSQL("ALTER TABLE questions ADD COLUMN updatedAt TEXT NOT NULL DEFAULT '$safeDefaultTimestamp'")

            assignSequentialTimestamps(db)

            // Update Topic.Posture enum values for backward compatibility
            // Old: neutral_critique, sceptique, comparatif_academique
            // New: neutral_critical, skeptical, academic_comparative
            // The fromString method handles both old and new values, so existing data remains compatible
            db.execSQL("UPDATE topics SET posture = 'neutral_critical' WHERE posture = 'neutral_critique'")
            db.execSQL("UPDATE topics SET posture = 'skeptical' WHERE posture = 'sceptique'")
            db.execSQL("UPDATE topics SET posture = 'academic_comparative' WHERE posture = 'comparatif_academique'")
        }
    }

    private fun assignSequentialTimestamps(db: SupportSQLiteDatabase) {
        val baseTime = System.currentTimeMillis()
        var offset = 0L
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun nextTimestamp(): String {
            val timestamp = formatter.format(Date(baseTime + offset))
            offset += 1
            return timestamp
        }

        /**
         * BUG-012: Added error handling for database migration queries.
         * Ensures migration doesn't silently fail on corrupted databases.
         */
        fun updateTable(
            table: String,
            idColumn: String = "id",
            hasCreatedColumn: Boolean
        ) {
            try {
                db.query("SELECT $idColumn FROM $table ORDER BY rowid").use { cursor ->
                    if (cursor == null) {
                        Timber.e("Failed to query table $table for migration")
                        return
                    }
                    while (cursor.moveToNext()) {
                        val id = cursor.getString(0)
                        val bindings = mutableListOf<Any>()
                        val assignments = mutableListOf<String>()

                        if (hasCreatedColumn) {
                            assignments += "createdAt = ?"
                            bindings += nextTimestamp()
                        }

                        assignments += "updatedAt = ?"
                        bindings += nextTimestamp()

                        bindings += id

                        db.execSQL(
                            "UPDATE $table SET ${assignments.joinToString(", ")} WHERE $idColumn = ?",
                            bindings.toTypedArray()
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error migrating table $table")
                throw e  // Re-throw to fail the migration properly
            }
        }

        updateTable(table = "tags", hasCreatedColumn = true)
        updateTable(table = "evidences", hasCreatedColumn = false)
        updateTable(table = "sources", hasCreatedColumn = false)
        updateTable(table = "questions", hasCreatedColumn = false)
    }

    /**
     * Migration from version 2 to 3.
     *
     * Changes:
     * - Added index on claimFingerprint column in claims table for faster duplicate detection
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create index on claimFingerprint for faster duplicate detection lookups
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_claims_claimFingerprint` ON `claims` (`claimFingerprint`)")
        }
    }

    /**
     * Migration from version 3 to 4.
     *
     * Changes:
     * - Added SourceFts table for full-text search on sources (title and citation)
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create FTS4 virtual table for sources
            db.execSQL("""
                CREATE VIRTUAL TABLE IF NOT EXISTS `sources_fts`
                USING fts4(content=`sources`, title, citation)
            """)

            // Populate FTS table with existing data
            db.execSQL("""
                INSERT INTO sources_fts(docid, title, citation)
                SELECT rowid, title, citation FROM sources
            """)
        }
    }

    /**
     * Migration from version 4 to 5.
     *
     * Changes:
     * - Added fallacyIds column to claims table for linking claims to identified fallacies
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add fallacyIds column to claims table (stored as JSON array, similar to topics)
            db.execSQL("ALTER TABLE claims ADD COLUMN fallacyIds TEXT NOT NULL DEFAULT '[]'")
        }
    }

    /**
     * Migration from version 5 to 6.
     *
     * Changes:
     * - Renamed fallacyTag to fallacyIds in rebuttals table for consistency with claims
     * - Converted from nullable String to JSON array of strings
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Rename fallacyTag column to fallacyIds and convert single value to array
            // SQLite doesn't support renaming columns directly in older versions,
            // so we need to recreate the table

            // Step 1: Create new table with updated schema
            db.execSQL("""
                CREATE TABLE rebuttals_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    claimId TEXT NOT NULL,
                    text TEXT NOT NULL,
                    fallacyIds TEXT NOT NULL DEFAULT '[]',
                    createdAt TEXT NOT NULL,
                    updatedAt TEXT NOT NULL,
                    FOREIGN KEY (claimId) REFERENCES claims(id) ON DELETE CASCADE
                )
            """)

            // Step 2: Create index on claimId
            db.execSQL("CREATE INDEX index_rebuttals_new_claimId ON rebuttals_new(claimId)")

            // Step 3: Copy data from old table to new table
            // Convert single fallacyTag to array format [fallacyTag] if not null, else []
            db.execSQL("""
                INSERT INTO rebuttals_new (id, claimId, text, fallacyIds, createdAt, updatedAt)
                SELECT id, claimId, text,
                    CASE
                        WHEN fallacyTag IS NULL OR fallacyTag = '' THEN '[]'
                        ELSE '["' || fallacyTag || '"]'
                    END,
                    createdAt, updatedAt
                FROM rebuttals
            """)

            // Step 4: Drop old table
            db.execSQL("DROP TABLE rebuttals")

            // Step 5: Rename new table to original name
            db.execSQL("ALTER TABLE rebuttals_new RENAME TO rebuttals")
        }
    }

    /**
     * Migration from version 6 to 7.
     *
     * Changes:
     * - Added TopicFts table for full-text search on topics
     */
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

    /**
     * Migration from version 7 to 8.
     *
     * Changes:
     * - Added EvidenceFts table for full-text search on evidences
     */
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

    /**
     * Migration from version 8 to 9.
     *
     * Changes:
     * - Added TagFts table for full-text search on tags
     */
    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create FTS4 virtual table for tags
            db.execSQL("""
                CREATE VIRTUAL TABLE IF NOT EXISTS `tags_fts`
                USING fts4(content=`tags`, label)
            """)

            // Populate FTS table with existing data
            db.execSQL("""
                INSERT INTO tags_fts(docid, label)
                SELECT rowid, label FROM tags
            """)
        }
    }

    /**
     * Migration from version 9 to 10.
     *
     * Changes:
     * - Added Fallacy table for CRUD operations on logical fallacies
     * - Pre-loads the 30 default fallacies from FallacyCatalog
     */
    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create fallacies table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `fallacies` (
                    `id` TEXT PRIMARY KEY NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `example` TEXT NOT NULL,
                    `category` TEXT NOT NULL DEFAULT '',
                    `isCustom` INTEGER NOT NULL DEFAULT 0,
                    `createdAt` TEXT NOT NULL,
                    `updatedAt` TEXT NOT NULL
                )
            """)

            // Pre-load default fallacies
            // Using hardcoded IDs and English names for database consistency
            // The UI will fetch localized names from strings.xml via FallacyCatalog when needed
            val currentTime = getCurrentIsoTimestamp()

            val defaultFallacies = listOf(
                "ad_hominem" to "Ad Hominem",
                "straw_man" to "Straw Man",
                "appeal_to_ignorance" to "Appeal to Ignorance",
                "post_hoc" to "Post Hoc",
                "false_dilemma" to "False Dilemma",
                "begging_question" to "Begging the Question",
                "slippery_slope" to "Slippery Slope",
                "postdiction" to "Postdiction",
                "cherry_picking" to "Cherry Picking",
                "appeal_to_tradition" to "Appeal to Tradition",
                "appeal_to_authority" to "Appeal to Authority",
                "appeal_to_popularity" to "Appeal to Popularity",
                "circular_reasoning" to "Circular Reasoning",
                "tu_quoque" to "Tu Quoque",
                "hasty_generalization" to "Hasty Generalization",
                "red_herring" to "Red Herring",
                "no_true_scotsman" to "No True Scotsman",
                "loaded_question" to "Loaded Question",
                "appeal_to_emotion" to "Appeal to Emotion",
                "appeal_to_nature" to "Appeal to Nature",
                "false_equivalence" to "False Equivalence",
                "burden_of_proof" to "Burden of Proof",
                "texas_sharpshooter" to "Texas Sharpshooter",
                "middle_ground" to "Middle Ground",
                "anecdotal" to "Anecdotal",
                "composition" to "Composition",
                "division" to "Division",
                "genetic_fallacy" to "Genetic Fallacy",
                "bandwagon" to "Bandwagon",
                "appeal_to_fear" to "Appeal to Fear"
            )

            // Insert default fallacies with placeholder descriptions
            // The actual localized content will be fetched from strings.xml in the app
            defaultFallacies.forEach { (id, name) ->
                db.execSQL("""
                    INSERT INTO fallacies (id, name, description, example, category, isCustom, createdAt, updatedAt)
                    VALUES (?, ?, ?, ?, ?, 0, ?, ?)
                """, arrayOf(
                    id,
                    name,
                    "See string resource: fallacy_${id}_description",
                    "See string resource: fallacy_${id}_example",
                    "",
                    currentTime,
                    currentTime
                ))
            }

            Timber.d("Migration 9→10: Created fallacies table and inserted ${defaultFallacies.size} default fallacies")
        }
    }

    /**
     * All migrations in order.
     */
    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9,
        MIGRATION_9_10
    )
}
