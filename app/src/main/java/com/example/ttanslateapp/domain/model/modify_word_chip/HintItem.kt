package com.example.ttanslateapp.domain.model.modify_word_chip

import javax.inject.Inject

data class HintItem @Inject constructor(
    override val id: Long = DEFAULT_ID,
    override val localId: Long,
    override val createdAt: Long,
    override val updatedAt: Long,
    override val value: String,
): Chip {
    companion object {
        const val DEFAULT_ID = 0L
    }
}
