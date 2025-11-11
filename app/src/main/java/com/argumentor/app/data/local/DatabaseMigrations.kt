package com.argumentor.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.argumentor.app.data.model.getCurrentIsoTimestamp

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
            val currentTimestamp = getCurrentIsoTimestamp()

            // Add timestamps to tags table
            db.execSQL("ALTER TABLE tags ADD COLUMN createdAt TEXT NOT NULL DEFAULT '$currentTimestamp'")
            db.execSQL("ALTER TABLE tags ADD COLUMN updatedAt TEXT NOT NULL DEFAULT '$currentTimestamp'")

            // Add updatedAt to evidences table
            db.execSQL("ALTER TABLE evidences ADD COLUMN updatedAt TEXT NOT NULL DEFAULT '$currentTimestamp'")

            // Add updatedAt to sources table
            db.execSQL("ALTER TABLE sources ADD COLUMN updatedAt TEXT NOT NULL DEFAULT '$currentTimestamp'")

            // Add updatedAt to questions table
            db.execSQL("ALTER TABLE questions ADD COLUMN updatedAt TEXT NOT NULL DEFAULT '$currentTimestamp'")

            // Update Topic.Posture enum values for backward compatibility
            // Old: neutral_critique, sceptique, comparatif_academique
            // New: neutral_critical, skeptical, academic_comparative
            // The fromString method handles both old and new values, so existing data remains compatible
            db.execSQL("UPDATE topics SET posture = 'neutral_critical' WHERE posture = 'neutral_critique'")
            db.execSQL("UPDATE topics SET posture = 'skeptical' WHERE posture = 'sceptique'")
            db.execSQL("UPDATE topics SET posture = 'academic_comparative' WHERE posture = 'comparatif_academique'")
        }
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
     * All migrations in order.
     */
    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4
    )
}
