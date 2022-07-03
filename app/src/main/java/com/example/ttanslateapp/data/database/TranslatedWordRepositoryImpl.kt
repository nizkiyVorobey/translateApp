package com.example.ttanslateapp.data.database

import com.example.ttanslateapp.data.mapper.WordMapper
import com.example.ttanslateapp.data.model.TranslatedWordDb
import com.example.ttanslateapp.domain.TranslatedWordRepository
import com.example.ttanslateapp.domain.model.ModifyWord
import com.example.ttanslateapp.domain.model.WordRV
import com.example.ttanslateapp.domain.model.exam.ExamWord
import com.example.ttanslateapp.domain.model.modify_word_chip.TranslateWordItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TranslatedWordRepositoryImpl @Inject constructor(
    private val translatedWordDao: TranslatedWordDao,
    private val mapper: WordMapper
) : TranslatedWordRepository {

    override suspend fun getWordList(): Flow<List<WordRV>> {
        return translatedWordDao.getWordList().map { list ->
            mapper.wordListDbToWordList(list)
        }
    }

    override suspend fun getExamWordList(count: Int): List<ExamWord> {
        return translatedWordDao.getExamWordList(count).map { mapper.wordDbToExamWord(it) }
    }

    override suspend fun updatePriorityById(priority: Int, id: Long): Boolean {
        return translatedWordDao.updatePriorityById(priority, id) != WORD_IS_NOT_FOUND
    }

    override suspend fun searchWordList(query: String): Flow<List<WordRV>> {
        return translatedWordDao.searchWordList("%$query%").map { list ->
            mapper.wordListDbToWordList(list)
        }
    }

    override suspend fun getWordById(id: Long): ModifyWord {
        return mapper.wordDbToModifyWord(translatedWordDao.getWordById(id))
    }

    override suspend fun deleteWord(id: Long): Boolean {
        return translatedWordDao.deleteWord(id) != WORD_IS_NOT_FOUND
    }

    override suspend fun modifyWord(translatedWordDb: TranslatedWordDb): Boolean {
        val modifiedWordId = translatedWordDao.modifyWord(translatedWordDb)
        return modifiedWordId.toInt() != WORD_IS_NOT_FOUND
    }

//    override suspend fun updateWordTranslates(translateWordList: String): Boolean {
//        val result = translatedWordDao.updateWordTranslates(translateWordList)
//        return result != UPDATE_IS_FAILED
//    }

    companion object {
        private const val WORD_IS_NOT_FOUND = -1
        private const val UPDATE_IS_FAILED = -1
    }
}