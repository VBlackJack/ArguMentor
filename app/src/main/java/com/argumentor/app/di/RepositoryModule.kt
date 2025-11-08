package com.argumentor.app.di

import com.argumentor.app.data.local.ArguMentorDatabase
import com.argumentor.app.data.local.dao.*
import com.argumentor.app.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTopicRepository(
        topicDao: TopicDao
    ): TopicRepository {
        return TopicRepository(topicDao)
    }

    @Provides
    @Singleton
    fun provideClaimRepository(
        claimDao: ClaimDao
    ): ClaimRepository {
        return ClaimRepository(claimDao)
    }

    @Provides
    @Singleton
    fun provideRebuttalRepository(
        rebuttalDao: RebuttalDao
    ): RebuttalRepository {
        return RebuttalRepository(rebuttalDao)
    }

    @Provides
    @Singleton
    fun provideEvidenceRepository(
        evidenceDao: EvidenceDao
    ): EvidenceRepository {
        return EvidenceRepository(evidenceDao)
    }

    @Provides
    @Singleton
    fun provideQuestionRepository(
        questionDao: QuestionDao
    ): QuestionRepository {
        return QuestionRepository(questionDao)
    }

    @Provides
    @Singleton
    fun provideSourceRepository(
        sourceDao: SourceDao
    ): SourceRepository {
        return SourceRepository(sourceDao)
    }

    @Provides
    @Singleton
    fun provideTagRepository(
        tagDao: TagDao
    ): TagRepository {
        return TagRepository(tagDao)
    }

    @Provides
    @Singleton
    fun provideImportExportRepository(
        database: ArguMentorDatabase
    ): ImportExportRepository {
        return ImportExportRepository(database)
    }
}
