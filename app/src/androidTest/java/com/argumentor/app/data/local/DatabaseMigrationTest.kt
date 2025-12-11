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
 *
 * Note: Only testing migrations with available schemas (9→10).
 * Historical schemas (1-8) were not exported and are not available.
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
    fun migrate9To10_createsFallaciesTable() {
        helper.createDatabase(testDbName, 9).apply {
            // Insert test data in v9 schema
            execSQL("""
                INSERT INTO topics (id, title, summary, posture, tags, createdAt, updatedAt)
                VALUES ('topic-1', 'Test Topic', 'Summary', 'neutral_critical', '[]', '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z')
            """)
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

        // Verify original topic data is preserved
        db.query("SELECT title FROM topics WHERE id = 'topic-1'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Test Topic")
        }

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate9To10_canOpenWithRoom() {
        // Create database at version 9
        helper.createDatabase(testDbName, 9).apply {
            close()
        }

        // Run migration
        helper.runMigrationsAndValidate(
            testDbName, 10, true,
            DatabaseMigrations.MIGRATION_9_10
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

    @Test
    @Throws(IOException::class)
    fun migrate9To10_fallaciesHaveCorrectStructure() {
        helper.createDatabase(testDbName, 9).apply {
            close()
        }

        val db = helper.runMigrationsAndValidate(testDbName, 10, true, DatabaseMigrations.MIGRATION_9_10)

        // Verify fallacy structure has all required columns matching the actual schema
        db.query("SELECT id, name, description, example, category, isCustom, createdAt, updatedAt FROM fallacies WHERE id = 'straw_man'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("straw_man")
            assertThat(cursor.getString(1)).isEqualTo("Straw Man")
            assertThat(cursor.getString(2)).isNotEmpty() // description (placeholder text)
            assertThat(cursor.getString(3)).isNotEmpty() // example (placeholder text)
            assertThat(cursor.getString(4)).isEmpty() // category (empty string by default)
            assertThat(cursor.getInt(5)).isEqualTo(0) // isCustom = false
            assertThat(cursor.getString(6)).isNotEmpty() // createdAt
            assertThat(cursor.getString(7)).isNotEmpty() // updatedAt
        }

        db.close()
    }
}
