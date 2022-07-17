package com.example.ttanslateapp.presentation.exam.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import com.example.ttanslateapp.R
import com.example.ttanslateapp.databinding.ItemRvExamAdapterBinding
import com.example.ttanslateapp.domain.model.exam.ExamWord
import com.example.ttanslateapp.domain.model.exam.ExamWordStatus
import com.example.ttanslateapp.presentation.exam.ExamKnowledgeWordsViewModel


class ExamAdapter :
    ListAdapter<ExamWord, ExamAdapterViewHolder>(ExamWordAdapterDiffCallback()) {
    var clickListener: OnItemClickListener? = null
    var handleLoadNewWords: HandleLoadNewWords? = null
    var mode: ExamMode = ExamMode.DAILY_MODE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamAdapterViewHolder {
        return ItemRvExamAdapterBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .run { ExamAdapterViewHolder(this) }
    }

    override fun onBindViewHolder(holder: ExamAdapterViewHolder, position: Int) {
        val item = getItem(position)

        handleLoadNewWords?.onLoadNewWords(position)

        with(holder.binding) {
            wordPosition.text = position.plus(1).toString()
            wordPosition.backgroundTintList = setBG(item.status, wordPosition.context)

            // TODO if mode is infinity always show dots until we reach the last item
            if (position == currentList.size - 1 && mode == ExamMode.DAILY_MODE) {
                wordPositionDots.visibility = View.GONE
            } else {
                wordPositionDots.visibility = View.VISIBLE
            }

            if (item.isActive) {
                wordPositionContainer.background = ContextCompat.getDrawable(
                    holder.binding.root.context,
                    R.drawable.prev_next_exam_button
                )
            } else {
                wordPositionContainer.background = null
            }

            holder.binding.root.setOnClickListener {
                clickListener?.onItemClick(root, position)
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

    interface HandleLoadNewWords {
        fun onLoadNewWords(position: Int)
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}