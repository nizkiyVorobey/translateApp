package com.example.ttanslateapp.presentation.modify_word.adapter

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ttanslateapp.domain.model.Chip

abstract class ModifyWordAdapter<
        T : Chip,
        VH : RecyclerView.ViewHolder,
        M : ModifyWordAdapter.OnItemClickListener<T>> : ListAdapter<T, VH>(Differ()) {

    var clickListener: M? = null

    class Differ<T : Chip> : DiffUtil.ItemCallback<T>() {
        @SuppressLint("DiffUtilEquals")
        /** As long as subclasses of Chip are data classes, this suppress can be used  */
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem.id == newItem.id
    }

    interface OnItemClickListener<in T : Chip> {
        fun onItemClick(it: View, item: T)
    }

    interface OnItemMultiClickListener<in T : Chip> : OnItemClickListener<T> {
        fun onItemDeleteClick(item: T)
    }
}