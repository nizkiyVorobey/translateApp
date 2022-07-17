package com.example.ttanslateapp.presentation.exam

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.RadioGroup
import com.example.ttanslateapp.R
import com.example.ttanslateapp.presentation.exam.adapter.ExamMode

class ExamModeDialog(context: Context, val viewModel: ExamKnowledgeWordsViewModel) :
    Dialog(context) {
    private lateinit var dialogView: View
    private lateinit var dialogBuilder: AlertDialog

    init {
        setupView()
    }

     fun setupView() {
        dialogBuilder = AlertDialog.Builder(context, R.style.ExamDialog)
            .create()
        dialogView = layoutInflater.inflate(R.layout.dialog_exam_mode, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCanceledOnTouchOutside(true)

        // update data in viewmodel if we close dialog by tap outside
        dialogBuilder.setOnCancelListener {
            viewModel.toggleOpenModeDialog(false)
        }
    }

     fun handleDialog(mode: ExamMode) {
        when (mode) {
            ExamMode.DAILY_MODE -> dialogView.findViewById<RadioGroup>(R.id.exam_radio_group)
                .check(R.id.daily_mode_button)
            ExamMode.INFINITY_MODE -> dialogView.findViewById<RadioGroup>(R.id.exam_radio_group)
                .check(R.id.infinity_mode_button)
        }

        // invoke only after set default value
        dialogView.findViewById<RadioGroup>(R.id.exam_radio_group)
            .setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.daily_mode_button -> viewModel.changeExamMode(mode = ExamMode.DAILY_MODE)
                    R.id.infinity_mode_button -> viewModel.changeExamMode(mode = ExamMode.INFINITY_MODE)
                }
                dialogBuilder.dismiss()
            }
    }


     fun setIsOpenModeDialog(isOpened: Boolean) {
        if (isOpened) {
            dialogBuilder.show()
        } else {
            dialogBuilder.dismiss()
        }
    }
}