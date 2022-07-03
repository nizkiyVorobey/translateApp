package com.example.ttanslateapp.presentation.exam

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.allViews
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.example.ttanslateapp.R
import com.example.ttanslateapp.databinding.FragmentExamKnowledgeWordsBinding
import com.example.ttanslateapp.domain.model.exam.ExamWord
import com.example.ttanslateapp.domain.model.exam.ExamWordStatus
import com.example.ttanslateapp.domain.model.modify_word_chip.TranslateWordItem
import com.example.ttanslateapp.presentation.core.BaseFragment
import com.example.ttanslateapp.presentation.core.BindingInflater
import com.example.ttanslateapp.presentation.exam.adapter.ExamAdapter
import com.example.ttanslateapp.presentation.exam.adapter.ExamKnowledgeUiState
import com.example.ttanslateapp.presentation.modify_word.ModifyWordModes
import com.example.ttanslateapp.presentation.modify_word.adapter.ModifyWordAdapter
import com.example.ttanslateapp.presentation.modify_word.adapter.translate.TranslateAdapter
import com.example.ttanslateapp.util.getAppComponent
import com.example.ttanslateapp.util.setOnTextChange


class ExamKnowledgeWordsFragment : BaseFragment<FragmentExamKnowledgeWordsBinding>() {
    private var selectableHint: TextView? = null

    override val bindingInflater: BindingInflater<FragmentExamKnowledgeWordsBinding>
        get() = FragmentExamKnowledgeWordsBinding::inflate

    private val viewModel by viewModels {
        get(ExamKnowledgeWordsViewModel::class.java)
    }
    private val examAdapter = ExamAdapter()
    private val translatesAdapter = TranslateAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AAAAAAA", "FFF")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAppComponent().inject(this)
        viewModel.generateWordsList()
        setupAdapter()
        observeLiveDate()
        clickListeners()

        binding.examWordInput.setOnTextChange {
            viewModel.handleAnswerEditText(it.toString())
            binding.examWordContainer.error = null
            // clear variant backgroundTintList on user change input manually
            if (it.toString() != selectableHint?.text.toString()) {
                selectableHint?.backgroundTintList = null
            }
        }

    }

    private fun clickListeners() = with(binding) {
        showVariantsLabel.setOnClickListener {
            viewModel.toggleVisibleVariants()
        }
        showHintsLabel.setOnClickListener {
            viewModel.toggleVisibleHint()
        }
        showNextHintButton.setOnClickListener {
            viewModel.showNextHint()
        }
        emptyListContainer.addFirstWord.setOnClickListener {
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
    }

    private fun observeLiveDate() = with(binding) {
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                is ExamKnowledgeUiState.IsLoadingWords -> {
                    progressBar.visibility = View.GONE
                }
                is ExamKnowledgeUiState.LoadedEmptyList -> {
                    examContainer.visibility = View.GONE
                    emptyListContainer.root.visibility = View.VISIBLE
                }
                is ExamKnowledgeUiState.LoadedWordsSuccess -> {
                    progressBar.visibility = View.GONE
                    examContainer.visibility = View.VISIBLE

                    examAdapter.submitList(uiState.examWordList)
                    examWordName.text = uiState.currentWord.value
                    examWordInput.text = null

                    setDefaultHintsVisibility(
                        currentWord = uiState.currentWord,
                        countShownHints = uiState.countShownHints
                    )
                    setDefaultVariantsVisibility(currentWord = uiState.currentWord)
                    setClickableNavigationButtons(
                        activeWordPosition = uiState.activeWordPosition,
                        listSize = uiState.examWordList.size - 1
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
                }
                is ExamKnowledgeUiState.CheckedAnswer -> {
                    if (uiState.isExamEnd) {
                        examCheckAnswer.setOnClickListener {
                            findNavController().popBackStack()
                        }
                    }
                    examCheckAnswer.isEnabled = false

                    examAdapter.submitList(uiState.examWordList)
                    val isAnswerCorrect = uiState.status == ExamWordStatus.SUCCESS

                    if (isAnswerCorrect) {
                        selectableHint?.backgroundTintList =
                            ContextCompat.getColorStateList(requireContext(), R.color.green)
                    }

                    if (!isAnswerCorrect) {
                        selectableHint?.backgroundTintList =
                            ContextCompat.getColorStateList(requireContext(), R.color.red)
                        setVisibleHints(false)
                        setVisibleVariants(false)
                        addHiddenTranslatesContainer.root.visibility = View.VISIBLE
                        addHiddenTranslatesContainer.addHiddenTranslate.visibility = View.VISIBLE
                        addHiddenTranslatesContainer.hiddenTranslatesDescriptionLabel.visibility =
                            View.VISIBLE
                    }

                    setAnswerResult(isCorrect = isAnswerCorrect, answer = uiState.givenAnswer)
                    showVariantsContainer.allViews.forEach { it.isClickable = false }
                }
                is ExamKnowledgeUiState.QuestionNavigation -> {
                    val centerOfScreen: Int = wordPositionRv.width / 2 - 100
                    (wordPositionRv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                        uiState.activeWordPosition,
                        centerOfScreen
                    )

                    setClickableNavigationButtons(
                        activeWordPosition = uiState.activeWordPosition,
                        listSize = uiState.examWordList.size - 1
                    )

                    // if answer was give show answer else hide
                    if (uiState.currentWord.givenAnswer != null) {
                        val isAnswerCorrect = uiState.currentWord.status == ExamWordStatus.SUCCESS
                        setAnswerResult(
                            isCorrect = isAnswerCorrect,
                            answer = uiState.currentWord.givenAnswer
                        )
                    } else {
                        yourAnswerResult.visibility = View.GONE
                    }

                    if (uiState.currentWord.isFreeze) {
                        setVisibleHints(false)
                        setVisibleVariants(false)
                        setExpandedTranslates(isExpanded = false)

                        addHiddenTranslatesContainer.root.visibility = View.VISIBLE
                        translatesAdapter.submitList(uiState.currentWord.translates)
                        addHiddenTranslatesContainer.addHiddenTranslate.visibility = View.GONE
                        addHiddenTranslatesContainer.hiddenTranslatesDescriptionLabel.visibility =
                            View.GONE
                        addHiddenTranslatesContainer.hiddenTranslatesDescription.visibility =
                            View.GONE
                    } else {
                        setDefaultVariantsVisibility(currentWord = uiState.currentWord)
                        setDefaultHintsVisibility(
                            countShownHints = uiState.countShownHints,
                            currentWord = uiState.currentWord
                        )
                        addHiddenTranslatesContainer.root.visibility = View.GONE
                    }
                    examAdapter.submitList(uiState.examWordList)

                    examWordName.text = uiState.currentWord.value
                    examWordInput.setText("")

                    examCheckAnswer.text = getString(R.string.check_answer)
                    counter.text = getString(
                        R.string.exam_counter,
                        uiState.activeWordPosition + 1,
                        uiState.examWordList.size
                    )
                }
                is ExamKnowledgeUiState.ToggleIsVisibleVariants -> {
                    if (uiState.isVisible) {
                        showVariantsContainer.visibility = View.VISIBLE
                        showVariantsLabel.text = getString(R.string.exam_hide_variants)
                    } else {
                        showVariantsContainer.visibility = View.GONE
                        showVariantsLabel.text = getString(R.string.exam_show_variants)
                    }
                }
                is ExamKnowledgeUiState.ToggleIsVisibleHint -> {
                    if (uiState.isVisible) {
                        showHintsContainer.visibility = View.VISIBLE
                        showNextHintButton.visibility = uiState.nextHintButtonVisibility
                        showHintsLabel.text = getString(R.string.exam_hide_hints)
                    } else {
                        showHintsContainer.visibility = View.GONE
                        showNextHintButton.visibility = View.GONE
                        showHintsLabel.text = getString(R.string.exam_show_hints)
                    }
                }
                is ExamKnowledgeUiState.ShowNextHint -> {
                    if (uiState.allHintsShown) {
                        showNextHintButton.visibility = View.GONE
                    }

                    if (uiState.countShownHints > 0) {
                        renderHints(uiState.currentWord, uiState.countShownHints)
                    }
                }
                is ExamKnowledgeUiState.ToggleVisibilityHiddenDescription -> {
                    addHiddenTranslatesContainer.hiddenTranslatesDescription.visibility =
                        uiState.visibility
                }
                is ExamKnowledgeUiState.ToggleCurrentWordTrasnalteExpanded -> {
                    setExpandedTranslates(uiState.isExpanded)
                    translatesAdapter.submitList(uiState.translates)
                }
                is ExamKnowledgeUiState.UpdateHiddenTranslates -> {
                    translatesAdapter.submitList(uiState.translates)
                    if (uiState.clearInputValue) {
                        examWordInput.setText("")
                    }
                }
            }
        }
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
    private fun setAnswerResult(isCorrect: Boolean, answer: String) = with(binding) {
        yourAnswerResult.visibility = View.VISIBLE
        yourAnswerResult.text = getString(R.string.exam_your_answer) + " " + answer

        val color = if (isCorrect) {
            R.color.green
        } else {
            R.color.red
        }
        yourAnswerResult.setTextColor(ContextCompat.getColor(requireContext(), color));
    }

    private fun setDefaultVariantsVisibility(currentWord: ExamWord) = with(binding) {
        showVariantsContainer.visibility = View.GONE
        showVariantsContainer.removeAllViews()
        renderShowVariants(currentWord)
        showVariantsLabel.visibility = View.VISIBLE
        showVariantsLabel.text = getString(R.string.exam_show_variants)
    }

    private fun setDefaultHintsVisibility(countShownHints: Int, currentWord: ExamWord) =
        with(binding) {
            showHintsContainer.removeAllViews()

            if (currentWord.hints.isEmpty()) {
                setVisibleHints(false)
            } else {
                showHintsLabel.visibility = View.VISIBLE
                showHintsContainer.visibility = View.GONE
                showNextHintButton.visibility = View.GONE
                if (countShownHints > 0) {
                    renderHints(currentWord, countShownHints)
                }
            }

            showHintsLabel.text = getString(R.string.exam_show_hints)
        }

    private fun setVisibleHints(isVisible: Boolean) = with(binding) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        showNextHintButton.visibility = visibility
        showHintsContainer.visibility = visibility
        showHintsLabel.visibility = visibility
    }

    private fun setVisibleVariants(isVisible: Boolean) = with(binding) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        showVariantsContainer.visibility = visibility
        showVariantsLabel.visibility = visibility
    }

    private fun setExpandedTranslates(isExpanded: Boolean) = with(binding) {
        val visibility = if (isExpanded) View.VISIBLE else View.GONE
        val text =
            if (isExpanded) getString(R.string.exam_hide_current_translates) else getString(R.string.exam_show_current_translates)
        addHiddenTranslatesContainer.translateChipsRv.visibility = visibility
        addHiddenTranslatesContainer.toggleExpandedCurrentTranslates.text = text
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

    private fun renderShowVariants(examWord: ExamWord) = with(binding) {
        for (i in (0 until examWord.answerVariants.size)) {
            val view = LayoutInflater.from(showVariantsContainer.context)
                .inflate(R.layout.item_translate_chip, showVariantsContainer, false)
            val textItem = view.findViewById<TextView>(R.id.chip_item)
            textItem.text = examWord.answerVariants[i].value

            textItem.setOnClickListener {
                showVariantsContainer.allViews.forEach {
                    it.backgroundTintList = null
                }

                it.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)

                selectableHint = textItem
                examWordInput.setText(textItem.text.toString().trim())
                examWordInput.setSelection(examWordInput.text.toString().length)
            }
            showVariantsContainer.addView(view)
        }
    }

    private fun setupAdapter() = with(binding) {
        wordPositionRv.adapter = examAdapter
        examCheckAnswer.setOnClickListener {
            viewModel.handleExamCheckAnswer(examWordInput.text.toString())
        }
        addHiddenTranslatesContainer.translateChipsRv.adapter = translatesAdapter

        // FIXME animation only for stroke
        // TODO maybe add navigation by tap in recyclerview item
        wordPositionRv.itemAnimator = null;
//        // disable scroll with touch
//        val disabler: OnItemTouchListener = RecyclerViewDisabler()
//        wordPositionRv.addOnItemTouchListener(disabler)

        translatesAdapter.clickListener =
            object : ModifyWordAdapter.OnItemClickListener<TranslateWordItem> {
                override fun onItemClick(it: View, item: TranslateWordItem) {

                }

                override fun onLongItemClick(it: View, item: TranslateWordItem) {
                    viewModel.toggleIsHiddenTranslate(item)
                }
            }

    }

}

class RecyclerViewDisabler : OnItemTouchListener {
    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        return true
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}