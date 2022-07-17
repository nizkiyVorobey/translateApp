package com.example.ttanslateapp.presentation.exam

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ttanslateapp.domain.TranslatedWordRepository
import com.example.ttanslateapp.domain.model.exam.ExamWord
import com.example.ttanslateapp.domain.model.exam.ExamWordStatus
import com.example.ttanslateapp.domain.model.modify_word_chip.Translate
import com.example.ttanslateapp.domain.use_case.GetExamWordListUseCase
import com.example.ttanslateapp.domain.use_case.ModifyWordUseCase
import com.example.ttanslateapp.domain.use_case.UpdateWordPriorityUseCase
import com.example.ttanslateapp.presentation.exam.adapter.ExamKnowledgeState
import com.example.ttanslateapp.presentation.exam.adapter.ExamKnowledgeUiState
import com.example.ttanslateapp.presentation.exam.adapter.ExamMode
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


class ExamKnowledgeWordsViewModel @Inject constructor(
    val getExamWordListUseCase: GetExamWordListUseCase,
    val updateWordPriorityUseCase: UpdateWordPriorityUseCase,
    val modifyWordUseCase: ModifyWordUseCase,
    val repository: TranslatedWordRepository,
) : ViewModel() {

    private val _uiState = MutableLiveData<ExamKnowledgeUiState>()
    val uiState: LiveData<ExamKnowledgeUiState> = _uiState

    private var state = ExamKnowledgeState()
    private fun getTimestamp(): Long = System.currentTimeMillis()

    // FIXME trouble with reenter on screen
    init {

//        viewModelScope.async {
//            var wordCount = 1
//            while (wordCount < 100_000) {
//                val word = ModifyWord(
//                    value = wordCount.toString(),
//                    translates = listOf(
//                        Translate(
//                            localId = 1,
//                            updatedAt = getTimestamp(),
//                            createdAt = getTimestamp(),
//                            value = "translate_$wordCount",
//                            isHidden = false,
//                        )
//                    ),
//                    description = "",
//                    sound = null,
//                    langFrom = "EN",
//                    langTo = "UA",
//                    hints = listOf(
//                        HintItem(
//                            localId = 1,
//                            updatedAt = getTimestamp(),
//                            createdAt = getTimestamp(),
//                            value = "translate_$wordCount",
//                        )
//                    ),
//                    createdAt = getTimestamp(),
//                    updatedAt = getTimestamp(),
//                    transcription = ""
//                )
//                val d = viewModelScope.async {
//                    modifyWordUseCase(word = word)
//                }
//                d.await()
//                delay(1)
//                wordCount++
//                Timber.d("${wordCount}")
//
//            }
//        }

        state = state.copy(isLoading = true)
        _uiState.value = ExamKnowledgeUiState.IsLoadingWords
        generateWordsList()
    }

    fun restoreUI() {
        _uiState.value = ExamKnowledgeUiState.RestoreUI(
            isLoading = state.isLoading,
            examWordList = state.examWordList,
            examWordListEmpty = state.examWordListEmpty,
            currentWord = state.currentWord,
            isExamEnd = state.isExamEnd,
            isInputWordInvalid = state.isInputWordInvalid,
            activeWordPosition = state.activeWordPosition,
            mode = state.mode,
            isModeDialogOpen = state.isModeDialogOpen
        )
    }

    fun changeExamMode(mode: ExamMode) {
        // reset state and set new mode
        getExamWordListUseCase.resetExamWordListCurrentPage()
        state = ExamKnowledgeState().copy(mode = mode)
        generateWordsList()
    }

    fun toggleOpenModeDialog(isOpened: Boolean) {
        state = state.copy(isModeDialogOpen = isOpened)
        _uiState.value = ExamKnowledgeUiState.ToggleOpenModeDialog(isOpened = isOpened)
    }

    fun loadNewPage(position: Int) {
        if (state.mode == ExamMode.DAILY_MODE || state.examWordList.size - position > 5) return

        viewModelScope.launch {
            val list = getExamWordListUseCase.loadNextPage() ?: return@launch

            state = state.copy(
                examWordList = state.examWordList.plus(list),
            )
            _uiState.value = ExamKnowledgeUiState.LoadedNewPage(
                examWordList = state.examWordList,
                activeWordPosition = state.activeWordPosition
            )
        }
    }

    private fun generateWordsList() {
        viewModelScope.launch {
            val list = getExamWordListUseCase().mapIndexed { index, examWord ->
                if (index == 0) examWord.copy(
                    status = ExamWordStatus.IN_PROCESS,
                    isActive = true
                ) else examWord
            }

            val firstWord = list.firstOrNull()?.run {
                if (this.hints.isNotEmpty()) {
                    return@run this.copy(countOfRenderHints = 1)
                }
                return@run this
            }

            state = state.copy(
                examWordListEmpty = list.isEmpty(),
                examWordList = list,
                currentWord = if (list.isEmpty()) null else firstWord,
                isLoading = false
            )
            if (list.isEmpty()) {
                _uiState.value = ExamKnowledgeUiState.LoadedEmptyList
            } else {
                _uiState.value = ExamKnowledgeUiState.LoadedWordsSuccess(
                    examWordList = state.examWordList,
                    currentWord = state.currentWord!!,
                    activeWordPosition = state.activeWordPosition,
                    mode = state.mode
                )
            }

        }
    }


    fun toggleHintExpanded() {
        val currentWord =
            state.currentWord!!.copy(isHintsExpanded = !state.currentWord!!.isHintsExpanded)
        state = state.copy(currentWord = currentWord)

        val allHintsIsShown =
            currentWord.countOfRenderHints >= currentWord.hints.size
        _uiState.value = ExamKnowledgeUiState.ToggleExpandedHint(
            isExpanded = currentWord.isHintsExpanded,
            allHintsIsShown = allHintsIsShown
        )
    }

    fun toggleVisibleVariants() {
        val currentWord =
            state.currentWord!!.copy(isVariantsExpanded = !state.currentWord!!.isVariantsExpanded)

        state = state.copy(currentWord = currentWord)
        _uiState.value =
            ExamKnowledgeUiState.ToggleIsVariantsExpanded(currentWord.isVariantsExpanded)
    }

    fun setSelectVariant(selectedVariantValue: String) {
        state =
            state.copy(currentWord = state.currentWord!!.copy(selectedVariantValue = selectedVariantValue))
        _uiState.value =
            ExamKnowledgeUiState.SelectVariants(selectedVariantValue = selectedVariantValue)
    }


    fun handleExamCheckAnswer(examWordInputValue: String) {
        checkAnswer(examWordInputValue)
    }

    fun showNextHint() {
        var newCount = state.currentWord!!.countOfRenderHints.plus(1)

        state.currentWord?.let {
            val allHintsShown = newCount == it.hints.size

            if (newCount > it.hints.size) {
                newCount -= 1
            }

            state = state.copy(
                currentWord = state.currentWord!!.copy(
                    countOfRenderHints = newCount,
                    allHintsIsShown = allHintsShown
                )
            )
            _uiState.value = ExamKnowledgeUiState.ShowNextHint(
                allHintsIsShown = allHintsShown,
                currentWord = state.currentWord!!
            )
        }
    }


    fun handleAnswerEditText(answer: String) {
        val userGaveAnswer = state.currentWord?.status != ExamWordStatus.IN_PROCESS
        val selectedVariantValue =
            if (state.currentWord?.selectedVariantValue == answer) state.currentWord?.selectedVariantValue else null

        state =
            state.copy(currentWord = state.currentWord?.copy(selectedVariantValue = selectedVariantValue))
        _uiState.value =
            ExamKnowledgeUiState.HandleAnswerInput(
                value = answer.trim(),
                userGaveAnswer = userGaveAnswer,
                selectedVariantValue = selectedVariantValue
            )
    }

    fun toggleVisibilityHiddenDescription() {
        val isExpanded =
            !state.currentWord!!.isHiddenTranslateDescriptionExpanded
        state =
            state.copy(currentWord = state.currentWord!!.copy(isHiddenTranslateDescriptionExpanded = isExpanded))
        _uiState.value =
            ExamKnowledgeUiState.ToggleHiddenDescriptionExpanded(isExpanded = isExpanded)
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
        if (value.isEmpty()) return

        val hiddenTranslate = Translate(
            id = 0L,
            localId = getTimestamp(),
            createdAt = getTimestamp(),
            updatedAt = getTimestamp(),
            value = value,
            isHidden = true
        )

        state.currentWord?.let { currentWord ->
            viewModelScope.launch {
                modifyWordUseCase.modifyTranslates(
                    wordId = currentWord.id,
                    translates = listOf(hiddenTranslate)
                ).apply {
                    val translates =
                        currentWord.translates.plus(hiddenTranslate.copy(id = this.first()))
                    val updatedList = state.examWordList.map {
                        if (it.id == currentWord.id) return@map it.copy(translates = translates) else return@map it
                    }

                    state = state.copy(
                        currentWord = currentWord.copy(translates = translates),
                        examWordList = updatedList
                    )

                    _uiState.value = ExamKnowledgeUiState.UpdateHiddenTranslates(
                        translates = state.currentWord?.translates ?: emptyList(),
                        clearInputValue = true
                    )
                }
            }
        }
    }

    fun toggleIsHiddenTranslate(item: Translate) {
        state.currentWord?.let { currentWord ->
            if (!currentWord.isFreeze) return // forbidden add/modify word translates if user don't answer yet

            val updatedTranslate = item.copy(isHidden = !item.isHidden)
            val translates =
                currentWord.translates.map { if (it.id == item.id) return@map updatedTranslate else return@map it }

            val updatedList = state.examWordList.map {
                if (it.id == currentWord.id) return@map it.copy(translates = translates) else return@map it
            }

            state = state.copy(
                currentWord = currentWord.copy(translates = translates),
                examWordList = updatedList
            )
            _uiState.value =
                ExamKnowledgeUiState.UpdateHiddenTranslates(translates = translates)

            viewModelScope.launch {
                modifyWordUseCase.modifyTranslates(
                    wordId = currentWord.id,
                    translates = listOf(updatedTranslate)
                )
            }
        }
    }

    fun goPrev() {
        if (state.activeWordPosition - 1 < 0) {
            return
        }

        val newActiveWordPosition = state.activeWordPosition - 1
        handleNavigation(newActiveWordPosition)
    }

    fun goToWord(position: Int) {
        handleNavigation(position)
    }

    fun goToNextQuestion() {
        if (state.activeWordPosition + 1 >= state.examWordList.size) {
            return
        }
        val newActiveWordPosition = state.activeWordPosition + 1
        handleNavigation(newActiveWordPosition)
    }


    private fun validateAnswer(answer: String): Boolean {
        return answer.trim().isNotEmpty()
    }

    private fun checkAnswer(answer: String) {
        val answerQuery = answer.trim().lowercase()
        val isValid = validateAnswer(answerQuery)

        // TODO add ui handle this
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

            val isExamEnd = updatedWordList.none { !it.isFreeze }

            state = state.copy(
                examWordList = updatedWordList,
                currentWord = updatedCurrentWord,
                isExamEnd = isExamEnd,
            )

            _uiState.value = ExamKnowledgeUiState.CheckedAnswer(
                status = state.currentWord!!.status,
                examWordList = state.examWordList,
                currentWord = updatedCurrentWord,
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


    private fun handleNavigation(newActiveWordPosition: Int) {
        val currentWord = state.currentWord!!.copy(
            isHintsExpanded = false,
            allHintsIsShown = false,
            isVariantsExpanded = false,
            countOfRenderHints = if (state.currentWord!!.hints.isEmpty()) 0 else 1
        )
        state = state.copy(currentWord = currentWord, activeWordPosition = newActiveWordPosition)

        val newList = updatePositionColors()

        state = state.copy(
            examWordList = newList,
            currentWord = newList[newActiveWordPosition].copy(
                countOfRenderHints = if (newList[newActiveWordPosition].hints.isEmpty()) 0 else 1,
                allHintsIsShown = newList[newActiveWordPosition].hints.size == 1,
                selectedVariantValue = null,
                isHiddenTranslateDescriptionExpanded = false
            ),
        )

        _uiState.value = ExamKnowledgeUiState.QuestionNavigation(
            examWordList = state.examWordList,
            currentWord = state.currentWord!!,
            activeWordPosition = newActiveWordPosition,

            )
    }

}
