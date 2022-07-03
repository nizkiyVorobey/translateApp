package com.example.ttanslateapp.domain.use_case

import com.example.ttanslateapp.domain.TranslatedWordRepository
import javax.inject.Inject

class GetWordItemUseCase @Inject constructor(
    private val repository: TranslatedWordRepository
){
    suspend operator fun invoke(wordId: Long) = repository.getWordById(wordId)
}