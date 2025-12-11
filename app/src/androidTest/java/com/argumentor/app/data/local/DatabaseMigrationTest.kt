package com.argumentor.app.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented tests for database migrations.
 *
 * These tests verify that:
 * 1. Each migration transforms the schema correctly
 * 2. Data is preserved during migrations
 * 3. No data corruption occurs
 */
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private val testDbName = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ArguMentorDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2_addsTimestampColumns() {
        // Create database with version 1 schema
        helper.createDatabase(testDbName, 1).apply {
            // Insert test data with version 1 schema
            execSQL("""
                INSERT INTO topics (id, title, summary, posture, tags, createdAt, updatedAt)
                VALUES ('topic-1', 'Test Topic', 'Summary', 'neutral_critique', '[]', '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z')
            """)

            execSQL("""
                INSERT INTO tags (id, label, color)
                VALUES ('tag-1', 'Test Tag', null)
            """)

            close()
        }

        // Run migration
        val db = helper.runMigrationsAndValidate(testDbName, 2, true, DatabaseMigrations.MIGRATION_1_2)

        // Verify new columns exist and have values
        db.query("SELECT createdAt, updatedAt FROM tags WHERE id = 'tag-1'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            val createdAt = cursor.getString(0)
            val updatedAt = cursor.getString(1)
            assertThat(createdAt).isNotEmpty()
            assertThat(updatedAt).isNotEmpty()
        }

        // Verify posture was migrated
        db.query("SELECT posture FROM topics WHERE id = 'topic-1'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            val posture = cursor.getString(0)
            assertThat(posture).isEqualTo("neutral_critical")
        }

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate2To3_addsClaimFingerprintIndex() {
        helper.createDatabase(testDbName, 2).apply {
            execSQL("""
                INSERT INTO claims (id, text, topics, stance, strength, claimFingerprint, fallacyIds, createdAt, updatedAt)
                VALUES ('claim-1', 'Test Claim', '["topic-1"]', 'PRO', 'MEDIUM', 'abc123', '[]', '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z')
            """)
            close()
        }

        val db = helper.runMigrationsAndValidate(testDbName, 3, true, DatabaseMigrations.MIGRATION_2_3)

        // Verify index exists by checking PRAGMA index_list
        db.query("PRAGMA index_list(claims)").use { cursor ->
            var foundIndex = false
            while (cursor.moveToNext()) {
                val indexName = cursor.getString(1)
                if (indexName.contains("claimFingerprint")) {
                    foundIndex = true
                    break
                }
            }
            assertThat(foundIndex).isTrue()
        }

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate3To4_createsSourcesFts() {
        helper.createDatabase(testDbName, 3).apply {
            execSQL("""
                INSERT INTO sources (id, title, citation, publisher, date, url, type, reliabilityScore, updatedAt)
                VALUES ('source-1', 'Test Source', 'Test Citation', 'Publisher', '2024', null, 'ACADEMIC', 0.8, '2024-01-01T00:00:00Z')
            """)
            close()
        }

        val db = helper.runMigrationsAndValidate(testDbName, 4, true, DatabaseMigrations.MIGRATION_3_4)

        // Verify FTS table was created and populated
        db.query("SELECT * FROM sources_fts WHERE sources_fts MATCH 'Test'").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
        }

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate4To5_addsFallacyIdsToClaimsTable() {
        helper.createDatabase(testDbName, 4).apply {
            close()
        }

        val db = helper.runMigrationsAndValidate(testDbName, 5, true, DatabaseMigrations.MIGRATION_4_5)

        // Verify the column exists by inserting a new claim
        db.execSQL("""
            INSERT INTO claims (id, text, topics, stance, strength, claimFingerprint, fallacyIds, createdAt, updatedAt)
            VALUES ('claim-1', 'Test', '[]', 'PRO', 'MEDIUM', 'fp', '["ad_hominem"]', '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z')
        """)

        db.query("SELECT fallacyIds FROM claims WHERE id = 'claim-1'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            val fallacyIds = cursor.getString(0)
            assertThat(fallacyIds).isEqualTo("[\"ad_hominem\"]")
        }

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate5To6_renamesFallacyTagToFallacyIds() {
        helper.createDatabase(testDbName, 5).apply {
            // Insert with old schema (fallacyTag column)
            execSQL("""
                INSERT INTO rebuttals (id, claimId, text, fallacyTag, createdAt, updatedAt)
                VALUES ('rebuttal-1', 'claim-1', 'Test Rebuttal', 'straw_man', '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z')
            """)
            execSQL("""
                INSERT INTO rebuttals (id, claimId, text, fallacyTag, createdAt, updatedAt)
                VALUES ('rebuttal-2', 'claim-1', 'Another Rebuttal', null, '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z')
            """)
            close()
        }

        val db = helper.runMigrationsAndValidate(testDbName, 6, true, DatabaseMigrations.MIGRATION_5_6)

        // Verify data was migrated correctly
        db.query("SELECT fallacyIds FROM rebuttals WHERE id = 'rebuttal-1'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            val fallacyIds = cursor.getString(0)
            assertThat(fallacyIds).isEqualTo("[\"straw_man\"]")
        }

        db.query("SELECT fallacyIds FROM rebuttals WHERE id = 'rebuttal-2'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            val fallacyIds = cursor.getString(0)
            assertThat(fallacyIds).isEqualTo("[]")
        }

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate6To7_createsTopicsFts() {
        helper.createDatabase(testDbName, 6).apply {
            execSQL("""
                INSERT INTO topics (id, title, summary, posture, tags, createdAt, updatedAt)
                VALUES ('topic-1', 'Searchable Topic', 'Findable Summary', 'neutral_critical', '[]', '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z')
            """)
            close()
        }

        val db = helper.runMigrationsAndValidate(testDbName, 7, true, DatabaseMigrations.MIGRATION_6_7)

        // Verify FTS table was created and populated
        db.query("SELECT * FROM topics_fts WHERE topics_fts MATCH 'Searchable'").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
        }

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate7To8_createsEvidencesFts() {
        helper.createDatabase(testDbName, 7).apply {
            execSQL("""
                INSERT INTO evidences (id, claimId, content, type, sourceId, quality, createdAt, updatedAt)
                VALUES ('evidence-1', 'claim-1', 'Scientific evidence content', 'STUDY', null, 'HIGH', '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z')
            """)
            close()
        }

        val db = helper.runMigrationsAndValidate(testDbName, 8, true, DatabaseMigrations.MIGRATION_7_8)

        // Verify FTS table was created and populated
        db.query("SELECT * FROM evidences_fts WHERE evidences_fts MATCH 'Scientific'").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
        }

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate8To9_createsTagsFts() {
        helper.createDatabase(testDbName, 8).apply {
            execSQL("""
                INSERT INTO tags (id, label, color, createdAt, updatedAt)
                VALUES ('tag-1', 'Philosophy', '#FF0000', '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z')
            """)
            close()
        }

        val db = helper.runMigrationsAndValidate(testDbName, 9, true, DatabaseMigrations.MIGRATION_8_9)

        // Verify FTS table was created and populated
        db.query("SELECT * FROM tags_fts WHERE tags_fts MATCH 'Philosophy'").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
        }

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate9To10_createsFallaciesTable() {
        helper.createDatabase(testDbName, 9).apply {
            close()
        }

        val db = helper.runMigrationsAndValidate(testDbName, 10, true, DatabaseMigrations.MIGRATION_9_10)

        // Verify fallacies table was created with 30 default fallacies
        db.query("SELECT COUNT(*) FROM fallacies").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            val count = cursor.getInt(0)
            assertThat(count).isEqualTo(30)
        }

        // Verify specific fallacy exists
        db.query("SELECT name FROM fallacies WHERE id = 'ad_hominem'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            val name = cursor.getString(0)
            assertThat(name).isEqualTo("Ad Hominem")
        }

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrateAll_preservesDataIntegrity() {
        // Create database at version 1
        helper.createDatabase(testDbName, 1).apply {
            execSQL("""
                INSERT INTO topics (id, title, summary, posture, tags, createdAt, updatedAt)
                VALUES ('topic-1', 'Original Topic', 'Original Summary', 'neutral_critique', '["tag1"]', '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z')
            """)
            close()
        }

        // Run all migrations
        val db = helper.runMigrationsAndValidate(
            testDbName, 10, true,
            *DatabaseMigrations.ALL_MIGRATIONS
        )

        // Verify original data is preserved
        db.query("SELECT title, summary FROM topics WHERE id = 'topic-1'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Original Topic")
            assertThat(cursor.getString(1)).isEqualTo("Original Summary")
        }

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun fullMigrationPath_canOpenWithRoom() {
        // Create database at version 1
        helper.createDatabase(testDbName, 1).apply {
            close()
        }

        // Run all migrations
        helper.runMigrationsAndValidate(
            testDbName, 10, true,
            *DatabaseMigrations.ALL_MIGRATIONS
        ).close()

        // Open with Room to verify schema is valid
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.databaseBuilder(
            context,
            ArguMentorDatabase::class.java,
            testDbName
        )
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
            .build()

        // Verify database is functional
        val fallacies = db.fallacyDao()
        assertThat(fallacies).isNotNull()

        db.close()
    }
}
