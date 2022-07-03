package com.example.ttanslateapp.presentation.exam

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ttanslateapp.domain.model.exam.ExamWord
import com.example.ttanslateapp.domain.model.exam.ExamWordStatus
import com.example.ttanslateapp.domain.model.modify_word_chip.TranslateWordItem
import com.example.ttanslateapp.domain.use_case.GetExamWordListUseCase
import com.example.ttanslateapp.domain.use_case.ModifyWordUseCase
import com.example.ttanslateapp.domain.use_case.UpdateWordPriorityUseCase
import com.example.ttanslateapp.presentation.exam.adapter.ExamKnowledgeState
import com.example.ttanslateapp.presentation.exam.adapter.ExamKnowledgeUiState
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

enum class AnswerResult {
    SUCCESS, FAILED, EMPTY
}

class ExamKnowledgeWordsViewModel @Inject constructor(
    val getExamWordListUseCase: GetExamWordListUseCase,
    val updateWordPriorityUseCase: UpdateWordPriorityUseCase,
    val modifyWordUseCase: ModifyWordUseCase,
    val application: Application
) : ViewModel() {

    private val _uiState = MutableLiveData<ExamKnowledgeUiState>()
    val uiState: LiveData<ExamKnowledgeUiState> = _uiState

    private var state = ExamKnowledgeState()
    private fun getTimestamp(): Long = System.currentTimeMillis()

    // trouble with reenter on screen
//    init {
//        generateWordsList()
//    }

     fun generateWordsList() {
        state = state.copy(isLoading = true)
        _uiState.value = ExamKnowledgeUiState.IsLoadingWords

        viewModelScope.launch {
            // FIXME change mapIndexed on smt like find, etc
            val list = getExamWordListUseCase().mapIndexed { index, examWord ->
                if (index == 0) examWord.copy(
                    status = ExamWordStatus.IN_PROCESS,
                    isActive = true
                ) else examWord
            }

            val firstWord = list.firstOrNull()
            state = state.copy(
                examWordListEmpty = list.isEmpty(),
                examWordList = list,
                currentWord = if (list.isEmpty()) null else firstWord,
                countShownHints = if (firstWord == null || firstWord.hints.isEmpty()) 0 else 1, // FIXME replace into currentWord
                isLoading = false
            )
            if (list.isEmpty()) {
                _uiState.value = ExamKnowledgeUiState.LoadedEmptyList
            } else {
                _uiState.value = ExamKnowledgeUiState.LoadedWordsSuccess(
                    examWordList = state.examWordList,
                    currentWord = state.currentWord!!,
                    countShownHints = state.countShownHints,
                    activeWordPosition = state.activeWordPosition
                )
            }

        }
    }

    fun toggleVisibleHint() {
        state = state.copy(isShownHintsVisible = !state.isShownHintsVisible)
        val nextHintButtonVisibility =
            if (state.allHintsShown || state.currentWord?.hints?.isEmpty() == true || state.countShownHints >= state.currentWord!!.hints.size) View.GONE else View.VISIBLE
        _uiState.value = ExamKnowledgeUiState.ToggleIsVisibleHint(
            state.isShownHintsVisible,
            nextHintButtonVisibility = nextHintButtonVisibility
        )
    }

    fun toggleVisibleVariants() {
        state = state.copy(isShownVariants = !state.isShownVariants)
        _uiState.value = ExamKnowledgeUiState.ToggleIsVisibleVariants(state.isShownVariants)

    }

    fun handleExamCheckAnswer(examWordInputValue: String) {
        checkAnswer(examWordInputValue)
    }

    fun showNextHint() {
        var newCount = state.countShownHints.plus(1)

        state.currentWord?.let {
            val allHintsShown = newCount == it.hints.size

            if (newCount > it.hints.size) {
                newCount -= 1
            }

            state = state.copy(allHintsShown = allHintsShown, countShownHints = newCount)
            _uiState.value = ExamKnowledgeUiState.ShowNextHint(
                allHintsShown = allHintsShown,
                countShownHints = newCount,
                currentWord = state.currentWord!!
            )
        }
    }

    private fun validateAnswer(answer: String): Boolean {
        return answer.trim().isNotEmpty()
    }

    fun handleAnswerEditText(answer: String) {
        Log.d("handleAnswerEditText", "${state.currentWord?.status}")
        val userGaveAnswer = state.currentWord?.status != ExamWordStatus.IN_PROCESS
        Log.d("handleAnswerEditText_userGaveAnswer", "${userGaveAnswer}")
        _uiState.value =
            ExamKnowledgeUiState.HandleAnswerInput(
                value = answer,
                userGaveAnswer = userGaveAnswer
            )
    }


    fun toggleVisibilityHiddenDescription() {
        val visibility =
            if (state.hiddenTranslateDescriptionVisibility == View.VISIBLE) View.GONE else View.VISIBLE
        state = state.copy(hiddenTranslateDescriptionVisibility = visibility)
        _uiState.value =
            ExamKnowledgeUiState.ToggleVisibilityHiddenDescription(visibility = visibility)
    }

    fun toggleExpandedAllTranslates() {
        val currentWord = state.currentWord!!
        val isExpanded = !currentWord.isTranslateExpanded
        state = state.copy(currentWord = currentWord.copy(isTranslateExpanded = isExpanded))

        val translates = state.currentWord?.translates ?: emptyList()
        _uiState.value = ExamKnowledgeUiState.ToggleCurrentWordTrasnalteExpanded(
            isExpanded = isExpanded,
            translates = translates
        )
    }

    fun addHiddenTranslate(value: String) {
        val hiddenTranslate = TranslateWordItem(
            id = UUID.randomUUID().toString(),
            createdAt = getTimestamp(),
            updatedAt = getTimestamp(),
            value = value,
            isHidden = true
        )
        state.currentWord?.let {
            val translates = it.translates.plus(hiddenTranslate)

            state = state.copy(currentWord = it.copy(translates = translates))
            _uiState.value = ExamKnowledgeUiState.UpdateHiddenTranslates(
                translates = state.currentWord?.translates ?: emptyList(),
                clearInputValue = true
            )
            // TODO add to room
//        viewModelScope.launch {
//            modifyWordUseCase.updateTranslates(currentWord.translates)
//        }
        }
    }

    private fun checkAnswer(answer: String) {
        val answerQuery = answer.trim().lowercase()
        val isValid = validateAnswer(answerQuery)
        if (!isValid) {
            return
        }

        val matchedAnswer = state.currentWord?.translates?.find { translate ->
            answerQuery == translate.value.lowercase().trim()
        }
        val isAnswerCorrect = matchedAnswer != null

        state.currentWord?.let { currentWord ->
            var newPriority =
                if (isAnswerCorrect) currentWord.priority.minus(1) else currentWord.priority.plus(1)
            if (newPriority < 0) newPriority = 0
            val newStatus =
                if (isAnswerCorrect) ExamWordStatus.SUCCESS else ExamWordStatus.FAIL

            val updatedCurrentWord =
                currentWord.copy(
                    priority = newPriority,
                    status = newStatus,
                    isFreeze = true,
                    givenAnswer = answer
                )

            viewModelScope.launch {
                updateWordPriorityUseCase(priority = newPriority, id = currentWord.id)
            }

            val updatedWordList = state.examWordList.map {
                if (it.id == currentWord.id) return@map updatedCurrentWord
                return@map it
            }

            var isExamEnd = false
            if (state.examWordList.last().id == currentWord.id) {
                isExamEnd = true
            }
            state = state.copy(
                examWordList = updatedWordList,
                currentWord = updatedCurrentWord,
                isExamEnd = isExamEnd // TODO maybe delete from state
            )

            _uiState.value = ExamKnowledgeUiState.CheckedAnswer(
                status = state.currentWord!!.status,
                examWordList = state.examWordList,
                isExamEnd = isExamEnd,
                givenAnswer = answer
            )
        }
    }


    private fun updatePositionColors(): List<ExamWord> {
        val newList = state.examWordList.mapIndexed { index, examWord ->
            if (index == state.activeWordPosition) return@mapIndexed examWord.copy(
                status = updatesStatusForSkippedQuestion(
                    examWord
                ),
                isActive = true
            )
            return@mapIndexed examWord.copy(
                status = updatesStatusForSkippedQuestion(examWord),
                isActive = false
            )
        }
        return newList
    }

    private fun updatesStatusForSkippedQuestion(word: ExamWord): ExamWordStatus {
        if (word.isFreeze) return word.status // when user already answered the question
        if (!word.isFreeze && state.examWordList.elementAt(state.activeWordPosition).id == word.id) return ExamWordStatus.IN_PROCESS // when word is currentWord (without answer yet)
        return ExamWordStatus.UNPROCESSED // when user skipped word
    }

    fun toggleIsHiddenTranslate(item: TranslateWordItem) {

        state.currentWord?.let {
            if (!it.isFreeze) return // forbidden add/modify word translates if user don't answer yet
            // FIXME doesn't work (not update style, areTheSame not invoke), why?
//            it.translates.find { it.id == item.id }?.isHidden = !item.isHidden
//            Log.d("newTranslateList", "${it.translates}")

            val translates =
                it.translates.map { if (it.id == item.id) return@map it.copy(isHidden = !it.isHidden) else return@map it }


            state = state.copy(currentWord = it.copy(translates = translates))
            _uiState.value =
                ExamKnowledgeUiState.UpdateHiddenTranslates(translates = translates)
        }
    }

    fun goPrev() {
        if (state.activeWordPosition - 1 < 0) {
            return
        }

        val newActiveWordPosition = state.activeWordPosition - 1
        handleNavigation(newActiveWordPosition)
    }


    fun goToNextQuestion() {
        if (state.activeWordPosition + 1 >= state.examWordList.size) {
            return
        }
        val newActiveWordPosition = state.activeWordPosition + 1
        handleNavigation(newActiveWordPosition)
    }


    private fun handleNavigation(newActiveWordPosition: Int) {
        state = state.copy(
            activeWordPosition = newActiveWordPosition
        )

        val newList = updatePositionColors()
        Log.d("newList", "${newList}")

        state = state.copy(
            examWordList = newList,
            currentWord = newList[newActiveWordPosition],
            isShownHintsVisible = false,
            allHintsShown = false,
            isShownVariants = false,
            countShownHints = if (newList[newActiveWordPosition].hints.isEmpty()) 0 else 1
        )

        _uiState.value = ExamKnowledgeUiState.QuestionNavigation(
            examWordList = state.examWordList,
            currentWord = state.currentWord!!,
            activeWordPosition = newActiveWordPosition,
            countShownHints = state.countShownHints
        )
    }
}
