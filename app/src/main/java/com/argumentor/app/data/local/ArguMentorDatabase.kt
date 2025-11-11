package com.argumentor.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.argumentor.app.data.local.dao.*
import com.argumentor.app.data.model.*

/**
 * ArguMentor Room Database.
 * Version 1 - Initial schema with FTS support.
 * Version 2 - Added timestamps to Tag, Evidence, Source, Question; renamed Topic.Posture enum values
 * Version 3 - Added index on claimFingerprint for faster duplicate detection
 * Version 4 - Added SourceFts for full-text search on sources
 * Version 5 - Added fallacyIds field to Claim for linking claims to identified fallacies
 */
@Database(
    entities = [
        Topic::class,
        Claim::class,
        Rebuttal::class,
        Evidence::class,
        Question::class,
        Source::class,
        Tag::class,
        ClaimFts::class,
        RebuttalFts::class,
        QuestionFts::class,
        SourceFts::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ArguMentorDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun claimDao(): ClaimDao
    abstract fun rebuttalDao(): RebuttalDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun questionDao(): QuestionDao
    abstract fun sourceDao(): SourceDao
    abstract fun tagDao(): TagDao

    companion object {
        const val DATABASE_NAME = "argumentor_db"
    }
}
