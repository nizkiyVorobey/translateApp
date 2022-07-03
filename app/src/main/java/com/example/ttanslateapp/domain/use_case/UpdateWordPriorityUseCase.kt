package com.example.ttanslateapp.domain.use_case

import com.example.ttanslateapp.domain.TranslatedWordRepository
import javax.inject.Inject

class UpdateWordPriorityUseCase @Inject constructor(val repository: TranslatedWordRepository) {
    suspend operator fun invoke(priority: Int, id: Long) =
        repository.updatePriorityById(priority, id)
}