package com.example.ttanslateapp.util

import java.util.*

fun getExamWorkerDelay(): Long {
    val currentDate = Calendar.getInstance()
    val dueDate = Calendar.getInstance()
    // Set Execution around 18:30:00 PM
    dueDate.set(Calendar.HOUR_OF_DAY, 10)
    dueDate.set(Calendar.MINUTE, 0)
    dueDate.set(Calendar.SECOND, 0)
    if (dueDate.before(currentDate)) {
        dueDate.add(Calendar.HOUR_OF_DAY, 24)
    }

//    dueDate.add(Calendar.MINUTE, 1)
    return dueDate.timeInMillis - currentDate.timeInMillis
}