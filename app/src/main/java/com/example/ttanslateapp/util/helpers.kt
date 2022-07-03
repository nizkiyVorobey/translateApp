package com.example.ttanslateapp.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

fun generateFileName(): String {
    val formatter = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.US)
    val date = Date()
    return "Recording_" + formatter.format(date) + ".3gp"
}

fun getAudioPath(context: Context, fileName: String): String {
    val dirName =
        context.getExternalFilesDir(null)?.absolutePath  // FIXME save into internal storage and then sync with Firebase storage

    return "$dirName/$fileName"
}
