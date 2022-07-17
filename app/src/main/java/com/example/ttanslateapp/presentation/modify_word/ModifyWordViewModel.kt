package com.example.ttanslateapp.presentation.modify_word

import android.text.TextUtils
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ttanslateapp.domain.model.ModifyWord
import com.example.ttanslateapp.domain.model.ValidateResult
import com.example.ttanslateapp.domain.model.WordAudio
import com.example.ttanslateapp.domain.model.modify_word_chip.HintItem
import com.example.ttanslateapp.domain.model.modify_word_chip.Translate
import com.example.ttanslateapp.domain.use_case.DeleteWordUseCase
import com.example.ttanslateapp.domain.use_case.GetWordItemUseCase
import com.example.ttanslateapp.domain.use_case.ModifyWordUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ModifyWordViewModel @Inject constructor(
    private val modifyWordUseCase: ModifyWordUseCase,
    private val getWordItemUseCase: GetWordItemUseCase,
    private val deleteWordUseCase: DeleteWordUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<ModifyWordUiState>()
    val uiState: LiveData<ModifyWordUiState> = _uiState

    private var state = ModifyWordState()

    private fun getTimestamp(): Long = System.currentTimeMillis()
    fun getAudioFileName(): String? = state.soundFileName

    fun resetWordValueError() {
        state = state.copy(wordValueError = null)
        _uiState.value = ModifyWordUiState.EditFieldError(
            wordValueError = null,
            translatesError = state.translatesError
        )
    }

    fun resetTranslatesError() {
        state = state.copy(translatesError = null)
        _uiState.value = ModifyWordUiState.EditFieldError(
            translatesError = null,
            wordValueError = state.wordValueError
        )
    }

    fun deleteWord(wordId: Long?) {
        if (wordId != null) {
            state = state.copy(isDeleteModalOpen = false)
            viewModelScope.launch { deleteWordUseCase(wordId) }
        }
    }

    fun setIsOpenedDeleteModal(isOpened: Boolean) {
        state = state.copy(isDeleteModalOpen = isOpened)
        _uiState.value = ModifyWordUiState.ToggleOpenedDeleteModel(isOpened)
    }

    fun saveWord(
        value: String = "",
        description: String = "",
        transcription: String = "",
        langFrom: String = "",
        langTo: String = "",
        priority: String,
    ) {
        val wordValidation = validateWordValue(value)
        val priorityValidation = validationPriority(priority)
        val translatesValidation = validateTranslates(state.translates)

        val hasError =
            listOf(wordValidation, translatesValidation, priorityValidation).any { !it.successful }

        // FIXME replace to usecase
        if (hasError) {
            state = state.copy(
                wordValueError = wordValidation.errorMessage,
                translatesError = translatesValidation.errorMessage,
                priorityValidation = priorityValidation.errorMessage,
            )

            _uiState.value = ModifyWordUiState.EditFieldError(
                wordValueError = wordValidation.errorMessage,
                translatesError = translatesValidation.errorMessage,
                priorityValidation = priorityValidation.errorMessage,
            )
            return
        }

        val sound = state.soundFileName?.let { WordAudio(it) }

        val word = ModifyWord(
            id = state.editableWordId ?: 0L,
            priority = priority.toInt(),
            value = value,
            translates = state.translates,
            description = description,
            sound = sound,
            langFrom = langFrom,
            langTo = langTo,
            hints = state.hints,
            transcription = transcription,
            createdAt = state.createdAt ?: getTimestamp(),
            updatedAt = getTimestamp(),
        )

        viewModelScope.launch {
            modifyWordUseCase(word).apply {
                val isSuccess = this != -1L
                _uiState.value = ModifyWordUiState.ShowResultModify(isSuccess)
            }
        }
    }

    // wordValue which selected and passed into app as intent
    fun launchAddMode(wordValue:String)  {
        state = state.copy(wordValue = wordValue)
        _uiState.postValue(state.toUiState())
    }

    fun launchEditMode(wordId: Long) {
        val id = if (wordId == -1L) {
            error("wordId is null")
        } else {
            wordId
        }
        getWordById(id)
    }

    fun restoreRightMode() {
        _uiState.value = state.toUiState(screenIsRestored = true)
    }

    fun addTranslate(translateValue: String) {
        if (translateValue.isBlank()) return

        val translateList = state.translates
        val editableTranslate = state.editableTranslate

        val newTranslateItem =
            editableTranslate?.copy(value = translateValue, updatedAt = getTimestamp())
                ?: Translate(
                    localId = getTimestamp(),
                    value = translateValue,
                    createdAt = getTimestamp(),
                    updatedAt = getTimestamp(),
                    isHidden = false
                )

        val hintAlreadyExist =
            translateList.find { it.localId == newTranslateItem.localId }

        val newList = if (hintAlreadyExist == null) {
            translateList.plus(newTranslateItem)
        } else {
            translateList.map {
                if (it.localId == newTranslateItem.localId) {
                    return@map newTranslateItem
                }
                return@map it
            }
        }

        state = state.copy(translates = newList, editableTranslate = null)
        _uiState.postValue(ModifyWordUiState.CompleteModifyTranslate(newList))
    }

    fun addHint(hintValue: String) {
        if (hintValue.isBlank()) return

        val hintList = state.hints
        val editableHint = state.editableHint

        val newHintItem =
            editableHint?.copy(value = hintValue, updatedAt = getTimestamp())
                ?: HintItem(
                    localId = getTimestamp(),
                    value = hintValue,
                    createdAt = getTimestamp(),
                    updatedAt = getTimestamp(),
                )

        val hintAlreadyExist = hintList.find { it.localId == newHintItem.localId }

        val newList = if (hintAlreadyExist == null) {
            hintList.plus(newHintItem)
        } else {
            hintList.map {
                if (it.localId == newHintItem.localId) {
                    return@map newHintItem
                }
                return@map it
            }
        }

        state = state.copy(hints = newList, editableHint = null)
        _uiState.postValue(ModifyWordUiState.CompleteModifyHint(newList))
    }

    fun deleteTranslate(translateLocalId: Long) {
        val updatedTranslates = state.translates.filter { it.localId != translateLocalId }

        state = state.copy(editableTranslate = null, translates = updatedTranslates)
        _uiState.value = ModifyWordUiState.DeleteTranslates(translates = updatedTranslates)
    }

    fun deleteHint(hintLocalId: Long) {
        val updatedHints = state.hints.filter { it.localId != hintLocalId }

        state = state.copy(editableHint = null, hints = updatedHints)
        _uiState.value = ModifyWordUiState.DeleteHints(hints = updatedHints)
    }

    fun setEditableHint(editableHint: HintItem) {
        state = state.copy(editableHint = editableHint)
        _uiState.value = ModifyWordUiState.StartModifyHint(value = editableHint.value)
    }

    fun cancelEditableHint() {
        state = state.copy(editableHint = null)
        _uiState.value = ModifyWordUiState.CompleteModifyHint(state.hints)
    }

    fun cancelEditableTranslate() {
        state = state.copy(editableTranslate = null)
        _uiState.value = ModifyWordUiState.CompleteModifyTranslate(state.translates)
    }

    fun setEditableTranslate(editableTranslate: Translate) {
        state = state.copy(editableTranslate = editableTranslate)
        _uiState.value =
            ModifyWordUiState.StartModifyTranslate(value = editableTranslate.value)
    }

    fun toggleIsHiddenTranslate(item: Translate) {
        val newTranslateList = state.translates.map {
            if (it.localId == item.localId) return@map it.copy(isHidden = !item.isHidden)
            return@map it
        }
        state = state.copy(translates = newTranslateList)
        _uiState.value = ModifyWordUiState.CompleteModifyTranslate(translates = newTranslateList)
    }

    fun toggleVisibleAdditionalField() {
        val newIsAdditionalFieldVisible =
            if (state.isAdditionalFieldVisible == View.VISIBLE) View.GONE else View.VISIBLE
        state = state.copy(isAdditionalFieldVisible = newIsAdditionalFieldVisible)
        _uiState.value = ModifyWordUiState.ShowAdditionalFields(newIsAdditionalFieldVisible)
    }

    fun updateAudio(fileName: String?) {
        viewModelScope.launch {
            state = state.copy(soundFileName = fileName)
            _uiState.value = ModifyWordUiState.UpdateSoundFile(fileName)

            state.editableWordId?.let {
                val sound = if (fileName == null) null else WordAudio(fileName = fileName)
                modifyWordUseCase.modifyOnlySound(it, sound = sound)
            }
        }
    }

    private fun validateWordValue(value: String): ValidateResult {
        return if (value.isBlank()) {
            ValidateResult(successful = false, errorMessage = "this field is required")
        } else {
            ValidateResult(successful = true)
        }
    }

    private fun validationPriority(value: String): ValidateResult {
        return if (value.isBlank()) {
            ValidateResult(successful = false, errorMessage = "this field is required")
        } else if (!TextUtils.isDigitsOnly(value)) {
            ValidateResult(successful = false, errorMessage = "must contain only digits")
        } else {
            ValidateResult(successful = true)
        }
    }

    private fun validateTranslates(value: List<Translate>): ValidateResult {
        return if (value.isEmpty()) {
            ValidateResult(successful = false, errorMessage = "this field is required")
        } else {
            ValidateResult(successful = true)
        }
    }

    private fun getWordById(id: Long) {
        _uiState.value = ModifyWordUiState.IsWordLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val word = getWordItemUseCase(id)
            state = state.copy(
                wordValue = word.value,
                translates = word.translates,
                transcription = word.transcription,
                description = word.description,
                selectableLanguage = word.langTo,
                hints = word.hints,
                soundFileName = word.sound?.fileName,
                editableWordId = word.id,
                langFrom = word.langFrom,
                createdAt = word.createdAt,
                priority = word.priority
            )
            // or you could avoid `withContext` and just use `uiState.postValue()`
            withContext(Dispatchers.Main) {
                _uiState.value = ModifyWordUiState.IsWordLoading(false)
                _uiState.value = state.toUiState()
            }
        }
    }

    private fun ModifyWordState.toUiState(screenIsRestored: Boolean = false): ModifyWordUiState {
        return ModifyWordUiState.PreScreen(
            wordValue = wordValue,
            wordValueError = wordValueError,
            priority = priority,
            transcription = transcription,
            description = description,
            translates = translates,
            translatesError = translatesError,
            editableTranslate = state.editableTranslate,
            hints = hints,
            editableHint = state.editableHint,
            langFrom = langFrom,
            soundFileName = soundFileName,
            isAdditionalFieldVisible = isAdditionalFieldVisible,
            screenIsRestored = screenIsRestored,
            isDeleteModalOpen = isDeleteModalOpen,
        )
    }
}