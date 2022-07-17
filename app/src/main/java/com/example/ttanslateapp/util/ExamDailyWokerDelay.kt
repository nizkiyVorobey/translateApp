package com.example.ttanslateapp.util

import com.example.ttanslateapp.util.PushFrequency.Companion.DEFAULT_HOURS
import com.example.ttanslateapp.util.PushFrequency.Companion.DEFAULT_MINUTES
import java.util.*

class PushFrequency {
    companion object {
        const val NONE = 0
        const val ONCE_AT_DAY = 86_400_000 // 0
        const val ONCE_AT_THREE_DAYS = 259_200_000 // 86_400_000 * 2
        const val ONCE_AT_SIX_DAYS = 518_400_000 // 86_400_000 * 5

        const val DEFAULT_HOURS = 10
        const val DEFAULT_MINUTES = 0
    }
}

fun getExamReminderDelayFromNow(
    hours: Int,
    minutes: Int,
    frequencyDelay: Int
): Long {
    val dueDate = Calendar.getInstance()
    val currentDate = Calendar.getInstance()

    dueDate.set(Calendar.HOUR_OF_DAY, hours)
    dueDate.set(Calendar.MINUTE, minutes)
    dueDate.set(Calendar.SECOND, 0)
    dueDate.set(Calendar.MILLISECOND, 0)

    dueDate.add(Calendar.MILLISECOND, frequencyDelay)

    if (dueDate.before(currentDate)) {
        dueDate.add(Calendar.HOUR_OF_DAY, 24)
    }

    return dueDate.timeInMillis

}


