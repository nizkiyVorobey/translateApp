package com.example.ttanslateapp.presentation.exam.adapter

import android.view.View
import com.example.ttanslateapp.domain.model.exam.ExamWord
import com.example.ttanslateapp.domain.model.exam.ExamWordStatus
import com.example.ttanslateapp.domain.model.modify_word_chip.TranslateWordItem
import com.example.ttanslateapp.presentation.exam.AnswerResult

sealed interface ExamKnowledgeUiState {
    object IsLoadingWords : ExamKnowledgeUiState
    object LoadedEmptyList : ExamKnowledgeUiState

    data class LoadedWordsSuccess(
        val examWordList: List<ExamWord>,
        val currentWord: ExamWord,
        val countShownHints: Int,
        val activeWordPosition: Int
    ) : ExamKnowledgeUiState

    data class HandleAnswerInput(val value: String, val userGaveAnswer: Boolean) :
        ExamKnowledgeUiState

    data class CheckedAnswer(
        val status: ExamWordStatus = ExamWordStatus.UNPROCESSED,
        val examWordList: List<ExamWord>,
        val isExamEnd: Boolean = false,
        val givenAnswer: String
    ) : ExamKnowledgeUiState

    data class QuestionNavigation(
        val examWordList: List<ExamWord>,
        val currentWord: ExamWord,
        val activeWordPosition: Int,
        val countShownHints: Int
    ) : ExamKnowledgeUiState

    data class ToggleIsVisibleVariants(val isVisible: Boolean) : ExamKnowledgeUiState
    data class ToggleIsVisibleHint(val isVisible: Boolean, val nextHintButtonVisibility: Int) :
        ExamKnowledgeUiState

    data class ShowNextHint(
        val allHintsShown: Boolean,
        val countShownHints: Int,
        val currentWord: ExamWord
    ) : ExamKnowledgeUiState

    data class ToggleVisibilityHiddenDescription(val visibility: Int) : ExamKnowledgeUiState

    data class ToggleCurrentWordTrasnalteExpanded(
        val isExpanded: Boolean,
        val translates: List<TranslateWordItem>
    ) : ExamKnowledgeUiState

    data class UpdateHiddenTranslates(
        val translates: List<TranslateWordItem>,
        val clearInputValue: Boolean = false
    ) : ExamKnowledgeUiState
}

data class ExamKnowledgeState(
    val isLoading: Boolean = false,
    var examWordList: List<ExamWord> = emptyList(),
    val examWordListEmpty: Boolean = false,
    val currentWord: ExamWord? = null,
    val isExamEnd: Boolean = false,
    val countShownHints: Int = 0,
    val allHintsShown: Boolean = false,
    val isShownHintsVisible: Boolean = false,
    val isShownVariants: Boolean = false,
    val isInoutWordInvalid: Boolean = false,
    val activeWordPosition: Int = 0,
    val hiddenTranslateDescriptionVisibility: Int = View.GONE,
)