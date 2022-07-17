package com.example.ttanslateapp.presentation.exam

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import com.example.ttanslateapp.R
import com.example.ttanslateapp.presentation.core.ConfirmDialog
import com.example.ttanslateapp.presentation.exam.adapter.ExamMode
import com.example.ttanslateapp.presentation.exam.adapter.ExamMode.*

class ExamEndDialog(context: Context) :
    Dialog(context) {
    private lateinit var dialogView: View
    private lateinit var dialogBuilder: AlertDialog
    private lateinit var closeButton: Button

    var mode: ExamMode? = null

    @JvmName("setMode1")
    fun setMode(mode: ExamMode) {
        this.mode = mode
        updateView()
    }

    init {
        setupView()
    }


    private fun setupView() {
        dialogBuilder = AlertDialog.Builder(context, R.style.ExamDialog)
            .create()
        dialogView = layoutInflater.inflate(R.layout.dialog_end_mode, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCanceledOnTouchOutside(true)
        updateView()
        closeButton = dialogView.findViewById(R.id.close)
    }

    private fun updateView() {
        val title = when (mode) {
            DAILY_MODE -> context.getString(R.string.exam_complete_daily_exam)
            INFINITY_MODE -> context.getString(R.string.exam_complete_infinity_exam)
            null -> ""
        }
        dialogView.findViewById<TextView>(R.id.title).text = title
    }

    fun handleCloseClick(cb: () -> Unit): ExamEndDialog {
        closeButton.setOnClickListener {
            cb()
            setIsOpenModeDialog(false)
        }
        return this
    }


    fun setIsOpenModeDialog(isOpened: Boolean) {
        if (isOpened) {
            dialogBuilder.show()
        } else {
            dialogBuilder.dismiss()
        }
    }
}