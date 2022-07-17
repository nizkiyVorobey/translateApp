package com.example.ttanslateapp.di

import android.app.Application
import com.example.ttanslateapp.data.database.*
import com.example.ttanslateapp.data.in_memory_storage.InMemoryStorage
import com.example.ttanslateapp.data.in_memory_storage.LocalCache
import com.example.ttanslateapp.domain.ExamWordAnswerRepository
import com.example.ttanslateapp.domain.TranslatedWordRepository
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface DataModule {

    @ApplicationScope
    @Binds
    fun bindTranslatedWordRepository(impl: TranslatedWordRepositoryImpl): TranslatedWordRepository

    @ApplicationScope
    @Binds
    fun bindExamWordAnswerRepository(impl: ExamWordAnswerRepositoryImpl): ExamWordAnswerRepository

    companion object {
        @Provides
        @ApplicationScope
        fun providesTranslatedWordDao(context: Application): TranslatedWordDao {
            return AppDatabase.getInstance(context).translatedWordDao()
        }

        @Provides
        @ApplicationScope
        fun providesExamWordAnswerDao(context: Application): ExamWordAnswerDao {
            return AppDatabase.getInstance(context).examWordAnswerDao()
        }

        @Provides
        fun providesInMemoryStorage(): InMemoryStorage = LocalCache()
    }
}