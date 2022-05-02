package com.example.ttanslateapp.domain

import androidx.lifecycle.LiveData
import com.example.ttanslateapp.data.model.TranslatedWordDb
import com.example.ttanslateapp.domain.model.ModifyWord
import com.example.ttanslateapp.domain.model.WordRV

interface TranslatedWordRepository {
    suspend fun getWordList(): LiveData<List<WordRV>>

    suspend fun getWordById(id: Long): ModifyWord

    suspend fun deleteWord(id: Long): Boolean

    suspend fun modifyWord(translatedWordDb: TranslatedWordDb): Boolean

    suspend fun searchWordList(query: String): LiveData<List<WordRV>>
}