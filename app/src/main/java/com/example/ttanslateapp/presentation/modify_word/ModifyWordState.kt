package com.example.ttanslateapp.presentation.modify_word

import android.view.View
import com.example.ttanslateapp.domain.model.ModifyWord
import com.example.ttanslateapp.domain.model.modify_word_chip.HintItem
import com.example.ttanslateapp.domain.model.modify_word_chip.Translate

sealed interface ModifyWordUiState {
    data class IsWordLoading(
        val isLoading: Boolean
    ) : ModifyWordUiState

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
        val translates: List<Translate>,
        val editableTranslate: Translate? = null,
        val hints: List<HintItem>,
        val editableHint: HintItem? = null,
        val langFrom: String,
        val soundFileName: String?,
        val isAdditionalFieldVisible: Int,
        val wordValueError: String? = null,
        val translatesError: String? = null,
        val screenIsRestored: Boolean? = false,
        val isDeleteModalOpen: Boolean = false,
    ) : ModifyWordUiState

    data class ShowAdditionalFields(val isVisible: Int) : ModifyWordUiState
    data class ShowResultModify(val isSuccess: Boolean) : ModifyWordUiState

    data class StartModifyTranslate(val value: String) : ModifyWordUiState
    data class CompleteModifyTranslate(val translates: List<Translate>) : ModifyWordUiState

    data class StartModifyHint(val value: String) : ModifyWordUiState
    data class CompleteModifyHint(val hints: List<HintItem>) : ModifyWordUiState

    data class DeleteTranslates(val translates: List<Translate>) : ModifyWordUiState
    data class DeleteHints(val hints: List<HintItem>) : ModifyWordUiState

    data class UpdateSoundFile(val name: String?) : ModifyWordUiState
    data class ToggleOpenedDeleteModel(val isOpened: Boolean): ModifyWordUiState
}


data class ModifyWordState(
    val wordValue: String = "",
    val transcription: String = "",
    val description: String = "",
    val selectableLanguage: String = "ua",
    val translates: List<Translate> = emptyList(),
    val hints: List<HintItem> = emptyList(),
    val editableHint: HintItem? = null,
    val editableTranslate: Translate? = null,
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
    val isDeleteModalOpen: Boolean = false,
)