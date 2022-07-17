package com.example.ttanslateapp.presentation.exam.adapter

import android.view.View
import com.example.ttanslateapp.domain.model.exam.ExamWord
import com.example.ttanslateapp.domain.model.exam.ExamWordStatus
import com.example.ttanslateapp.domain.model.modify_word_chip.Translate

sealed interface ExamKnowledgeUiState {
    object IsLoadingWords : ExamKnowledgeUiState
    object LoadedEmptyList : ExamKnowledgeUiState

    data class LoadedNewPage(var examWordList: List<ExamWord>, val activeWordPosition: Int) :
        ExamKnowledgeUiState

    data class RestoreUI(
        val isLoading: Boolean,
        var examWordList: List<ExamWord> = emptyList(),
        val examWordListEmpty: Boolean = false,
        val currentWord: ExamWord? = null,
        val isExamEnd: Boolean = false,
        val isInputWordInvalid: Boolean = false,
        val activeWordPosition: Int = 0,
        val hiddenTranslateDescriptionVisibility: Int = View.GONE,
        val mode: ExamMode,
        val isModeDialogOpen: Boolean
    ) : ExamKnowledgeUiState

    data class LoadedWordsSuccess(
        val examWordList: List<ExamWord>,
        val currentWord: ExamWord,
        val activeWordPosition: Int,
        val mode: ExamMode
    ) : ExamKnowledgeUiState

    data class HandleAnswerInput(
        val value: String,
        val userGaveAnswer: Boolean,
        val selectedVariantValue: String?
    ) :
        ExamKnowledgeUiState

    data class CheckedAnswer(
        val status: ExamWordStatus = ExamWordStatus.UNPROCESSED,
        val examWordList: List<ExamWord>,
        val currentWord: ExamWord,
        val isExamEnd: Boolean = false,
        val givenAnswer: String
    ) : ExamKnowledgeUiState

    data class QuestionNavigation(
        val examWordList: List<ExamWord>,
        val currentWord: ExamWord,
        val activeWordPosition: Int,
    ) : ExamKnowledgeUiState

    data class ToggleIsVariantsExpanded(val isExpanded: Boolean) : ExamKnowledgeUiState
    data class ToggleExpandedHint(val isExpanded: Boolean, val allHintsIsShown: Boolean) :
        ExamKnowledgeUiState

    data class ShowNextHint(
        val allHintsIsShown: Boolean,
        val currentWord: ExamWord
    ) : ExamKnowledgeUiState

    data class SelectVariants(
        val selectedVariantValue: String?,
    ) : ExamKnowledgeUiState

    data class ToggleHiddenDescriptionExpanded(val isExpanded: Boolean) : ExamKnowledgeUiState

    data class ToggleCurrentWordTrasnalteExpanded(
        val isExpanded: Boolean,
        val translates: List<Translate>
    ) : ExamKnowledgeUiState

    data class UpdateHiddenTranslates(
        val translates: List<Translate>,
        val clearInputValue: Boolean = false
    ) : ExamKnowledgeUiState

    data class ToggleOpenModeDialog(val isOpened: Boolean) : ExamKnowledgeUiState
}

data class ExamKnowledgeState(
    val isLoading: Boolean = false,
    var examWordList: List<ExamWord> = emptyList(),
    val examWordListEmpty: Boolean = false,
    val currentWord: ExamWord? = null,
    val isExamEnd: Boolean = false,
    val isInputWordInvalid: Boolean = false,
    val activeWordPosition: Int = 0,
    val mode: ExamMode = ExamMode.DAILY_MODE,
    val isModeDialogOpen: Boolean = false
)

enum class ExamMode {
    DAILY_MODE, INFINITY_MODE
}

