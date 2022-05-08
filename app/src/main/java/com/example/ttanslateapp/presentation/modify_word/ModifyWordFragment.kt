package com.example.ttanslateapp.presentation.modify_word

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.ttanslateapp.R
import com.example.ttanslateapp.databinding.FragmentModifyWordBinding
import com.example.ttanslateapp.domain.model.ModifyWord
import com.example.ttanslateapp.domain.model.modify_word_chip.HintItem
import com.example.ttanslateapp.domain.model.modify_word_chip.TranslateWordItem
import com.example.ttanslateapp.presentation.core.BaseFragment
import com.example.ttanslateapp.presentation.core.BindingInflater
import com.example.ttanslateapp.presentation.core.RecordAudioBottomSheet
import com.example.ttanslateapp.presentation.modify_word.adapter.ModifyWordAdapter
import com.example.ttanslateapp.presentation.modify_word.adapter.hints.HintAdapter
import com.example.ttanslateapp.presentation.modify_word.adapter.translate.TranslateAdapter
import com.example.ttanslateapp.util.ScrollEditTextInsideScrollView
import com.example.ttanslateapp.util.getAppComponent
import com.example.ttanslateapp.util.setOnTextChange
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


private const val MODE_ADD = "mode-add"
private const val MODE_EDIT = "mode-edit"
private const val MODE = "mode"
private const val WORD_ID = "word_id"
private const val RECORD_AUDIO_RC = 1
private const val STORAGE_RC = 2

class ModifyWordFragment : BaseFragment<FragmentModifyWordBinding>() {
    override val bindingInflater: BindingInflater<FragmentModifyWordBinding>
        get() = FragmentModifyWordBinding::inflate

    private val viewModel by viewModels { get(ModifyWordViewModel::class.java) }

    private var mode: String? = null
    private var wordId: Long? = null
    private val translateAdapter = TranslateAdapter()
    private val hintAdapter = HintAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mode = it.getString(MODE)
            wordId = it.getLong(WORD_ID)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAppComponent().inject(this)

        binding.model = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        launchRightMode()
        setupClickListener()
        editTextScrollListener()
        setAdaptersClickListener()
        setupView()
        setObservers()
    }

    private fun launchRightMode() {
        when (mode) {
            MODE_EDIT -> launchEditMode()
        }
    }

    private fun launchEditMode() {
        val id = wordId ?: error("wordId is null")
        viewModel.getWordById(id)
        viewModel.successLoadWordById = object : ModifyWordViewModel.SuccessLoadWordById {
            override fun onLoaded(word: ModifyWord) {
                with(binding) {
                    inputTranslatedWord.englishWordInput.setText(word.value);
                    inputTranslatedWord.englishTranscriptionInput.setText(word.transcription)
                    translateWordDescription.descriptionInput.setText(word.description)

                    if (word.sound != null) {
                        recordEnglishPronunciation.setImageResource(R.drawable.mic_successful)
                        isRecordAdded.visibility = View.VISIBLE
                    }

                    val langList = mutableMapOf<String, Int>()

                    val adapter = inputTranslatedWord.selectLanguageSpinner.adapter
                    for (i in 0 until adapter.count) {
                        langList[adapter.getItem(i).toString()] = i
                    }

                    // FIXME change spinner for text view on edit mode
                    val spinnerValue = langList[word.langFrom] ?: 0
                    inputTranslatedWord.selectLanguageSpinner.setSelection(spinnerValue)
                    inputTranslatedWord.selectLanguageSpinner.isEnabled = false
                }
            }
        }
    }

    private fun setupView() = with(binding) {
        inputTranslatedWord.englishWordInput.setOnTextChange { viewModel.setWordValueError(false) }

        addTranslate.translateChipsRv.adapter = translateAdapter
        addTranslate.translateChipsRv.itemAnimator = null
        addTranslate.translateInput.setOnTextChange { viewModel.setTranslatesError(false) }

        addHints.hintChipsRv.adapter = hintAdapter
        addHints.hintChipsRv.itemAnimator = null

        recordEnglishPronunciation.setOnClickListener {
            requestRecordPermission()
        }
    }

    private fun requestRecordPermission() {
        val recordPermissionGranted = ActivityCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val storagePermissionGranted = ActivityCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED


        if (recordPermissionGranted && storagePermissionGranted) {
            openRecordBottomSheet()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                RECORD_AUDIO_RC
            )
        }
    }

    private fun openRecordBottomSheet() {
        val recordSheetDialog =
            RecordAudioBottomSheet(modifiedPath = viewModel.soundPath, "Test Word")
        recordSheetDialog.show(requireActivity().supportFragmentManager, RecordAudioBottomSheet.TAG)
        recordSheetDialog.callbackListener = object : RecordAudioBottomSheet.CallbackListener {
            override fun saveAudio(path: String?) {
                if (path != null) {
                    Toast.makeText(requireContext(), "audio added successfully", Toast.LENGTH_SHORT)
                        .show()
                    viewModel.soundPath = path
                    binding.recordEnglishPronunciation.setImageResource(R.drawable.mic_successful)
                    binding.isRecordAdded.visibility = View.VISIBLE
                } else {
                    viewModel.soundPath = path
                    binding.recordEnglishPronunciation.setImageResource(R.drawable.mic_active)
                    binding.isRecordAdded.visibility = View.INVISIBLE
                }

            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RECORD_AUDIO_RC && grantResults.isNotEmpty()) {
            val permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (permissionGranted) {
                openRecordBottomSheet()
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        android.Manifest.permission.RECORD_AUDIO
                    )
                ) {
                    Toast.makeText(context, "enable permission", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setObservers() = with(viewModel) {
        translates.observe(viewLifecycleOwner) { translateAdapter.submitList(it) }
        hints.observe(viewLifecycleOwner) { hintAdapter.submitList(it) }
        savedWordResult.observe(viewLifecycleOwner) {
            val message = if (it == true) {
                requireActivity().supportFragmentManager.popBackStack()
                getString(R.string.modify_word_success_save_word)
            } else {
                getString(R.string.error_happened)
            }
            Toast.makeText(binding.root.context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setAdaptersClickListener() {
        translateAdapter.clickListener =
            object : ModifyWordAdapter.OnItemClickListener<TranslateWordItem> {
                override fun onItemClick(it: View, item: TranslateWordItem) {
                    createTranslatePopupMenu(it, item)
                }
            }

        hintAdapter.clickListener =
            object : ModifyWordAdapter.OnItemMultiClickListener<HintItem> {
                override fun onItemClick(it: View, item: HintItem) {
                    createHintPopupMenu(it, item)
                }

                override fun onItemDeleteClick(item: HintItem) {
                    viewModel.deleteHint(item.id)
                }
            }
    }

    private fun setupClickListener() = with(binding) {
        addTranslate.button.setOnClickListener {
            viewModel.addTranslate(addTranslate.translateInput.text.toString())
            addTranslate.translateInput.setText("")
        }

        addTranslate.cancelEditTranslate.setOnClickListener {
            viewModel.setEditableTranslate(null)
            addTranslate.translateInput.setText("")
        }

        addHints.button.setOnClickListener {
            viewModel.addHint(addHints.inputHint.text.toString())
            addHints.inputHint.setText("")
        }

        addHints.cancelEditHint.setOnClickListener {
            viewModel.setEditableHint(null)
            addHints.inputHint.setText("")
        }

        toggleAdditionalField.setOnClickListener { viewModel.toggleVisibleAdditionalField() }
        saveTranslatedWord.setOnClickListener {
            viewModel.saveWord(
                value = inputTranslatedWord.englishWordInput.text.toString(),
                description = translateWordDescription.descriptionInput.text.toString(),
                transcription = inputTranslatedWord.englishTranscriptionInput.text.toString(),
                langFrom = inputTranslatedWord.selectLanguageSpinner.selectedItem.toString(),
                langTo = "UA"
            )
        }
    }

    private fun createHintPopupMenu(v: View, hintChip: HintItem) {
        val popupMenu = PopupMenu(requireContext(), v)
        popupMenu.menuInflater.inflate(R.menu.hint_item_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    with(binding.addHints.inputHint) {
                        setText(hintChip.value)
                        requestFocus()
                        setSelection(length())
                    }
                    viewModel.setEditableHint(hintChip)
                }
            }
            false
        }
        popupMenu.show()
    }

    private fun createTranslatePopupMenu(v: View, translateChip: TranslateWordItem) {
        val popupMenu = PopupMenu(requireContext(), v)
        popupMenu.menuInflater.inflate(R.menu.trasnlate_item_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    with(binding.addTranslate.translateInput) {
                        setText(translateChip.value)
                        requestFocus()
                        setSelection(length())
                    }
                    viewModel.setEditableTranslate(translateChip)
                }

                R.id.menu_delete -> {
                    viewModel.deleteTranslate(translateChip.id)
                }
            }
            false
        }

        popupMenu.show()
    }

    private fun editTextScrollListener() {
        ScrollEditTextInsideScrollView.allowScroll(binding.translateWordDescription.descriptionInput)
    }

    companion object {
        @JvmStatic
        fun newInstanceAdd() =
            ModifyWordFragment().apply {
                arguments = Bundle().apply {
                    putString(MODE, MODE_ADD)
                }
            }

        fun newInstanceEdit(wordId: Long) =
            ModifyWordFragment().apply {
                arguments = Bundle().apply {
                    putString(MODE, MODE_EDIT)
                    putLong(WORD_ID, wordId)
                }
            }
    }
}