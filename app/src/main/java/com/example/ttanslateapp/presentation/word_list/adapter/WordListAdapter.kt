package com.example.ttanslateapp.presentation.word_list.adapter

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.ttanslateapp.databinding.ItemWordRvBinding
import com.example.ttanslateapp.domain.model.WordRV

class WordListAdapter : ListAdapter<WordRV, WordItemViewHolder>(WordListAdapterDiffCallback()) {
    var onClickListener: OnClickListener? = null
    private val player = MediaPlayer()
    private val playingList = mutableMapOf<Long, Boolean>()

    private val expandedList = hashMapOf<Long, Boolean>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordItemViewHolder {
        return ItemWordRvBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .run { WordItemViewHolder(this, player, playingList,expandedList) }
    }

    override fun onBindViewHolder(holder: WordItemViewHolder, position: Int) {
        val word = getItem(position)
        holder.bind(word, onClickListener)
    }

    interface OnClickListener {
        fun onRootClickListener(wordId: Long)
    }
}