package com.example.ttanslateapp.presentation.exam.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.ttanslateapp.domain.model.exam.ExamWord

class ExamWordAdapterDiffCallback : DiffUtil.ItemCallback<ExamWord>() {
    override fun areItemsTheSame(oldItem: ExamWord, newItem: ExamWord): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ExamWord, newItem: ExamWord): Boolean {
        return oldItem == newItem
    }
}