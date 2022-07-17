package com.example.ttanslateapp.data.mapper

import com.example.ttanslateapp.data.model.*
import com.example.ttanslateapp.domain.model.ModifyWord
import com.example.ttanslateapp.domain.model.WordRV
import com.example.ttanslateapp.domain.model.exam.ExamAnswerVariant
import com.example.ttanslateapp.domain.model.exam.ExamWord
import com.example.ttanslateapp.domain.model.exam.ExamWordStatus
import com.example.ttanslateapp.domain.model.modify_word_chip.HintItem
import com.example.ttanslateapp.domain.model.modify_word_chip.Translate
import javax.inject.Inject

class WordMapper @Inject constructor() {
    fun wordListDbToWordList(dbWordList: List<WordFullDb>) = dbWordList.map {
        wordFullDbToWordRv(it)
    }

    private fun translateDbToLocal(translate: TranslateDb): Translate =
        Translate(
            id = translate.id,
            localId = translate.id,
            createdAt = translate.createdAt,
            updatedAt = translate.updatedAt,
            value = translate.value,
            isHidden = translate.isHidden,
        )

    fun translateLocalToDb(
        translate: Translate,
        wordId: Long
    ): TranslateDb =
        TranslateDb(
            id = translate.id,
            createdAt = translate.createdAt,
            updatedAt = translate.updatedAt,
            value = translate.value,
            isHidden = translate.isHidden,
            wordId = wordId
        )

    fun hintLocalToDb(hint: HintItem, wordId: Long): HintDb =
        HintDb(
            createdAt = hint.createdAt,
            updatedAt = hint.updatedAt,
            value = hint.value,
            wordId = wordId
        )

    fun hintDbToLocal(hint: HintDb): HintItem =
        HintItem(
            id = hint.id,
            localId = hint.id,
            createdAt = hint.createdAt,
            updatedAt = hint.updatedAt,
            value = hint.value,
        )


    private fun wordFullDbToWordRv(wordDb: WordFullDb): WordRV = WordRV(
        id = wordDb.wordInfo.id,
        value = wordDb.wordInfo.value,
        translates = wordDb.translates.map { translateDbToLocal(it) },
        description = wordDb.wordInfo.description,
        sound = wordDb.wordInfo.sound,
        langFrom = wordDb.wordInfo.langFrom,
        langTo = wordDb.wordInfo.langTo,
        transcription = wordDb.wordInfo.transcription,
    )

    fun wordFullDbToModifyWord(wordDb: WordFullDb): ModifyWord = ModifyWord(
        id = wordDb.wordInfo.id,
        priority = wordDb.wordInfo.priority,
        value = wordDb.wordInfo.value,
        translates = wordDb.translates.map { translateDbToLocal(it) },
        description = wordDb.wordInfo.description,
        sound = wordDb.wordInfo.sound,
        langFrom = wordDb.wordInfo.langFrom,
        langTo = wordDb.wordInfo.langTo,
        hints = wordDb.hints.map { hintDbToLocal(it) },
        transcription = wordDb.wordInfo.transcription,
        createdAt = wordDb.wordInfo.createdAt,
        updatedAt = wordDb.wordInfo.updatedAt,
    )

    fun modifyWordToWordInfoDb(modifyWord: ModifyWord) = WordInfoDb(
        id = modifyWord.id,
        priority = modifyWord.priority,
        value = modifyWord.value,
        description = modifyWord.description,
        sound = modifyWord.sound,
        langFrom = modifyWord.langFrom,
        langTo = modifyWord.langTo,
        transcription = modifyWord.transcription,
        createdAt = modifyWord.createdAt,
        updatedAt = modifyWord.updatedAt,
    )

    fun wordFullDbToExamWord(wordDb: WordFullDb): ExamWord = ExamWord(
        id = wordDb.wordInfo.id,
        value = wordDb.wordInfo.value,
        translates = wordDb.translates.map { translateDbToLocal(it) },
        hints = wordDb.hints.map { hintDbToLocal(it) } ?: emptyList(),
        priority = wordDb.wordInfo.priority,
        status = ExamWordStatus.UNPROCESSED,
        answerVariants = emptyList(),
    )


    fun examAnswerToExamAnswerDb(examAnswer: ExamAnswerVariant) = PotentialExamAnswerDb(
        id = examAnswer.id,
        value = examAnswer.value,
    )

    fun examAnswerDbToExamAnswer(examAnswer: PotentialExamAnswerDb) = ExamAnswerVariant(
        id = examAnswer.id,
        value = examAnswer.value,
    )
}
