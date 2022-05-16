package com.example.ttanslateapp.presentation.exam

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ttanslateapp.data.model.Sound
import com.example.ttanslateapp.domain.TranslatedWordRepository
import com.example.ttanslateapp.domain.model.ModifyWord
import com.example.ttanslateapp.domain.model.exam.ExamWord
import com.example.ttanslateapp.domain.model.exam.ExamWordStatus
import com.example.ttanslateapp.domain.model.modify_word_chip.HintItem
import com.example.ttanslateapp.domain.model.modify_word_chip.TranslateWordItem
import com.example.ttanslateapp.domain.use_case.GetExamAnswerVariantsUseCase
import com.example.ttanslateapp.domain.use_case.GetExamWordListUseCase
import com.example.ttanslateapp.domain.use_case.ModifyWordUseCase
import com.example.ttanslateapp.domain.use_case.UpdateWordPriorityUseCase
import com.example.ttanslateapp.presentation.exam.AnswerResult.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

enum class AnswerResult {
    SUCCESS, FAILED, EMPTY
}

class ExamKnowledgeWordsViewModel @Inject constructor(
    val getExamWordListUseCase: GetExamWordListUseCase,
    val updateWordPriorityUseCase: UpdateWordPriorityUseCase,
//    val modifyWordUseCase: ModifyWordUseCase

    ) : ViewModel() {
    private val _examWordList = MutableLiveData<List<ExamWord>>()
    val examWordList: LiveData<List<ExamWord>> = _examWordList

    private val _currentWord = MutableLiveData<ExamWord>()
    val currentWord: LiveData<ExamWord> = _currentWord

    private val _answerResult = MutableLiveData(EMPTY)
    val answerResult: LiveData<AnswerResult> = _answerResult

    private val _isExamEnd = MutableLiveData(false)
    val isExamEnd: LiveData<Boolean> = _isExamEnd

    private val _countShownHints = MutableLiveData(0)
    val countShownHints: LiveData<Int> = _countShownHints

    private val _allHintsShown = MutableLiveData(false)
    val allHintsShown: LiveData<Boolean> = _allHintsShown

    private val _isShownHintsVisible = MutableLiveData(false)
    val isShownHintsVisible: LiveData<Boolean> = _isShownHintsVisible

    private val _isShownVariants = MutableLiveData(false)
    val isShownVariants: LiveData<Boolean> = _isShownVariants

    private val _isInoutWordInvalid = MutableLiveData(false)
    val isInoutWordInvalid: LiveData<Boolean> = _isInoutWordInvalid

    var activeWordPosition = 0


//    init {
//        viewModelScope.launch {
//            for (word in generateList()) {
//                modifyWordUseCase(word)
//            }
//        }
//    }

    fun toggleVisibleHint() {
        if (_countShownHints.value == 0) {
            _countShownHints.value = 1
        }
        _isShownHintsVisible.value = !_isShownHintsVisible.value!!
    }

    fun toggleVisibleVariants() {
        _isShownVariants.value = !_isShownVariants.value!!
    }

    fun showNextHint() {
        val newCount = _countShownHints.value!!.plus(1)

        if (newCount == _currentWord.value!!.hints.size) {
            _allHintsShown.value = true
        }

        if (newCount > _currentWord.value!!.hints.size) {
            return
        }

        _countShownHints.value = newCount
    }

    fun generateWordsList() {
        viewModelScope.launch {
            val list = getExamWordListUseCase().mapIndexed { index, examWord ->
                if (index == 0) examWord.copy(status = ExamWordStatus.IN_PROCESS) else examWord
            }

            if (list.isNotEmpty()) {
                _examWordList.value = list
                _currentWord.value = list[0]
            }
        }
    }

    private fun validateAnswer(answer: String): Boolean {
        return answer.trim().isNotEmpty()
    }

    fun checkAnswer(answer: String, cb: () -> Unit) {
        val answerQuery = answer.trim().lowercase()
        val isValid = validateAnswer(answerQuery)
        if (!isValid) {
            _isInoutWordInvalid.value = true
            return
        }

        val answerIsCorrect = _currentWord.value!!.translates.find { translate ->
            translate.value.lowercase() == answerQuery
        }

        if (answerIsCorrect != null) {
            _answerResult.value = SUCCESS
        } else {
            _answerResult.value = FAILED
        }

        updatePositionColors()
        updatePriority()
        if (_examWordList.value?.last()!!.id == _currentWord.value!!.id) {
            _isExamEnd.value = true
        }

        // invoke callback if validate is success
        cb()
    }


    private fun updatePositionColors() {
        val newList = _examWordList.value?.mapIndexed { index, it ->

            if (index == activeWordPosition) {
                return@mapIndexed it.copy(status = getCorrectStatus())
            }

            return@mapIndexed it
        }

        newList?.let {
            _examWordList.value = it

        }
    }

    private fun updatePriority() {
        val isAnswerCorrect = getIsAnswerCorrect()
        val newList = _examWordList.value?.mapIndexed { index, it ->
            var newPriority = if (isAnswerCorrect) it.priority - 1 else it.priority + 1
            if (newPriority < 0) newPriority = 0

            if (index == activeWordPosition) {
                viewModelScope.launch {
                    updateWordPriorityUseCase(priority = newPriority, id = it.id)
                }
                return@mapIndexed it.copy(priority = newPriority)
            }

            return@mapIndexed it
        }

        newList?.let {
            _examWordList.value = it

        }
    }

    fun goToNextQuestion() {
        activeWordPosition++
        if (activeWordPosition >= _examWordList.value!!.size) {
            return
        }
        goNext()
    }

    private fun getIsAnswerCorrect(): Boolean {
        return _answerResult.value!! == SUCCESS
    }

    private fun getCorrectStatus(): ExamWordStatus {
        return when (_answerResult.value!!) {
            SUCCESS -> {
                ExamWordStatus.SUCCESS
            }
            FAILED -> {
                ExamWordStatus.FAIL
            }
            EMPTY -> {
                ExamWordStatus.IN_PROCESS
            }
        }
    }

    private fun goNext() {
        resetWordSateToDefault()
        updatePositionColors()
        _currentWord.value = _examWordList.value!![activeWordPosition]
    }

    private fun resetWordSateToDefault() {
        // close hints and variants when go to text word
        _isShownHintsVisible.value = false
        _allHintsShown.value = false
        _isShownVariants.value = false
        _answerResult.value = EMPTY
    }
}


fun generateList(): List<ModifyWord> {
    return listOf(
        ModifyWord(
            priority = 5,
            value = "Apple",
            translates = listOf(
                TranslateWordItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "яблуко"
                ),
                TranslateWordItem(
                    id = "2",
                    createdAt = 2L,
                    updatedAt = 2L,
                    value = "яблучко"
                )
            ),
            hints = listOf(
                HintItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "an fruit"
                ),
                HintItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "green"
                ),
                HintItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "you can eat this"
                ),
            ),
            description = "",
            langFrom = "en",
            langTo = "ua",
            sound = null,
            transcription = ""
        ),

        ModifyWord(
            priority = 5,
            value = "Car",
            translates = listOf(
                TranslateWordItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "Машина"
                )
            ),
            hints = listOf(
                HintItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "може їздити"
                ),
                HintItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "має 4 колеса"
                ),
                HintItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "робить біп-біп"
                ),
            ),
            description = "",
            langFrom = "en",
            langTo = "ua",
            sound = null,
            transcription = ""
        ),
        ModifyWord(
            priority = 5,
            value = "Frog",
            translates = listOf(
                TranslateWordItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "жаба"
                ),
                TranslateWordItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "жабеня"
                ),
                TranslateWordItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "жабка"
                )
            ),
            hints = listOf(
                HintItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "зелене"
                ),
                HintItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "квакає"
                ),
                HintItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "живе біля водойми"
                ),
            ),
            description = "звичайна жабка, що живе у пруді",
            langFrom = "en",
            langTo = "ua",
            sound = null,
            transcription = ""
        ),


        ModifyWord(
            priority = 5,
            value = "Fish",
            translates = listOf(
                TranslateWordItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "риба"
                ),
                TranslateWordItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "рибка"
                )
            ),
            hints = listOf(
                HintItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "плаває"
                ),
            ),
            description = "",
            langFrom = "en",
            langTo = "ua",
            sound = null,
            transcription = ""
        ),

        ModifyWord(
            priority = 5,
            value = "Computer",
            translates = listOf(
                TranslateWordItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "комп'ютер"
                ),
            ),
            hints = listOf(),
            description = "",
            langFrom = "en",
            langTo = "ua",
            sound = null,
            transcription = ""
        ),


        ModifyWord(
            priority = 5,
            value = "ball",
            translates = listOf(
                TranslateWordItem(
                    id = "1",
                    createdAt = 1L,
                    updatedAt = 1L,
                    value = "м'яч"
                ),
            ),
            hints = listOf(),
            description = "",
            langFrom = "en",
            langTo = "ua",
            sound = null,
            transcription = ""
        )
    )
}




