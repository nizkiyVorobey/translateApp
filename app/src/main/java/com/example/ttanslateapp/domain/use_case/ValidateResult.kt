package com.example.ttanslateapp.domain.use_case

data class ValidateResult(
    val successful: Boolean = true,
    val errorMessage: String? = null
)
