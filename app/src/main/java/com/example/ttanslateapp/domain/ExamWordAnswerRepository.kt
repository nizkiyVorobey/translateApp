package com.example.ttanslateapp.domain

import com.example.ttanslateapp.data.model.ExamAnswerVariantDb
import com.example.ttanslateapp.domain.model.exam.ExamAnswerVariant

interface ExamWordAnswerRepository {
    suspend fun getWordAnswerList(limit: Int): List<ExamAnswerVariant>
    suspend fun modifyWordAnswer(examAnswerVariantDb: ExamAnswerVariantDb): Boolean
}