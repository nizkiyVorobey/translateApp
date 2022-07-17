package com.example.ttanslateapp.domain.use_case

import com.example.ttanslateapp.data.mapper.WordMapper
import com.example.ttanslateapp.domain.TranslatedWordRepository
import com.example.ttanslateapp.domain.model.ModifyWord
import com.example.ttanslateapp.domain.model.WordAudio
import com.example.ttanslateapp.domain.model.modify_word_chip.Translate
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class ModifyWordUseCase @Inject constructor(
    private val repository: TranslatedWordRepository,
    private val mapper: WordMapper,
) {
    suspend operator fun invoke(word: ModifyWord): Long = coroutineScope {
        val wordId = repository.modifyWordInfo(wordInfoDb = mapper.modifyWordToWordInfoDb(word))
        val translates = repository.modifyWordTranslates(
            translates = word.translates.map {
                mapper.translateLocalToDb(
                    translate = it,
                    wordId = wordId
                )
            }
        )
        val hints = repository.modifyWordHints(
            hints = word.hints.map {
                mapper.hintLocalToDb(
                    hint = it,
                    wordId = wordId
                )
            }
        )

        return@coroutineScope wordId
    }


    suspend fun modifyTranslates(
        wordId: Long,
        translates: List<Translate>
    ) = repository.modifyWordTranslates(translates.map {
        mapper.translateLocalToDb(
            wordId = wordId,
            translate = it
        )
    })

    suspend fun modifyOnlySound(
        id: Long,
        sound: WordAudio?, // english sound
    ) = coroutineScope {
        async {
            val oldWord = repository.getWordById(id)
            val word = oldWord.copy(sound = sound)
            repository.modifyWordInfo(mapper.modifyWordToWordInfoDb(word))
        }
    }.await()

}