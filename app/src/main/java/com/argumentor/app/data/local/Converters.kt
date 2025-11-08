package com.argumentor.app.data.local

import androidx.room.TypeConverter
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Evidence
import com.argumentor.app.data.model.Question
import com.argumentor.app.data.model.Topic
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room type converters for complex data types.
 */
class Converters {
    private val gson = Gson()

    // List<String> converters
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    // Topic.Posture converters
    @TypeConverter
    fun fromPosture(posture: Topic.Posture): String {
        return posture.toString()
    }

    @TypeConverter
    fun toPosture(value: String): Topic.Posture {
        return Topic.Posture.fromString(value)
    }

    // Claim.Stance converters
    @TypeConverter
    fun fromStance(stance: Claim.Stance): String {
        return stance.toString()
    }

    @TypeConverter
    fun toStance(value: String): Claim.Stance {
        return Claim.Stance.fromString(value)
    }

    // Claim.Strength converters
    @TypeConverter
    fun fromStrength(strength: Claim.Strength): String {
        return strength.toString()
    }

    @TypeConverter
    fun toStrength(value: String): Claim.Strength {
        return Claim.Strength.fromString(value)
    }

    // Evidence.EvidenceType converters
    @TypeConverter
    fun fromEvidenceType(type: Evidence.EvidenceType): String {
        return type.toString()
    }

    @TypeConverter
    fun toEvidenceType(value: String): Evidence.EvidenceType {
        return Evidence.EvidenceType.fromString(value)
    }

    // Evidence.Quality converters
    @TypeConverter
    fun fromQuality(quality: Evidence.Quality): String {
        return quality.toString()
    }

    @TypeConverter
    fun toQuality(value: String): Evidence.Quality {
        return Evidence.Quality.fromString(value)
    }

    // Question.QuestionKind converters
    @TypeConverter
    fun fromQuestionKind(kind: Question.QuestionKind): String {
        return kind.toString()
    }

    @TypeConverter
    fun toQuestionKind(value: String): Question.QuestionKind {
        return Question.QuestionKind.fromString(value)
    }
}
