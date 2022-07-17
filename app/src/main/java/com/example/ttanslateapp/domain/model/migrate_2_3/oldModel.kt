package com.example.ttanslateapp.domain.model.migrate_2_3

import javax.inject.Inject


data class OldTranslate @Inject constructor(
    val id: String,
    val createdAt: Long,
    val updatedAt: Long,
    val value: String,
    var isHidden: Boolean
)

data class OldHints @Inject constructor(
    val id: String,
    val createdAt: Long,
    val updatedAt: Long,
    val value: String,
)
