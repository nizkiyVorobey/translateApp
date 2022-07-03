package com.example.ttanslateapp.presentation.exam.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.ttanslateapp.R
import com.example.ttanslateapp.databinding.ItemRvExamAdapterBinding
import com.example.ttanslateapp.domain.model.exam.ExamWord
import com.example.ttanslateapp.domain.model.exam.ExamWordStatus


class ExamAdapter :
    ListAdapter<ExamWord, ExamAdapterViewHolder>(ExamWordAdapterDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamAdapterViewHolder {
        return ItemRvExamAdapterBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .run { ExamAdapterViewHolder(this) }
    }

    override fun onBindViewHolder(holder: ExamAdapterViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            wordPosition.text = position.plus(1).toString()
            wordPosition.backgroundTintList = setBG(item.status, wordPosition.context)

            if (position == currentList.size - 1) {
                wordPositionDots.visibility = View.GONE
            } else {
                wordPositionDots.visibility = View.VISIBLE
            }

            if (item.isActive) {
                stroke.visibility = View.VISIBLE
            } else {
                stroke.visibility = View.INVISIBLE
            }

        }
    }

    private fun setBG(status: ExamWordStatus, context: Context): ColorStateList {
        return when (status) {
            ExamWordStatus.SUCCESS -> {
                context.resources.getColorStateList(R.color.green)
            }
            ExamWordStatus.FAIL -> {
                context.resources.getColorStateList(R.color.red)
            }
            ExamWordStatus.IN_PROCESS -> {
                context.resources.getColorStateList(R.color.blue)
            }
            ExamWordStatus.UNPROCESSED -> {
                context.resources.getColorStateList(R.color.grey)
            }
        }
    }
}