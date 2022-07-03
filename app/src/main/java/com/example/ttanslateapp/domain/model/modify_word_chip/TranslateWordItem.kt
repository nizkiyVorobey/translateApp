package com.example.ttanslateapp.domain.model.modify_word_chip

import javax.inject.Inject

data class TranslateWordItem @Inject constructor(
    override val id: String,
    override val createdAt: Long,
    override val updatedAt: Long,
    override val value: String,
    var isHidden: Boolean // if translate is hidden - it will not shown in wordList, but on exam screen validation will be include this word
) : Chip
