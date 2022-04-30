package com.example.ttanslateapp.di

import androidx.lifecycle.ViewModel
import com.example.ttanslateapp.presentation.modify_word.ModifyWordViewModel
import com.example.ttanslateapp.presentation.word_list.WordListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {
    @IntoMap
    @ViewModelKey(ModifyWordViewModel::class)
    @Binds
    fun bindModifyWordViewModel(impl: ModifyWordViewModel): ViewModel

    @IntoMap
    @ViewModelKey(WordListViewModel::class)
    @Binds
    fun bindWordListViewModel(impl: WordListViewModel): ViewModel
}