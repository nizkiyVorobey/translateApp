package com.example.ttanslateapp.domain.use_case

import com.example.ttanslateapp.domain.TranslatedWordRepository
import com.example.ttanslateapp.domain.model.WordRV
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSearchedWordListUseCase @Inject constructor(
    private val repository: TranslatedWordRepository
) {
    private var getExamWordListCurrentPage: Int = 0
    private var isLoadingNextPage: Boolean = false
    private var totalCount: Int? = null
    private var _isReachedEnd: Boolean = false

    fun resetExamWordListCurrentPage() {
        getExamWordListCurrentPage = 0
        totalCount = null
        _isReachedEnd = false
    }

    suspend fun loadNextPage(searchValue: String): Flow<List<WordRV>>? {
        if (isLoadingNextPage || _isReachedEnd) return null

        return loadData(searchValue)
    }

    /**
     * Оскільки ми працюємо разом з Flow, то завантажувати наступні 10 елементів і плюсовати не вийде,
     * то при 2-ух таких завантаженнях у нас буде відпрацбовувати два flow, при 5-ти - п'ять.
     * Тому при першому завантаженні ми завантажуємо перші 10 елементів, при другому вже 20, при шостому 60.
     */
    suspend fun loadData(searchValue: String) = coroutineScope {
        isLoadingNextPage = true
        val count = getExamWordListCurrentPage * WORD_LIST_PAGE_SIZE + WORD_LIST_PAGE_SIZE
        getExamWordListCurrentPage += 1

        if (totalCount == null) {
            totalCount = repository.searchWordListCount(query = searchValue)
        }

        totalCount?.let {
            _isReachedEnd = count >= it
        }

        repository.searchWordList(
            count = count,
            query = searchValue
        ).also { isLoadingNextPage = false }
    }

    companion object {
         const val WORD_LIST_PAGE_SIZE = 10
    }
}