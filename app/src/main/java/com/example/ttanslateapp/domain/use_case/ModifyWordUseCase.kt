package com.example.ttanslateapp.domain.use_case

import com.example.ttanslateapp.data.mapper.WordMapper
import com.example.ttanslateapp.domain.TranslatedWordRepository
import com.example.ttanslateapp.domain.model.ModifyWord
import com.example.ttanslateapp.domain.model.WordAudio
import com.example.ttanslateapp.domain.model.modify_word_chip.TranslateWordItem
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class ModifyWordUseCase @Inject constructor(
    private val repository: TranslatedWordRepository,
    private val mapper: WordMapper
) {
    suspend operator fun invoke(word: ModifyWord) =
        repository.modifyWord(mapper.modifyWordToDbWord(word))

//    suspend fun updateTranslates(translateList: List<TranslateWordItem>) {
//        val gson = Gson()
//        repository.updateWordTranslates(gson.toJson(translateList))
//    }

    suspend fun modifyOnlySound(
        id: Long,
        sound: WordAudio?, // english sound
    ) = coroutineScope {
        async {
            val oldWord = repository.getWordById(id)
            val word = oldWord.copy(sound = sound)
            repository.modifyWord(mapper.modifyWordToDbWord(word))
        }
    }.await()

}