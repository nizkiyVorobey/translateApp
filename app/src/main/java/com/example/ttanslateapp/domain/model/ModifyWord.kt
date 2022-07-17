package com.example.ttanslateapp.domain.model

import com.example.ttanslateapp.domain.model.modify_word_chip.HintItem
import com.example.ttanslateapp.domain.model.modify_word_chip.Translate
import javax.inject.Inject

data class ModifyWord @Inject constructor(
    val id: Long = UNDEFINED_ID,
    val priority: Int = DEFAULT_PRIORITY,
    val value: String,
    val translates: List<Translate>,
    val description: String,
    val sound: WordAudio?, // english sound
    val langFrom: String,
    val langTo: String,
    val hints: List<HintItem>,
    val transcription: String,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        const val UNDEFINED_ID = 0L
        const val DEFAULT_PRIORITY = 5
    }
}
