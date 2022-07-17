package com.example.ttanslateapp.presentation.modify_word.adapter.translate

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.ttanslateapp.R
import com.example.ttanslateapp.databinding.ItemTranslateChipBinding
import com.example.ttanslateapp.domain.model.modify_word_chip.Translate
import com.example.ttanslateapp.presentation.modify_word.adapter.ModifyWordAdapter

private typealias ClickListener = ModifyWordAdapter.OnItemClickListener<Translate>

class TranslateAdapter :
    ModifyWordAdapter<Translate, TranslateItemViewHolder, ClickListener>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslateItemViewHolder {
        return ItemTranslateChipBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .run { TranslateItemViewHolder(this) }
    }

    private fun applyHiddenStyle(isHidden: Boolean, binding: ItemTranslateChipBinding) {
        val context = binding.root.context

        binding.chipItem.backgroundTintList = ColorStateList.valueOf(R.color.blue)
        if (isHidden) {
            binding.chipItem.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.light_grey)
            binding.chipItem.alpha = 0.5f
            binding.chipItem.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            binding.chipItem.backgroundTintList = null
            binding.chipItem.alpha = 1.0f
            binding.chipItem.setTextColor(ContextCompat.getColor(context, androidx.media.R.color.secondary_text_default_material_light));
        }
    }

    override fun onBindViewHolder(
        holder: TranslateItemViewHolder,
        position: Int
    ) {
        val chip: Translate = getItem(position)
        with(holder.binding) {
            chipItem.text = chip.value

            applyHiddenStyle(chip.isHidden, holder.binding)

            root.setOnClickListener {
                clickListener?.onItemClick(it, chip)
            }
            root.setOnLongClickListener {
                clickListener?.onLongItemClick(it, chip)
                return@setOnLongClickListener true
            }
        }
    }
}