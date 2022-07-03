package com.example.ttanslateapp.presentation.modify_word

import android.view.View
import com.example.ttanslateapp.domain.model.ModifyWord
import com.example.ttanslateapp.domain.model.modify_word_chip.HintItem
import com.example.ttanslateapp.domain.model.modify_word_chip.TranslateWordItem
import java.util.*

sealed interface ModifyWordUiState {
    data class IsWordLoading(val isLoading: Boolean) : ModifyWordUiState
    data class EditFieldError(
        val wordValueError: String? = null,
        val translatesError: String? = null,
        val priorityValidation: String? = null,
    ) : ModifyWordUiState

    data class PreScreen(
        val wordValue: String,
        val transcription: String,
        val priority: Int,
        val description: String,
        val translates: List<TranslateWordItem>,
        val hints: List<HintItem>,
        val langFrom: String,
        val soundFileName: String?,
        val isAdditionalFieldVisible: Int,
        val wordValueError: String? = null,
        val translatesError: String? = null
    ) : ModifyWordUiState

    data class ShowAdditionalFields(val isVisible: Int) : ModifyWordUiState
    data class ShowResultModify(val isSuccess: Boolean) : ModifyWordUiState

    data class StartModifyTranslate(val value: String) : ModifyWordUiState
    data class CompleteModifyTranslate(val translates: List<TranslateWordItem>) : ModifyWordUiState

    data class StartModifyHint(val value: String) : ModifyWordUiState
    data class CompleteModifyHint(val hints: List<HintItem>) : ModifyWordUiState

    data class DeleteTranslates(val translates: List<TranslateWordItem>) : ModifyWordUiState
    data class DeleteHints(val hints: List<HintItem>) : ModifyWordUiState

    data class UpdateSoundFile(val name: String?) : ModifyWordUiState
}


data class ModifyWordState(
    val wordValue: String = "",
    val transcription: String = "",
    val description: String = "",
    val selectableLanguage: String = "ua",
    val translates: List<TranslateWordItem> = emptyList(),
    val hints: List<HintItem> = emptyList(),
    val editableHint: HintItem? = null,
    val editableTranslate: TranslateWordItem? = null,
    val langFrom: String = "cz",
    val soundFileName: String? = null,
    val priority: Int = ModifyWord.DEFAULT_PRIORITY,


    val translatesError: String? = null,
    val wordValueError: String? = null,
    val priorityValidation: String? = null,

    val isAdditionalFieldVisible: Int = View.GONE,
    val savedWordResult: Boolean = false,
    val editableWordId: Long? = null,

    val createdAt: Long? = null,
)