package com.example.ttanslateapp.presentation.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import androidx.lifecycle.ViewModelProvider
import com.example.ttanslateapp.R
import com.example.ttanslateapp.databinding.ViewRecordAudioBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class RecordAudioBottomSheet : BottomSheetDialogFragment() {
    private var _binding: ViewRecordAudioBinding? = null
    private val binding get() = _binding!!

    var callbackListener: CallbackListener? = null

    val viewModel by lazy { ViewModelProvider(this)[RecordAudioViewModel::class.java] }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val modifiedFileName = arguments?.getString(MODIFIED_FILE_NAME)
        val word = arguments?.getString(WORD)
        viewModel.setArguments(
            modifiedFileName = modifiedFileName,
            word = word
        )
    }

    override fun onStart() {
        super.onStart()

        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.maxWidth = WindowManager.LayoutParams.MATCH_PARENT


        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val layoutParams = binding.root.layoutParams
            layoutParams.height = Resources.getSystem().displayMetrics.heightPixels;
            binding.root.layoutParams = layoutParams
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewRecordAudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupClickListener()
        setupListeners()
    }

    private fun setupListeners() = with(binding) {
        viewModel.isRecordExist.observe(viewLifecycleOwner) {
            if (it) {
                recordingChronometer.isCountDown = true
                resetChronometer()
                prepareAppearsAudio()
            } else {
                deleteAudioFile()
                binding.recordingChronometer.base = SystemClock.elapsedRealtime()
            }
        }

        viewModel.isRecording.observe(viewLifecycleOwner) {
            if (it) {
                recordingChronometer.isCountDown = false
                recordingChronometer.base = SystemClock.elapsedRealtime()
                recordingChronometer.start()
                saveRecord.isEnabled = false
            } else {
                recordingChronometer.isCountDown = true
                saveRecord.isEnabled = true
                recordingChronometer.stop()
            }
        }

        viewModel.isPlaying.observe(viewLifecycleOwner) {
            if (it) {
                resetChronometer()
                recordingChronometer.start()
            } else {
                recordingChronometer.stop()
                resetChronometer()
            }
        }

        viewModel.callbackListener = object: RecordAudioViewModel.CallbackListener {
            override fun deleteRecordingButNotSaveAudio() {
                callbackListener?.saveAudio(null)
            }
        }
    }


    private fun resetChronometer() {
        val diff = viewModel._player?.let {
            it.duration.toLong() - it.currentPosition
        } ?: 0

        binding.recordingChronometer.base =
            SystemClock.elapsedRealtime() + diff
    }


    private fun setupView() = with(binding) {
        wordValue.text = arguments?.getString(WORD)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupClickListener() = with(binding) {
        deleteRecord.setOnClickListener {
            viewModel.deleteRecording()
            callbackListener?.saveAudio(null)
        }
        listenRecord.setOnClickListener {
            viewModel.startPlaying()
        }
        saveRecord.setOnClickListener {
            saveRecording()
        }

        handleButtonContainer.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                startRecording()

            } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                endRecording()
            }
            true
        }
    }


    private fun startRecording() = with(binding) {
        viewModel.startRecording()
        saveRecord.isEnabled = false
        binding.listenRecord.isEnabled = false
        handleButton.setImageResource(R.drawable.mic_active)
        recordAudioAnimation.visibility = View.VISIBLE

    }

    private fun endRecording() = with(binding) {
        viewModel.endRecording()
        listenRecord.isEnabled = true
        handleButton.setImageResource(R.drawable.mic_disable)
        deleteRecord.setImageResource(R.drawable.delete_active)
        recordAudioAnimation.visibility = View.INVISIBLE
    }

    private fun saveRecording() {
        viewModel.fileRecordedBuNotSaved = false
        callbackListener?.saveAudio(viewModel.fileName)
        this.dismiss()
    }

    private fun prepareAppearsAudio() = with(binding) {
        saveRecord.isEnabled = true
        listenRecord.isEnabled = true
        binding.deleteRecord.setImageResource(R.drawable.delete_active)
    }

    private fun deleteAudioFile() = with(binding) {
        deleteRecord.setImageResource(R.drawable.delete_disabled)
        saveRecord.isEnabled = false
        listenRecord.isEnabled = false
        handleButton.setImageResource(R.drawable.mic_disable)
    }

    interface CallbackListener {
        fun saveAudio(fileName: String?)
    }

    companion object {
        const val TAG = "RecordAudioBottomSheet"
        const val MODIFIED_FILE_NAME = "modifiedFileName"
        const val WORD = "word"
    }
}