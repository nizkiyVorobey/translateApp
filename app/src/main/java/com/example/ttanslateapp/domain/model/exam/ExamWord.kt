package com.example.ttanslateapp.domain.model.exam

import com.example.ttanslateapp.domain.model.modify_word_chip.HintItem
import com.example.ttanslateapp.domain.model.modify_word_chip.Translate

data class ExamWord(
    val id: Long,
    val value: String,
    val translates: List<Translate>,
    val hints: List<HintItem>,
    val priority: Int,

    val status: ExamWordStatus,
    val isFreeze: Boolean = false,
    val answerVariants: List<ExamAnswerVariant>,
    val givenAnswer: String? = null,
    val isActive: Boolean = false,
    val isTranslateExpanded: Boolean = false,
    val isHiddenTranslateDescriptionExpanded: Boolean = false,

    val isVariantsExpanded: Boolean = false,
    val selectedVariantValue: String? = null,

    val isHintsExpanded: Boolean = false,
    val allHintsIsShown: Boolean = false,
    val countOfRenderHints: Int = 0,

    )

