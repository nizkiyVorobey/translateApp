package com.example.ttanslateapp.data.model

import androidx.room.*
import com.example.ttanslateapp.domain.model.WordAudio
import com.example.ttanslateapp.domain.model.modify_word_chip.HintItem
import com.example.ttanslateapp.domain.model.modify_word_chip.TranslateWordItem
import com.example.ttanslateapp.util.TRANSLATED_WORDS_TABLE_NAME
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class TranslationConverters {
    @TypeConverter
    fun fromList(translations: List<TranslateWordItem>): String {
        val gson = Gson()
        return gson.toJson(translations)
    }

    @TypeConverter
    fun fromString(translations: String): List<TranslateWordItem> {
        val listType: Type = object : TypeToken<List<TranslateWordItem>?>() {}.type
        return Gson().fromJson(translations, listType)
    }
}

class HintConverters {
    @TypeConverter
    fun fromList(hints: List<HintItem>?): String {
        val gson = Gson()
        return gson.toJson(hints)
    }

    @TypeConverter
    fun fromString(hints: String): List<HintItem>? {
        val listType: Type = object : TypeToken<List<HintItem>?>() {}.type
        return Gson().fromJson(hints, listType)
    }
}

class SoundConvertor {
    @TypeConverter
    fun fromSound(sound: WordAudio?) = sound?.fileName //  if value exist set value, else null

    @TypeConverter
    fun toSound(value: String?) = value?.let {
        WordAudio(fileName = it)
    }
}

@Entity(tableName = TRANSLATED_WORDS_TABLE_NAME)
@TypeConverters(SoundConvertor::class, TranslationConverters::class, HintConverters::class)
data class TranslatedWordDb(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val priority: Int,
    val value: String,
    val translates: List<TranslateWordItem>,
    val description: String,
    val sound: WordAudio?, // english sound
    val langFrom: String,
    val langTo: String,
    val hints: List<HintItem>?,
    val transcription: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
