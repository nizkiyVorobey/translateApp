package com.example.ttanslateapp.presentation.modify_word.adapter.translate

import androidx.recyclerview.widget.DiffUtil
import com.example.ttanslateapp.domain.model.Chip
import com.example.ttanslateapp.domain.model.edit.TranslateWordItem

class TranslateAdapterDiffCallback : DiffUtil.ItemCallback<TranslateWordItem>() {
    override fun areItemsTheSame(oldItem: TranslateWordItem, newItem: TranslateWordItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TranslateWordItem, newItem: TranslateWordItem): Boolean {
        return oldItem == newItem
    }
}