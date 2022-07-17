package com.example.ttanslateapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ttanslateapp.data.model.PotentialExamAnswerDb
import com.example.ttanslateapp.util.EXAM_WORD_ANSWERS_TABLE_NAME

@Dao
interface ExamWordAnswerDao {
    @Query("SELECT * FROM $EXAM_WORD_ANSWERS_TABLE_NAME ORDER BY RANDOM() LIMIT :limit")
    suspend fun getWordAnswerList(limit: Int): List<PotentialExamAnswerDb>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun modifyWordAnswer(examAnswerVariant: PotentialExamAnswerDb): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setWordAnswerList(list: List<PotentialExamAnswerDb>)

}