package com.example.ttanslateapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ttanslateapp.util.EXAM_WORD_ANSWERS_TABLE_NAME

@Entity(tableName = EXAM_WORD_ANSWERS_TABLE_NAME)
data class ExamAnswerVariantDb(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val value: String
)