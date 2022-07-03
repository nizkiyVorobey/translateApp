package com.example.ttanslateapp.data.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.*
import com.example.ttanslateapp.R
import com.example.ttanslateapp.presentation.MainActivity
import com.example.ttanslateapp.util.PushFrequency
import com.example.ttanslateapp.util.getExamReminderDelayFromNow
import java.util.*
import java.util.concurrent.TimeUnit

class DailyWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        showNotification()

        val delay = getExamReminderDelayFromNow(
            frequencyDelay = PushFrequency.ONCE_AT_DAY,
            hours = 11,
            minutes = 30
        ) - Calendar.getInstance().timeInMillis
        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(TAG)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                NAME,
                ExistingWorkPolicy.REPLACE,
                dailyWorkRequest
            )


        return Result.success()
    }

    private fun showNotification() {
        val notificationManager =
            context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChanel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(notificationChanel)
        }

        val pendingIntent = NavDeepLinkBuilder(context)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.app_navigation)
            .setDestination(R.id.examKnowledgeWordsFragment)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setContentTitle("Час повторити слова")
            .setContentText("Пройдіть тест, щоб перевірити на скільки добре ви вивчили слова")
            .setSmallIcon(R.drawable.mic_successful)
            .setAutoCancel(true)
            .build()


        notificationManager.notify(1, notification)
    }

    companion object {
        const val NAME = "name"
        const val CHANNEL_ID = "test_channel_id"
        private const val CHANNEL_NAME = "channel_name"

        const val TAG = "DailyWorker"
    }
}