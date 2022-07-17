package com.example.ttanslateapp.data.database

import androidx.room.*
import com.example.ttanslateapp.data.model.*
import com.example.ttanslateapp.util.TRANSLATED_WORDS_TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslatedWordDao {
    @Query("SELECT * FROM $TRANSLATED_WORDS_TABLE_NAME WHERE value LIKE :query ORDER BY created_at DESC LIMIT :count")
    fun searchWordList(query: String, count: Int): Flow<List<WordFullDb>>

    @Query("SELECT COUNT(*) FROM $TRANSLATED_WORDS_TABLE_NAME WHERE value LIKE :query")
    suspend fun searchWordListCount(query: String): Int

    @Query("SELECT * FROM $TRANSLATED_WORDS_TABLE_NAME ORDER BY priority DESC, updated_at DESC LIMIT :count OFFSET :skip")
    suspend fun getExamWordList(count: Int, skip: Int): List<WordFullDb>

    @Query("SELECT COUNT(*) FROM $TRANSLATED_WORDS_TABLE_NAME")
    suspend fun getExamWordListSize(): Int

    @Query("SELECT * FROM $TRANSLATED_WORDS_TABLE_NAME WHERE id= :wordId")
    suspend fun getWordById(wordId: Long): WordFullDb

    @Query("DELETE FROM $TRANSLATED_WORDS_TABLE_NAME WHERE id = :wordId")
    suspend fun deleteWord(wordId: Long): Int

    // word parts
    @Query("UPDATE $TRANSLATED_WORDS_TABLE_NAME SET priority=:priority, updated_at=:updated_time WHERE id = :id")
    suspend fun updatePriorityById(
        priority: Int,
        id: Long,
        updated_time: Long = System.currentTimeMillis()
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun modifyWordInfo(wordInfoDb: WordInfoDb): Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun modifyTranslates(translates: List<TranslateDb>): List<Long>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun modifyHints(hints: List<HintDb>): List<Long>

}