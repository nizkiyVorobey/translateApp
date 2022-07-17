package com.example.ttanslateapp.domain

import com.example.ttanslateapp.data.model.PotentialExamAnswerDb
import com.example.ttanslateapp.domain.model.exam.ExamAnswerVariant

interface ExamWordAnswerRepository {
    suspend fun getWordAnswerList(limit: Int): List<ExamAnswerVariant>
    suspend fun modifyWordAnswer(potentialExamAnswerDb: PotentialExamAnswerDb): Boolean
    suspend fun setWordAnswerList(list: List<ExamAnswerVariant>)
}