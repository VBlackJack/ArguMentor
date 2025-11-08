package com.argumentor.app.di

import com.argumentor.app.data.local.ArguMentorDatabase
import com.argumentor.app.data.local.dao.ClaimDao
import com.argumentor.app.data.local.dao.TopicDao
import com.argumentor.app.data.repository.ClaimRepository
import com.argumentor.app.data.repository.ImportExportRepository
import com.argumentor.app.data.repository.TopicRepository
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
    fun provideImportExportRepository(
        database: ArguMentorDatabase
    ): ImportExportRepository {
        return ImportExportRepository(database)
    }
}
