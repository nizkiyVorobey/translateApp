package com.example.ttanslateapp.presentation.core

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import com.example.ttanslateapp.R
import com.example.ttanslateapp.presentation.exam.adapter.ExamMode

class ConfirmDialog(
    context: Context,
    val closeByClickOutside: Boolean = true
) :
    Dialog(context) {
    private lateinit var dialogView: View
    private lateinit var dialogBuilder: AlertDialog
    private var yesButton: Button? = null
    private var noButton: Button? = null
    private var titleView: TextView? = null

    init {
        setupView()
    }


    private fun setupView() {
        dialogBuilder = AlertDialog.Builder(context, R.style.ExamDialog)
            .create()
        dialogView = layoutInflater.inflate(R.layout.dialog_confirm, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCanceledOnTouchOutside(closeByClickOutside)

        // update data in viewmodel if we close dialog by tap outside
        yesButton = dialogView.findViewById(R.id.yes_button)
        noButton = dialogView.findViewById(R.id.no_button)
        titleView = dialogView.findViewById(R.id.title)


        yesButton?.setOnClickListener {
            setIsOpenModeDialog(false)
        }

        noButton?.setOnClickListener {
            setIsOpenModeDialog(false)
        }

        dialogBuilder.setOnCancelListener {
            setIsOpenModeDialog(false)
        }
    }

    fun setTitle(title:String):ConfirmDialog {
        titleView?.text = title
        return this
    }

    fun handleOkClick(cb: () -> Unit): ConfirmDialog {
        yesButton?.setOnClickListener {
            cb()
            setIsOpenModeDialog(false)
        }
        return this
    }

    fun handleCancelClick(cb: () -> Unit): ConfirmDialog {
        noButton?.setOnClickListener {
            cb()
            setIsOpenModeDialog(false)
        }
        return this
    }

    fun handleOutsideClick(cb: () -> Unit): ConfirmDialog {
        dialogBuilder.setOnCancelListener {
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