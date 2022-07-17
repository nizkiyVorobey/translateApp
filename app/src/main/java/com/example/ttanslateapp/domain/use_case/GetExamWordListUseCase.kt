package com.example.ttanslateapp.domain.use_case

import com.example.ttanslateapp.data.mapper.WordMapper
import com.example.ttanslateapp.domain.ExamWordAnswerRepository
import com.example.ttanslateapp.domain.TranslatedWordRepository
import com.example.ttanslateapp.domain.model.exam.ExamAnswerVariant
import com.example.ttanslateapp.domain.model.exam.ExamWord
import com.example.ttanslateapp.util.EXAM_WORD_ANSWER_LIST_SIZE
import com.example.ttanslateapp.util.temporarryAnswerList
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

class GetExamWordListUseCase @Inject constructor(
    private val repository: TranslatedWordRepository,
    private val examWordAnswerRepository: ExamWordAnswerRepository,
    val mapper: WordMapper,
) {
    private var getExamWordListCurrentPage: Int = 0
    private var isLoadingNextPage: Boolean = false

    fun resetExamWordListCurrentPage() {
        getExamWordListCurrentPage = 0
    }


    suspend fun loadNextPage(): List<ExamWord>? {
//        repository.getWord()
        if (isLoadingNextPage) return null
        return invoke()
    }

    suspend operator fun invoke() = coroutineScope {
        isLoadingNextPage = true
        val skip = getExamWordListCurrentPage * EXAM_WORD_LIST_COUNT
        getExamWordListCurrentPage += 1

        val answerList = getExamAnswerVariants(EXAM_WORD_LIST_COUNT)

        repository.getExamWordList(
            count = EXAM_WORD_LIST_COUNT,
            skip = skip
        )
            .mapIndexed { index, examWord ->
                val from = index * EXAM_WORD_ANSWER_LIST_SIZE
                val to = from + EXAM_WORD_ANSWER_LIST_SIZE - 1

                val randomWordTranslateIndex =
                    Random(System.currentTimeMillis()).nextInt(0 until examWord.translates.size)
                examWord.copy(
                    answerVariants = answerList.slice(from until to)// FIXME ME FROM TO
                        .plus(ExamAnswerVariant(value = examWord.translates[randomWordTranslateIndex].value))
                        .shuffled()
                )
            }
            .apply { isLoadingNextPage = false }
    }

    // get 60 random Answer Variants
    private suspend fun getExamAnswerVariants(examWordListCount: Int) = coroutineScope {
        val limit = examWordListCount * EXAM_WORD_ANSWER_LIST_SIZE
        val list = examWordAnswerRepository.getWordAnswerList(limit)

        if (list.isEmpty()) {
            fillAnswerVariantsDb()
            examWordAnswerRepository.getWordAnswerList(limit)
        } else {
            list
        }
    }

    private suspend fun fillAnswerVariantsDb() {
        val newList = mutableListOf<ExamAnswerVariant>()
        for (wordItem in temporarryAnswerList) {
            newList.add(
                ExamAnswerVariant(
                    value = wordItem,
                )
            )
        }

        examWordAnswerRepository.setWordAnswerList(newList)
    }

    companion object {
        private const val EXAM_WORD_LIST_COUNT = 10
    }
}