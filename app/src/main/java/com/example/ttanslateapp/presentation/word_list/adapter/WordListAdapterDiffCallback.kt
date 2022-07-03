package com.example.ttanslateapp.presentation.word_list.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.ttanslateapp.domain.model.WordRV

class WordListAdapterDiffCallback : DiffUtil.ItemCallback<WordRV>() {
    override fun areItemsTheSame(oldItem: WordRV, newItem: WordRV): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WordRV, newItem: WordRV): Boolean {
        return oldItem == newItem
    }
}