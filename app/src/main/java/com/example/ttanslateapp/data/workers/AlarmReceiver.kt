package com.example.ttanslateapp.data.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavDeepLinkBuilder
import com.example.ttanslateapp.R
import com.example.ttanslateapp.presentation.MainActivity
import com.example.ttanslateapp.presentation.TranslateApp
import com.example.ttanslateapp.presentation.exam.ExamReminder
import javax.inject.Inject


class AlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var examReminder: ExamReminder
    private val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    init {
        (TranslateApp.applicationContext() as TranslateApp).component.inject(this)
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val notificationManager = getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager
            createNotificationChannel(notificationManager)

            val openExamFragmentIntent = NavDeepLinkBuilder(context)
                .setComponentName(MainActivity::class.java)
                .setGraph(R.navigation.app_navigation)
                .setDestination(R.id.examKnowledgeWordsFragment)
                .createPendingIntent()

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Час повторити слова")
                .setContentText("Пройдіть тест, щоб перевірити на скільки добре ви вивчили слова")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setVibrate(longArrayOf(0, 500, 100))
                .setContentIntent(openExamFragmentIntent)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)

            examReminder.repeatReminder()
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChanel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            notificationChanel.setSound(soundUri,audioAttributes)
            notificationManager.createNotificationChannel(notificationChanel)
        }
    }

    companion object {
        const val CHANNEL_ID = "test_channel_id"
        private const val CHANNEL_NAME = "channel_name"
        private const val NOTIFICATION_ID = 100


        fun newIntent(context: Context): Intent {
            return Intent(context, AlarmReceiver::class.java)
        }
    }
}