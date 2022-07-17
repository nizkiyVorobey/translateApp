package com.example.ttanslateapp.presentation.exam

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.allViews
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttanslateapp.R
import com.example.ttanslateapp.databinding.FragmentExamKnowledgeWordsBinding
import com.example.ttanslateapp.domain.model.exam.ExamWord
import com.example.ttanslateapp.domain.model.exam.ExamWordStatus
import com.example.ttanslateapp.domain.model.modify_word_chip.Translate
import com.example.ttanslateapp.presentation.core.BaseFragment
import com.example.ttanslateapp.presentation.core.BindingInflater
import com.example.ttanslateapp.presentation.exam.adapter.ExamAdapter
import com.example.ttanslateapp.presentation.exam.adapter.ExamKnowledgeUiState
import com.example.ttanslateapp.presentation.exam.adapter.ExamMode
import com.example.ttanslateapp.presentation.modify_word.ModifyWordModes
import com.example.ttanslateapp.presentation.modify_word.adapter.ModifyWordAdapter
import com.example.ttanslateapp.presentation.modify_word.adapter.translate.TranslateAdapter
import com.example.ttanslateapp.util.getAppComponent
import com.example.ttanslateapp.util.setOnTextChange
import timber.log.Timber


class ExamKnowledgeWordsFragment : BaseFragment<FragmentExamKnowledgeWordsBinding>() {

    override val bindingInflater: BindingInflater<FragmentExamKnowledgeWordsBinding>
        get() = FragmentExamKnowledgeWordsBinding::inflate

    private val viewModel by viewModels {
        get(ExamKnowledgeWordsViewModel::class.java)
    }

    private val examAdapter = ExamAdapter()
    private val translatesAdapter = TranslateAdapter()

    private val modeDialog by lazy {
        ExamModeDialog(
            context = requireContext(),
            viewModel = viewModel
        )
    }

    private val examEndDialog by lazy {
        ExamEndDialog(
            context = requireContext(),
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAppComponent().inject(this)

        if (savedInstanceState != null) {
            viewModel.restoreUI()
        }

        setupAdapter()
        observeLiveDate()
        clickListeners()

        binding.examWordInput.setOnTextChange {
            viewModel.handleAnswerEditText(it.toString())
            binding.examWordContainer.error = null
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clickListeners() = with(binding) {
        showVariantsLabel.setOnClickListener {
            viewModel.toggleVisibleVariants()
        }
        showHintsLabel.setOnClickListener {
            viewModel.toggleHintExpanded()
        }
        showNextHintButton.setOnClickListener {
            viewModel.showNextHint()
        }

        emptyListLayout.addFirstWord.setOnClickListener {
            val action =
                ExamKnowledgeWordsFragmentDirections.actionExamKnowledgeWordsFragmentToModifyWordFragment(
                    mode = ModifyWordModes.MODE_ADD
                )
            findNavController().navigate(action)
        }
        addHiddenTranslatesContainer.hiddenTranslatesDescriptionLabel.setOnClickListener { viewModel.toggleVisibilityHiddenDescription() }
        addHiddenTranslatesContainer.toggleExpandedCurrentTranslates.setOnClickListener { viewModel.toggleExpandedAllTranslates() }
        addHiddenTranslatesContainer.addHiddenTranslate.setOnClickListener {
            viewModel.addHiddenTranslate(
                examWordInput.text.toString()
            )
        }
        goNextQuestion.setOnClickListener { viewModel.goToNextQuestion() }
        goPrevQuestion.setOnClickListener { viewModel.goPrev() }

        examModeButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.alpha = 0.5f
                MotionEvent.ACTION_UP -> {
                    v.alpha = 1f
                    viewModel.toggleOpenModeDialog(true)
                }
            }
            true
        }

        examEndDialog.handleCloseClick {
            findNavController().popBackStack()
        }
    }

    private fun observeLiveDate() = with(binding) {
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                is ExamKnowledgeUiState.ToggleOpenModeDialog -> {
                    modeDialog.setIsOpenModeDialog(uiState.isOpened)
                }
                is ExamKnowledgeUiState.RestoreUI -> {
                    if (uiState.isLoading) {
                        progressBar.visibility = View.VISIBLE
                        return@observe
                    }

                    progressBar.visibility = View.GONE
                    if (uiState.examWordList.isEmpty()) {
                        loadedEmptyList()
                        return@observe
                    }

                    examContainer.visibility = View.VISIBLE
                    handleMode(mode = uiState.mode)
                    modeDialog.setIsOpenModeDialog(uiState.isModeDialogOpen)
                    uiState.currentWord?.let { currentWord ->
                        scrollExamWordListToCurrentPosition(uiState.activeWordPosition)

                        if (examWordInput.text.toString().trim()
                                .isNotEmpty() && !uiState.currentWord.isFreeze
                        ) {
                            examCheckAnswer.isEnabled = true
                        }

                        examWordName.text = uiState.currentWord.value

                        setupBaseStyle(
                            currentWord = uiState.currentWord,
                            examWordListSize = uiState.examWordList.size,
                            activeWordPosition = uiState.activeWordPosition
                        )

                        translatesAdapter.submitList(uiState.currentWord.translates)
                        examAdapter.submitList(uiState.examWordList)

                        examWordName.text = uiState.currentWord.value

                        counter.text = getString(
                            R.string.exam_counter,
                            uiState.activeWordPosition + 1,
                            uiState.examWordList.size
                        )
                        setVariantsBackground(selectedVariantValue = uiState.currentWord.selectedVariantValue)
                    }
                }
                is ExamKnowledgeUiState.IsLoadingWords -> {
                    progressBar.visibility = View.VISIBLE
                }
                is ExamKnowledgeUiState.LoadedEmptyList -> {
                    loadedEmptyList()
                }
                is ExamKnowledgeUiState.LoadedWordsSuccess -> {
                    progressBar.visibility = View.GONE
                    examContainer.visibility = View.VISIBLE
                    handleMode(mode = uiState.mode)

                    examAdapter.submitList(uiState.examWordList)

                    examWordName.text = uiState.currentWord.value
                    examWordInput.text = null

                    setupBaseStyle(
                        currentWord = uiState.currentWord,
                        examWordListSize = uiState.examWordList.size,
                        activeWordPosition = uiState.activeWordPosition
                    )

                    counter.text = getString(
                        R.string.exam_counter,
                        uiState.activeWordPosition + 1,
                        uiState.examWordList.size
                    )
                    goPrevQuestion.alpha = 0.5f
                    goPrevQuestion.isClickable = false
                }
                is ExamKnowledgeUiState.HandleAnswerInput -> {
                    examCheckAnswer.isEnabled = uiState.value.trim()
                        .isNotEmpty() // FIXME make it in view model or in the data class
                    if (uiState.userGaveAnswer) {
                        examCheckAnswer.isEnabled = false
                    } else {
                        examCheckAnswer.isEnabled = uiState.value.trim().isNotEmpty()
                    }

                    setVariantsBackground(selectedVariantValue = uiState.selectedVariantValue)
                }
                is ExamKnowledgeUiState.CheckedAnswer -> {
                    if (uiState.isExamEnd) {
                        examEndDialog.setIsOpenModeDialog(true)
                    }
                    examCheckAnswer.isEnabled = false

                    examAdapter.submitList(uiState.examWordList)
                    val isAnswerCorrect = uiState.status == ExamWordStatus.SUCCESS
                    setAnswerResult(isCorrect = isAnswerCorrect, answer = uiState.givenAnswer)

                    translatesAdapter.submitList(uiState.currentWord.translates)
                    setVariantsStyle(currentWord = uiState.currentWord)
                    setHintsStyle(currentWord = uiState.currentWord)
                    setTranslateStyles(currentWord = uiState.currentWord)
                }
                is ExamKnowledgeUiState.QuestionNavigation -> {
                    scrollExamWordListToCurrentPosition(uiState.activeWordPosition)
                    setClickableNavigationButtons(
                        activeWordPosition = uiState.activeWordPosition,
                        listSize = uiState.examWordList.size - 1
                    )

                    // if answer was give show answer else hide
                    val isAnswerCorrect = uiState.currentWord.status == ExamWordStatus.SUCCESS
                    setAnswerResult(
                        isCorrect = isAnswerCorrect,
                        answer = uiState.currentWord.givenAnswer
                    )

                    setVariantsStyle(currentWord = uiState.currentWord)
                    setHintsStyle(currentWord = uiState.currentWord)
                    setTranslateStyles(currentWord = uiState.currentWord)

                    translatesAdapter.submitList(uiState.currentWord.translates)

                    examAdapter.submitList(uiState.examWordList)
                    examWordName.text = uiState.currentWord.value
                    examWordInput.setText("")

                    counter.text = getString(
                        R.string.exam_counter,
                        uiState.activeWordPosition + 1,
                        uiState.examWordList.size
                    )
                }
                is ExamKnowledgeUiState.ToggleIsVariantsExpanded -> {
                    setExpandedVariants(uiState.isExpanded)
                }
                is ExamKnowledgeUiState.ToggleExpandedHint -> {
                    setExpandedHints(uiState.isExpanded, allHintsIsShown = uiState.allHintsIsShown)
                }
                is ExamKnowledgeUiState.ShowNextHint -> {
                    if (uiState.allHintsIsShown) {
                        showNextHintButton.visibility = View.GONE
                    }

                    if (uiState.currentWord.countOfRenderHints > 0) {
                        renderHints(uiState.currentWord, uiState.currentWord.countOfRenderHints)
                    }
                }
                is ExamKnowledgeUiState.ToggleHiddenDescriptionExpanded -> {
                    setHiddenTranslateDescriptionVisible(true, isExpanded = uiState.isExpanded)
                }
                is ExamKnowledgeUiState.ToggleCurrentWordTrasnalteExpanded -> {
                    setExpandedTranslates(uiState.isExpanded)
                }
                is ExamKnowledgeUiState.UpdateHiddenTranslates -> {
                    translatesAdapter.submitList(uiState.translates)
                    if (uiState.clearInputValue) {
                        examWordInput.setText("")
                    }
                }
                is ExamKnowledgeUiState.SelectVariants -> {
                    setVariantsBackground(selectedVariantValue = uiState.selectedVariantValue)
                    examWordInput.requestFocus()
                    examWordInput.setText(uiState.selectedVariantValue)
                    examWordInput.setSelection(examWordInput.text.toString().length)
                }
                is ExamKnowledgeUiState.LoadedNewPage -> {
                    examAdapter.submitList(uiState.examWordList)
                    counter.text = getString(
                        R.string.exam_counter,
                        uiState.activeWordPosition + 1,
                        uiState.examWordList.size
                    )
                }
            }
        }
    }

    private fun scrollExamWordListToCurrentPosition(activeWordPosition: Int) = with(binding) {
        wordPositionRv.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val centerOfScreen: Int = wordPositionRv.width / 2 - 100
                (wordPositionRv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                    activeWordPosition,
                    centerOfScreen
                )
                wordPositionRv.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        )
    }

    private fun handleMode(mode: ExamMode) = with(binding) {
        modeDialog.handleDialog(mode = mode)
        examEndDialog.setMode(mode)
        toolbarTitle.text = when (mode) {
            ExamMode.DAILY_MODE -> getString(R.string.exam_counter_toolbar_title_daily_mode)
            ExamMode.INFINITY_MODE -> getString(R.string.exam_counter_toolbar_title_infinity_mode)
        }
        scrollExamWordListToCurrentPosition(0)
        examAdapter.mode = mode
    }

    private fun setupBaseStyle(
        currentWord: ExamWord,
        examWordListSize: Int,
        activeWordPosition: Int
    ) {
        setClickableNavigationButtons(
            activeWordPosition = activeWordPosition,
            listSize = examWordListSize - 1
        )
        setHintsStyle(currentWord = currentWord)
        setTranslateStyles(currentWord = currentWord)
        setVariantsStyle(currentWord = currentWord)
        val isAnswerCorrect = currentWord.status == ExamWordStatus.SUCCESS
        setAnswerResult(
            isCorrect = isAnswerCorrect,
            answer = currentWord.givenAnswer
        )
    }

    private fun setVariantsBackground(selectedVariantValue: String?) = with(binding) {
        // FIXME work twice (because on edit text exist listener)
        showVariantsContainer.allViews.forEach { view ->
            val textView = view.findViewById<TextView>(R.id.chip_item) ?: return@with
            if (textView.text == selectedVariantValue && selectedVariantValue != null) {
                textView.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
            } else {
                textView.backgroundTintList = null
            }
        }
    }

    private fun loadedEmptyList() = with(binding) {
        examContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
        emptyListLayout.root.visibility = View.VISIBLE
        examModeButton.isEnabled = false
        examModeButton.alpha = 0.5f
    }

    // FIXME make it outside fragment
    private fun setClickableNavigationButtons(activeWordPosition: Int, listSize: Int) =
        with(binding) {
            if (activeWordPosition == 0 && activeWordPosition == listSize) { // list is empty
                goPrevQuestion.alpha = 0.5f
                goPrevQuestion.isClickable = false
                goNextQuestion.alpha = 0.5f
                goNextQuestion.isClickable = false
                return
            }

            if (activeWordPosition == 0) {
                goPrevQuestion.alpha = 0.5f
                goPrevQuestion.isClickable = false
            } else { // more than 0
                goPrevQuestion.alpha = 1f
                goPrevQuestion.isClickable = true
            }

            if (activeWordPosition == listSize) {
                goNextQuestion.alpha = 0.5f
                goNextQuestion.isClickable = false
            } else { // lest list size
                goNextQuestion.alpha = 1f
                goNextQuestion.isClickable = true
            }
        }

    // FIXME make it outside fragment
    private fun setAnswerResult(isCorrect: Boolean, answer: String?) = with(binding) {
        if (answer == null) {
            yourAnswerResult.visibility = View.GONE
            return@with
        }
        yourAnswerResult.visibility = View.VISIBLE
        yourAnswerResult.text = getString(R.string.exam_your_answer) + " " + answer

        val color = if (isCorrect) {
            R.color.green
        } else {
            R.color.red
        }
        yourAnswerResult.setTextColor(ContextCompat.getColor(requireContext(), color))
    }

    private fun setVariantsStyle(currentWord: ExamWord) = with(binding) {
        showVariantsContainer.removeAllViews()

        if (currentWord.givenAnswer == null) {
            renderShowVariants(currentWord)
            showVariantsLabel.visibility = View.VISIBLE
        } else {
            showVariantsLabel.visibility = View.GONE
        }

        setExpandedVariants(isExpanded = currentWord.isVariantsExpanded)
    }

    private fun setExpandedVariants(isExpanded: Boolean) = with(binding) {
        if (isExpanded) {
            showVariantsContainer.visibility = View.VISIBLE
            showVariantsLabel.text = getString(R.string.exam_hide_variants)
        } else {
            showVariantsContainer.visibility = View.GONE
            showVariantsLabel.text = getString(R.string.exam_show_variants)
        }
    }

    private fun renderShowVariants(examWord: ExamWord) = with(binding) {
        for (i in (0 until examWord.answerVariants.size)) {
            val view = LayoutInflater.from(showVariantsContainer.context)
                .inflate(R.layout.item_translate_chip, showVariantsContainer, false)
            val textItem = view.findViewById<TextView>(R.id.chip_item)
            textItem.text = examWord.answerVariants[i].value

            textItem.setOnClickListener {
                viewModel.setSelectVariant(selectedVariantValue = examWord.answerVariants[i].value)
            }
            showVariantsContainer.addView(view)
        }
    }

    private fun setHintsStyle(currentWord: ExamWord) = with(binding) {
        showHintsContainer.removeAllViews()

        if (currentWord.hints.isEmpty() || currentWord.givenAnswer != null) {
            setExpandedHints(false, false)
            showHintsLabel.visibility = View.GONE
            return@with
        }

        showHintsLabel.visibility = View.VISIBLE
        setExpandedHints(currentWord.isHintsExpanded, allHintsIsShown = currentWord.allHintsIsShown)

        if (currentWord.countOfRenderHints > 0) {
            renderHints(currentWord, currentWord.countOfRenderHints)
        }

    }

    private fun setExpandedHints(isExpanded: Boolean, allHintsIsShown: Boolean) = with(binding) {
        val visibility = if (isExpanded) View.VISIBLE else View.GONE
        showNextHintButton.visibility = if (allHintsIsShown) View.GONE else visibility
        showHintsContainer.visibility = visibility
        showHintsLabel.text =
            if (isExpanded) getString(R.string.exam_hide_hints) else getString(R.string.exam_show_hints)
    }

    private fun renderHints(examWord: ExamWord, count: Int) = with(binding) {
        showHintsContainer.removeAllViews()
        for (i in (0 until count)) {
            val view = LayoutInflater.from(showHintsContainer.context)
                .inflate(R.layout.item_exam_word_hint, showHintsContainer, false)
            view.findViewById<TextView>(R.id.hint_value).text = examWord.hints[i].value
            showHintsContainer.addView(view)
        }
    }

    private fun setTranslateStyles(currentWord: ExamWord) = with(binding) {
        if (currentWord.status == ExamWordStatus.FAIL) {
            addHiddenTranslatesContainer.addHiddenTranslate.visibility = View.VISIBLE
            // TODO add isExpanded for description
            setHiddenTranslateDescriptionVisible(
                isVisible = true,
                isExpanded = currentWord.isHiddenTranslateDescriptionExpanded
            )
        } else {
            addHiddenTranslatesContainer.addHiddenTranslate.visibility = View.GONE
            setHiddenTranslateDescriptionVisible(isVisible = false, isExpanded = false)
        }

        if (currentWord.givenAnswer != null) {
            addHiddenTranslatesContainer.root.visibility = View.VISIBLE
            setExpandedTranslates(isExpanded = currentWord.isTranslateExpanded)
        } else {
            addHiddenTranslatesContainer.root.visibility = View.GONE
        }
    }

    private fun setExpandedTranslates(isExpanded: Boolean) = with(binding) {
        val visibility = if (isExpanded) View.VISIBLE else View.INVISIBLE
        val text =
            if (isExpanded) getString(R.string.exam_hide_current_translates) else getString(R.string.exam_show_current_translates)
        addHiddenTranslatesContainer.translateChipsRv.visibility = visibility
        addHiddenTranslatesContainer.toggleExpandedCurrentTranslates.text = text
    }

    private fun setHiddenTranslateDescriptionVisible(isVisible: Boolean, isExpanded: Boolean) =
        with(binding) {
            val visibility = if (isVisible) View.VISIBLE else View.GONE
            addHiddenTranslatesContainer.hiddenTranslatesDescriptionLabel.visibility = visibility
            addHiddenTranslatesContainer.hiddenTranslatesDescription.visibility =
                if (isExpanded) visibility else View.GONE
        }

    private fun setupAdapter() = with(binding) {
        wordPositionRv.adapter = examAdapter
        examCheckAnswer.setOnClickListener { viewModel.handleExamCheckAnswer(examWordInput.text.toString()) }
        addHiddenTranslatesContainer.translateChipsRv.adapter = translatesAdapter
        addHiddenTranslatesContainer.translateChipsRv.itemAnimator = null

        wordPositionRv.itemAnimator = null
        examAdapter.clickListener = object : ExamAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                viewModel.goToWord(position)
            }
        }

        examAdapter.handleLoadNewWords = object : ExamAdapter.HandleLoadNewWords {
            override fun onLoadNewWords(position: Int) {
                viewModel.loadNewPage(position)
            }

        }

        translatesAdapter.clickListener =
            object : ModifyWordAdapter.OnItemClickListener<Translate> {
                override fun onItemClick(it: View, item: Translate) {}
                override fun onLongItemClick(it: View, item: Translate) {
                    viewModel.toggleIsHiddenTranslate(item)
                }
            }
    }
}
