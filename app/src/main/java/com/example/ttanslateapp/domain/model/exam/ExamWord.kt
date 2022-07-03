package com.example.ttanslateapp.domain.model.exam

import com.example.ttanslateapp.domain.model.modify_word_chip.HintItem
import com.example.ttanslateapp.domain.model.modify_word_chip.TranslateWordItem
import com.example.ttanslateapp.presentation.exam.AnswerResult

data class ExamWord(
    val id: Long,
    val value: String,
    val translates: List<TranslateWordItem>,
    val hints: List<HintItem>,
    val priority: Int,

    val status: ExamWordStatus,
    val isFreeze: Boolean = false,
    val answerVariants: List<ExamAnswerVariant>,
    val givenAnswer: String? = null,
    val isTranslateExpanded: Boolean = false,
    val isActive: Boolean = false,
)

