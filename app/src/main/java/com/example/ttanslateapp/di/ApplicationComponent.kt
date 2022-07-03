package com.example.ttanslateapp.di

import android.app.Application
import com.example.ttanslateapp.data.workers.AlarmReceiver
import com.example.ttanslateapp.presentation.MainActivity
import com.example.ttanslateapp.presentation.exam.ExamKnowledgeWordsFragment
import com.example.ttanslateapp.presentation.modify_word.ModifyWordFragment
import com.example.ttanslateapp.presentation.settings.SettingsFragment
import com.example.ttanslateapp.presentation.word_list.WordListFragment
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(modules = [DataModule::class, ViewModelModule::class])
interface ApplicationComponent {

    fun inject(mainActivity: MainActivity)
    fun inject(alarmReceiver: AlarmReceiver)
    fun inject(modifyWordFragment: ModifyWordFragment)
    fun inject(wordListFragment: WordListFragment)
    fun inject(examKnowledgeWordsFragment: ExamKnowledgeWordsFragment)
    fun inject(settingsFragment: SettingsFragment)

    @Component.Factory
    interface ApplicationFactory {
        fun create(@BindsInstance context: Application): ApplicationComponent
    }
}