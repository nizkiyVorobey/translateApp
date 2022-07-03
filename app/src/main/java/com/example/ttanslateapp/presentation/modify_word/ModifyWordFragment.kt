package com.example.ttanslateapp.presentation.modify_word

import android.Manifest.permission
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ttanslateapp.R
import com.example.ttanslateapp.databinding.FragmentModifyWordBinding
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


enum class ModifyWordModes {
    MODE_ADD, MODE_EDIT
}

class ModifyWordFragment : BaseFragment<FragmentModifyWordBinding>() {
    override val bindingInflater: BindingInflater<FragmentModifyWordBinding>
        get() = FragmentModifyWordBinding::inflate

    private val args by navArgs<ModifyWordFragmentArgs>()

    private val viewModel by viewModels {
        get(ModifyWordViewModel::class.java)
    }
    private val recordPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val audio = permissions[permission.RECORD_AUDIO]

            if (audio == true) {
                openRecordBottomSheet()
            } else {
                val showRationaleRecord =
                    shouldShowRequestPermissionRationale(permission.RECORD_AUDIO)

                if (!showRationaleRecord) {
                    Toast.makeText(context, "enable RECORD_AUDIO", Toast.LENGTH_SHORT).show()
                }
            }
        }


    private val translateAdapter = TranslateAdapter()
    private val hintAdapter = HintAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAppComponent().inject(this)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbar.setNavigationOnClickListener {
            this.goBack()
        }

        // FIXME Livedata observe twice
        if (savedInstanceState == null) {
            launchRightMode()
        }

        setObservers()
        setupClickListener()
        editTextScrollListener()
        setAdaptersClickListener()
        setupView()
    }


    private fun launchRightMode() {
        when (args.mode) {
            ModifyWordModes.MODE_EDIT -> viewModel.launchEditMode(args.wordId)
            ModifyWordModes.MODE_ADD -> viewModel.launchAddMode()
        }
    }

    private fun setupView() = with(binding) {
        addTranslate.translateChipsRv.adapter = translateAdapter
        addTranslate.translateChipsRv.itemAnimator = null
        addTranslate.translateInput.setOnTextChange { viewModel.resetTranslatesError() }
        inputTranslatedWord.englishWordInput.setOnTextChange { viewModel.resetWordValueError() }

        // focus next input on click action key
        listOf(
            inputTranslatedWord.englishWordInput,
            inputTranslatedWord.englishTranscriptionInput,
            addTranslate.translateInput
        ).forEach {
            it.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_NEXT || event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                    v.focusSearch(View.FOCUS_DOWN).requestFocus()
                    true
                } else {
                    false
                }
            }
        }


        addHints.hintChipsRv.adapter = hintAdapter
        addHints.hintChipsRv.itemAnimator = null

        recordEnglishPronunciation.setOnClickListener { requestRecordPermission() }
    }

    private fun requestRecordPermission() {
        recordPermission.launch(
            arrayOf(
                permission.RECORD_AUDIO,
                permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    private fun openRecordBottomSheet() {
        val recordSheetDialog = RecordAudioBottomSheet()
        recordSheetDialog.arguments = Bundle().apply {
            putString(
                RecordAudioBottomSheet.MODIFIED_FILE_NAME,
                viewModel.getAudioFileName()
            )
            putString(
                RecordAudioBottomSheet.WORD,
                binding.inputTranslatedWord.englishWordInput.text.toString()
            )
        }
        recordSheetDialog.show(requireActivity().supportFragmentManager, RecordAudioBottomSheet.TAG)
        setRecordAudioBottomSheetClick(recordSheetDialog)
    }

    override fun onResume() {
        super.onResume()
        val bottomSheet =
            requireActivity().supportFragmentManager.findFragmentByTag(RecordAudioBottomSheet.TAG)
        if (bottomSheet != null) {
            setRecordAudioBottomSheetClick(bottomSheet as RecordAudioBottomSheet)
        }
    }

    private fun setRecordAudioBottomSheetClick(recordSheetDialog: RecordAudioBottomSheet) {
        recordSheetDialog.callbackListener = object : RecordAudioBottomSheet.CallbackListener {
            override fun saveAudio(fileName: String?) {
                if (fileName != null) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.add_audio_success),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    viewModel.updateAudio(fileName)
                } else {
                    viewModel.updateAudio(null)
                }
                updateMicrophoneIcon(fileName)
            }
        }
    }

    private fun setObservers() = with(viewModel) {
        with(binding) {
            uiState.observe(viewLifecycleOwner) { uiState ->
                when (uiState) {
                    is ModifyWordUiState.IsWordLoading -> {
                        if (uiState.isLoading) {
                            rootScrollView.visibility = View.INVISIBLE
                        } else {
                            rootScrollView.visibility = View.VISIBLE
                        }
                    }
                    is ModifyWordUiState.EditFieldError -> {
                        inputTranslatedWord.englishWordContainer.error = uiState.wordValueError
                        addTranslate.englishWordContainer.error = uiState.translatesError
                        wordPriorityContainer.error = uiState.priorityValidation
                    }
                    is ModifyWordUiState.PreScreen -> {
                        inputTranslatedWord.englishWordInput.setText(uiState.wordValue)
                        inputTranslatedWord.englishWordContainer.error = uiState.wordValueError
                        inputTranslatedWord.englishTranscriptionInput.setText(uiState.transcription)
                        translateWordDescription.descriptionInput.setText(uiState.description)
                        wordPriorityValue.setText(uiState.priority.toString())

                        val langList = mutableMapOf<String, Int>()
                        val adapter = inputTranslatedWord.selectLanguageSpinner.adapter
                        for (i in 0 until adapter.count) {
                            langList[adapter.getItem(i).toString()] = i
                        }

                        // FIXME change spinner for text view on edit mode
                        val spinnerValue = langList[uiState.langFrom] ?: 0
                        inputTranslatedWord.selectLanguageSpinner.setSelection(spinnerValue)
                        inputTranslatedWord.selectLanguageSpinner.isEnabled = false

                        addTranslate.englishWordContainer.error = uiState.translatesError
                        translateAdapter.submitList(uiState.translates)

                        updateMicrophoneIcon(uiState.soundFileName)

                        addHints.root.visibility = uiState.isAdditionalFieldVisible

                        hintAdapter.submitList(uiState.hints)
                    }
                    is ModifyWordUiState.ShowAdditionalFields -> {
                        addHints.root.visibility = uiState.isVisible
                    }
                    is ModifyWordUiState.ShowResultModify -> {
                        val message = if (uiState.isSuccess) "Success" else "Fail"
                        Toast.makeText(binding.root.context, message, Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is ModifyWordUiState.StartModifyTranslate -> {
                        addTranslate.cancelEditTranslate.visibility = View.VISIBLE
                        addTranslate.button.text = "edit"
                        addTranslate.translateInput.setText(uiState.value)
                    }
                    is ModifyWordUiState.CompleteModifyTranslate -> {
                        addTranslate.cancelEditTranslate.visibility = View.INVISIBLE
                        addTranslate.button.text = "add"
                        addTranslate.translateInput.setText("")
                        translateAdapter.submitList(uiState.translates)
                    }
                    is ModifyWordUiState.StartModifyHint -> {
                        addHints.cancelEditHint.visibility = View.VISIBLE
                        addHints.button.text = "edit"
                        addHints.inputHint.setText(uiState.value)
                    }
                    is ModifyWordUiState.CompleteModifyHint -> {
                        addHints.cancelEditHint.visibility = View.INVISIBLE
                        addHints.button.text = "add"
                        addHints.inputHint.setText("")
                        hintAdapter.submitList(uiState.hints)
                    }
                    is ModifyWordUiState.DeleteHints -> {
                        hintAdapter.submitList(uiState.hints)
                    }
                    is ModifyWordUiState.DeleteTranslates -> {
                        translateAdapter.submitList(uiState.translates)
                    }
                    is ModifyWordUiState.UpdateSoundFile -> {
                        updateMicrophoneIcon(uiState.name)
                    }
                }
            }
        }
    }

    private fun updateMicrophoneIcon(fileName: String?) = with(binding) {
        if (fileName == null) {
            recordEnglishPronunciation.setImageResource(R.drawable.mic_active)
            isRecordAdded.visibility = View.INVISIBLE
        } else {
            recordEnglishPronunciation.setImageResource(R.drawable.mic_successful)
            isRecordAdded.visibility = View.VISIBLE
        }
    }

    private fun setAdaptersClickListener() {
        translateAdapter.clickListener =
            object : ModifyWordAdapter.OnItemClickListener<TranslateWordItem> {
                override fun onItemClick(it: View, item: TranslateWordItem) {
                    createTranslatePopupMenu(it, item)
                }

                override fun onLongItemClick(it: View, item: TranslateWordItem) {
                    viewModel.toggleIsHiddenTranslate(item)
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

                override fun onLongItemClick(it: View, item: HintItem) {}
            }
    }

    private fun setupClickListener() = with(binding) {
        addTranslate.button.setOnClickListener { viewModel.addTranslate(addTranslate.translateInput.text.toString()) }
        addTranslate.cancelEditTranslate.setOnClickListener { viewModel.cancelEditableTranslate() }
        addHints.button.setOnClickListener { viewModel.addHint(addHints.inputHint.text.toString()) }
        addHints.cancelEditHint.setOnClickListener { viewModel.cancelEditableHint() }

        toggleAdditionalField.setOnClickListener { viewModel.toggleVisibleAdditionalField() }
        saveTranslatedWord.setOnClickListener {
            viewModel.saveWord(
                value = inputTranslatedWord.englishWordInput.text.toString(),
                description = translateWordDescription.descriptionInput.text.toString(),
                transcription = inputTranslatedWord.englishTranscriptionInput.text.toString(),
                langFrom = "EN",
                langTo = inputTranslatedWord.selectLanguageSpinner.selectedItem.toString(),
                priority = wordPriorityValue.text.toString()
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
}