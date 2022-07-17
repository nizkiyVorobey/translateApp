package com.example.ttanslateapp.presentation.modify_word

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.properties.Delegates.notNull

class AudioPermissionResolver(
    private val registry: ActivityResultRegistry,
    private val listener: ResultListener
) : DefaultLifecycleObserver {

    private var requestAudio by notNull<ActivityResultLauncher<String>>()
    private var isRequesting: Boolean = false

    override fun onCreate(owner: LifecycleOwner) {
        requestAudio = registry.register(
            KEY_AUDIO,
            owner,
            ActivityResultContracts.RequestPermission()
        ) { isGranted -> proceedResult(isGranted) }
    }

    fun requestPermission(context: Context) {
        if (isRequesting) return
        isRequesting = true

        when (PackageManager.PERMISSION_GRANTED) {
            checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) -> {
                listener.onAudioGranted()
                isRequesting = false
            }
            else -> requestAudio.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun proceedResult(isGranted: Boolean) {
        isRequesting = false
        if (isGranted) {
            listener.onAudioGranted()
        } else {
            val showRationaleRecord =
                listener.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
            if (!showRationaleRecord) {
                listener.showMessage("enable RECORD_AUDIO")
            }
        }
    }

    interface ResultListener {
        fun onAudioGranted()
        fun showMessage(text: String)
        fun shouldShowRequestPermissionRationale(key: String): Boolean
    }

    companion object {
        private const val KEY_AUDIO = "translate_app_audio_request"
    }
}