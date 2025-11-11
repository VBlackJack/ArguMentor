package com.argumentor.app.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.argumentor.app.data.local.ArguMentorDatabase
import com.argumentor.app.data.local.DatabaseMigrations
import com.argumentor.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module for providing database and DAO instances.
 *
 * Security & Performance considerations:
 * - Uses WAL (Write-Ahead Logging) for better concurrent read performance
 * - Auto-close timeout to free resources when database is idle
 * - Fallback migration strategy to handle corrupted databases gracefully
 * - Callbacks for error monitoring and debugging
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val TAG = "DatabaseModule"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ArguMentorDatabase {
        return Room.databaseBuilder(
            context,
            ArguMentorDatabase::class.java,
            ArguMentorDatabase.DATABASE_NAME
        )
            // Add all migrations for smooth upgrades
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)

            // DEP-001: Enable Write-Ahead Logging for better concurrent read performance
            // WAL mode allows concurrent readers while a write is in progress
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)

            // PERF-001: Auto-close database after 10 seconds of inactivity to save memory
            // The database will be automatically reopened when needed
            .setAutoCloseTimeout(10, TimeUnit.SECONDS)

            // SEC-001: Fallback strategy for database corruption or downgrade scenarios
            // Only destroys and recreates on downgrade (version going backwards)
            // This prevents data loss on migration failures while handling edge cases
            .fallbackToDestructiveMigrationOnDowngrade()

            // ERROR-001: Add callback for database events monitoring
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Note: Timber may not be initialized yet at this point
                    Log.d(TAG, "Database created: version ${db.version}")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Log.d(TAG, "Database opened: version ${db.version}")
                }

                override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                    super.onDestructiveMigration(db)
                    // Log destructive migration - indicates potential data loss
                    Log.w(TAG, "Destructive migration occurred - data may have been lost")
                }
            })

            .build()
    }

    @Provides
    fun provideTopicDao(database: ArguMentorDatabase): TopicDao {
        return database.topicDao()
    }

    @Provides
    fun provideClaimDao(database: ArguMentorDatabase): ClaimDao {
        return database.claimDao()
    }

    @Provides
    fun provideRebuttalDao(database: ArguMentorDatabase): RebuttalDao {
        return database.rebuttalDao()
    }

    @Provides
    fun provideEvidenceDao(database: ArguMentorDatabase): EvidenceDao {
        return database.evidenceDao()
    }

    @Provides
    fun provideQuestionDao(database: ArguMentorDatabase): QuestionDao {
        return database.questionDao()
    }

    @Provides
    fun provideSourceDao(database: ArguMentorDatabase): SourceDao {
        return database.sourceDao()
    }

    @Provides
    fun provideTagDao(database: ArguMentorDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    fun provideFallacyDao(database: ArguMentorDatabase): FallacyDao {
        return database.fallacyDao()
    }
}
