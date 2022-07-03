package com.example.ttanslateapp.presentation.settings

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ttanslateapp.presentation.exam.ExamReminder
import com.example.ttanslateapp.presentation.exam.ReminderTime
import com.example.ttanslateapp.util.*
import com.google.gson.Gson
import java.lang.String
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.round

sealed interface SettingsUiState {
    data class SetReminderFrequency(val frequency: kotlin.String) : SettingsUiState
    data class UpdateTime(
        val timeHours: kotlin.String,
        val timeMinutes: kotlin.String,
    ) : SettingsUiState

    data class SetInitial(
        val frequency: kotlin.String,
        val timeHours: kotlin.String,
        val timeMinutes: kotlin.String,
        val isSettingsTheSame: Boolean = true
    ) : SettingsUiState

    data class IsSuccessUpdateSettings(val isSuccess: Boolean) : SettingsUiState
    data class SettingsHasBeenChanged(val isSame: Boolean) : SettingsUiState

    data class TimeBeforePush(val time: kotlin.String) : SettingsUiState
}

data class SettingsState(
    val frequencyValue: kotlin.String = "",
    val timeHours: kotlin.String = "",
    val timeMinutes: kotlin.String = "",
)

class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val examReminder: ExamReminder
) : ViewModel() {
    private val _uiState = MutableLiveData<SettingsUiState>()
    val uiState: LiveData<SettingsUiState> = _uiState
    var state = SettingsState()
    private var initialValues = SettingsState()

    private val sharedPref: SharedPreferences =
        application.getSharedPreferences(
            MY_PREFERENCES,
            AppCompatActivity.MODE_PRIVATE
        )

    private fun setInitialData() {
        val position =
            when (sharedPref.getInt(EXAM_REMINDER_FREQUENCY, PushFrequency.ONCE_AT_DAY)) {
                PushFrequency.NONE -> 0
                PushFrequency.ONCE_AT_DAY -> 1
                PushFrequency.ONCE_AT_THREE_DAYS -> 2
                PushFrequency.ONCE_AT_SIX_DAYS -> 3
                else -> 0
            }
        val text = reminderFrequencyList[position]

        val time = getTimeReminder()
        val hours = convertTime(time.hours)
        val minutes = convertTime(time.minutes)

        _uiState.value =
            SettingsUiState.SetInitial(frequency = text, timeHours = hours, timeMinutes = minutes)
        state = state.copy(frequencyValue = text, timeHours = hours, timeMinutes = minutes)
        initialValues = state
        showTimeBeforePush()
    }

    fun setupData(initialLaunch: Boolean) {
        if (initialLaunch) {
            setInitialData()
        } else {
            _uiState.value =
                SettingsUiState.SetInitial(
                    frequency = state.frequencyValue,
                    timeHours = state.timeHours,
                    timeMinutes = state.timeMinutes,
                    isSettingsTheSame = state == initialValues
                )
            showTimeBeforePush()
        }
    }

    private fun getTimeReminder(): ReminderTime {
        val examReminderTime = sharedPref.getString(EXAM_REMINDER_TIME, "")
        val gson = Gson()

        return gson.fromJson(examReminderTime, ReminderTime::class.java)
    }

    fun updateUiFrequency(frequencyPosition: Int) {
        state = state.copy(frequencyValue = reminderFrequencyList[frequencyPosition])
        _uiState.value =
            SettingsUiState.SetReminderFrequency(reminderFrequencyList[frequencyPosition])
        checkIsChangedExist()
    }

    fun updateTime(minutes: Int, hours: Int) {
        state = state.copy(timeHours = hours.toString(), timeMinutes = minutes.toString())
        _uiState.value = SettingsUiState.UpdateTime(
            timeHours = convertTime(hours),
            timeMinutes = convertTime(minutes)
        )
        checkIsChangedExist()
    }

    private fun checkIsChangedExist() {
        val isSame = state == initialValues
        _uiState.value = SettingsUiState.SettingsHasBeenChanged(isSame)
    }

    private fun convertTime(value: Int): kotlin.String {
        return if (value < 10) "0$value" else value.toString()
    }

    private fun showTimeBeforePush() {
        val getNotificationPref =
            sharedPref.getLong(LEFT_BEFORE_NOTIFICATION, -1L)
        if (getNotificationPref == -1L) return
        val millis = getNotificationPref - Calendar.getInstance().timeInMillis

        val timer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hms = convertTimeToHMS(millisUntilFinished)
                _uiState.value = SettingsUiState.TimeBeforePush(time = hms)
            }

            override fun onFinish() {

            }
        }
        timer.start()
    }

    @SuppressLint("DefaultLocale")
    private fun convertTimeToHMS(millis: Long): kotlin.String {
        return String.format(
            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(
                    millis
                )
            ),
            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    millis
                )
            )
        )
    }


    fun updateSettingsPreferences() {
        val reminderFrequency = when (reminderFrequencyList.indexOf(state.frequencyValue)) {
            0 -> PushFrequency.NONE
            1 -> PushFrequency.ONCE_AT_DAY
            2 -> PushFrequency.ONCE_AT_THREE_DAYS
            3 -> PushFrequency.ONCE_AT_SIX_DAYS
            else -> PushFrequency.ONCE_AT_DAY
        }

        if (reminderFrequency == PushFrequency.NONE) {
            examReminder.resetReminder()
            _uiState.value = SettingsUiState.IsSuccessUpdateSettings(true)
            return
        }

        try {
            examReminder.updateReminder(
                frequency = reminderFrequency,
                startHour = state.timeHours.toInt(),
                startMinute = state.timeMinutes.toInt()
            )

            _uiState.value = SettingsUiState.IsSuccessUpdateSettings(true)
        } catch (e: Exception) {
            _uiState.value = SettingsUiState.IsSuccessUpdateSettings(false)
        }
    }
}