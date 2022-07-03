package com.example.ttanslateapp.data.database

import com.example.ttanslateapp.data.mapper.WordMapper
import com.example.ttanslateapp.data.model.ExamAnswerVariantDb
import com.example.ttanslateapp.domain.ExamWordAnswerRepository
import com.example.ttanslateapp.domain.model.exam.ExamAnswerVariant
import javax.inject.Inject

class ExamWordAnswerRepositoryImpl @Inject constructor(
    private val examWordAnswerDao: ExamWordAnswerDao,
    private val mapper: WordMapper
) : ExamWordAnswerRepository {
    override suspend fun getWordAnswerList(limit: Int): List<ExamAnswerVariant> {
        return examWordAnswerDao.getWordAnswerList(limit).map { mapper.examAnswerDbToExamAnswer(it) }
    }

    override suspend fun modifyWordAnswer(examAnswerVariantDb: ExamAnswerVariantDb): Boolean {
        return examWordAnswerDao.modifyWordAnswer(examAnswerVariantDb)
            .toInt() != ANSWER_VARIANT_IS_NOT_FOUND
    }

    companion object {
        private const val ANSWER_VARIANT_IS_NOT_FOUND = -1
    }
}