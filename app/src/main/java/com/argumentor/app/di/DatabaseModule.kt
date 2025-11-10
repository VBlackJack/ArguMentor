package com.argumentor.app.di

import android.content.Context
import androidx.room.Room
import com.argumentor.app.data.local.ArguMentorDatabase
import com.argumentor.app.data.local.DatabaseMigrations
import com.argumentor.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ArguMentorDatabase {
        return Room.databaseBuilder(
            context,
            ArguMentorDatabase::class.java,
            ArguMentorDatabase.DATABASE_NAME
        )
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
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
}
