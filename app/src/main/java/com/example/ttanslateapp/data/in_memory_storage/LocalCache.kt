package com.example.ttanslateapp.data.in_memory_storage

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

@Singleton
class LocalCache: InMemoryStorage {
    override val data = MutableStateFlow("")

    override fun setData(value: String) {
        data.value = value
    }
}