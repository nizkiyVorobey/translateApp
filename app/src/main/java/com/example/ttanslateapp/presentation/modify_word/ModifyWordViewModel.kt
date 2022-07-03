package com.example.ttanslateapp.presentation.modify_word

import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ttanslateapp.domain.model.ModifyWord
import com.example.ttanslateapp.domain.model.WordAudio
import com.example.ttanslateapp.domain.model.modify_word_chip.HintItem
import com.example.ttanslateapp.domain.model.modify_word_chip.TranslateWordItem
import com.example.ttanslateapp.domain.use_case.GetWordItemUseCase
import com.example.ttanslateapp.domain.use_case.ModifyWordUseCase
import com.example.ttanslateapp.domain.use_case.ValidateResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ModifyWordViewModel @Inject constructor(
    private val modifyWordUseCase: ModifyWordUseCase,
    private val getWordItemUseCase: GetWordItemUseCase,
) : ViewModel() {

    private val _uiState = MutableLiveData<ModifyWordUiState>()
    val uiState: LiveData<ModifyWordUiState> = _uiState

    private var state = ModifyWordState()

    private fun getTimestamp(): Long = System.currentTimeMillis()
    fun getAudioFileName(): String? = state.soundFileName

    fun resetWordValueError() {
        Timber.d("resetWordValueError")
        _uiState.value = ModifyWordUiState.EditFieldError(
            wordValueError = null,
            translatesError = state.translatesError
        )
    }

    fun resetTranslatesError() {
        Timber.d("resetTranslatesError")

        _uiState.value = ModifyWordUiState.EditFieldError(
            translatesError = null,
            wordValueError = state.wordValueError
        )
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

    private fun validateTranslates(value: List<TranslateWordItem>): ValidateResult {
        return if (value.isEmpty()) {
            ValidateResult(successful = false, errorMessage = "this field is required")
        } else {
            ValidateResult(successful = true)
        }
    }


    override fun onCleared() {
//        _state.value = _state.value?.copy(editableWordId = null)
    }

//    fun setWordValue(value: String) = _state = _state.copy(wordValue = value)
//    fun setDescription(value: String) = _state = _state.copy(description = value)

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
                _uiState.value = ModifyWordUiState.ShowResultModify(this)
            }
        }
    }

    fun launchAddMode() = _uiState.postValue(state.toUiState())
    fun launchEditMode(wordId: Long) {
        val id = if (wordId == -1L) {
            error("wordId is null")
        } else {
            wordId
        }
        getWordById(id)
    }

    private fun getWordById(id: Long) {
        _uiState.value = ModifyWordUiState.IsWordLoading(true)
        val loadedWord = viewModelScope.async(Dispatchers.IO) {
            val word = getWordItemUseCase(id)
            Log.d("getWordItemUseCase","getWordItemUseCase $word")

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
        }

        viewModelScope.launch {
            loadedWord.await()
            _uiState.value = ModifyWordUiState.IsWordLoading(false)
            _uiState.value = state.toUiState()
        }
    }

    private fun ModifyWordState.toUiState(): ModifyWordUiState {
        return ModifyWordUiState.PreScreen(
            wordValue = wordValue,
            wordValueError = wordValueError,
            priority = priority,
            transcription = transcription,
            description = description,
            translates = translates,
            translatesError = translatesError,
            hints = hints,
            langFrom = langFrom,
            soundFileName = soundFileName,
            isAdditionalFieldVisible = isAdditionalFieldVisible,
        )
    }

    fun addTranslate(translateValue: String) {
        if (translateValue.isBlank()) return

        val translateList = state.translates
        val editableTranslate = state.editableTranslate

        val newTranslateItem =
            editableTranslate?.copy(value = translateValue, updatedAt = getTimestamp())
                ?: TranslateWordItem(
                    value = translateValue,
                    id = UUID.randomUUID().toString(),
                    createdAt = getTimestamp(),
                    updatedAt = getTimestamp(),
                    isHidden = false
                )

        val hintAlreadyExist = translateList.find { it.id == newTranslateItem.id }

        val newList = if (hintAlreadyExist == null) {
            translateList.plus(newTranslateItem)
        } else {
            translateList.map {
                if (it.id == newTranslateItem.id) {
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
                    value = hintValue,
                    id = UUID.randomUUID().toString(),
                    createdAt = getTimestamp(),
                    updatedAt = getTimestamp()
                )

        val hintAlreadyExist = hintList.find { it.id == newHintItem.id }

        val newList = if (hintAlreadyExist == null) {
            hintList.plus(newHintItem)
        } else {
            hintList.map {
                if (it.id == newHintItem.id) {
                    return@map newHintItem
                }
                return@map it
            }
        }

        state = state.copy(hints = newList, editableHint = null)
        _uiState.postValue(ModifyWordUiState.CompleteModifyHint(newList))
    }

    fun deleteTranslate(translateId: String) {
        val updatedTranslates = state.translates.filter { it.id != translateId }

        state = state.copy(editableTranslate = null, translates = updatedTranslates)
        _uiState.value = ModifyWordUiState.DeleteTranslates(translates = updatedTranslates)
    }

    fun deleteHint(hintId: String) {
        val updatedHints = state.hints.filter { it.id != hintId }

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

    fun setEditableTranslate(editableTranslateWordItem: TranslateWordItem) {
        state = state.copy(editableTranslate = editableTranslateWordItem)
        _uiState.value =
            ModifyWordUiState.StartModifyTranslate(value = editableTranslateWordItem.value)
    }

    fun toggleIsHiddenTranslate(item: TranslateWordItem) {
        val newTranslateList = state.translates.map {
            if (it.id == item.id) return@map it.copy(isHidden = !item.isHidden)
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
}