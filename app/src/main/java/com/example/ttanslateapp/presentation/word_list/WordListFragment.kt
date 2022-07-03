package com.example.ttanslateapp.presentation.word_list

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.ttanslateapp.R
import com.example.ttanslateapp.databinding.FragmentWordListBinding
import com.example.ttanslateapp.presentation.core.BaseFragment
import com.example.ttanslateapp.presentation.core.BindingInflater
import com.example.ttanslateapp.presentation.exam.ExamKnowledgeWordsFragment
import com.example.ttanslateapp.presentation.modify_word.ModifyWordFragment
import com.example.ttanslateapp.presentation.modify_word.ModifyWordModes
import com.example.ttanslateapp.presentation.word_list.adapter.WordListAdapter
import com.example.ttanslateapp.util.getAppComponent
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class WordListFragment : BaseFragment<FragmentWordListBinding>() {

    override val bindingInflater: BindingInflater<FragmentWordListBinding>
        get() = FragmentWordListBinding::inflate

    private val viewModel by viewModels {
        get(WordListViewModel::class.java)
    }

    private val wordListAdapter = WordListAdapter()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAppComponent().inject(this)

        setAdapter()
        clickListeners()
        observeData()

        binding.searchWord.setOnQueryTextFocusChangeListener { v, hasFocus ->
            viewModel.searchInputHasFocus = hasFocus
        }

        binding.searchWord.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Timber.d(query.toString())
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (viewModel.searchInputHasFocus) {
                    viewModel.searchDebounced(query.toString())
                }

                return true
            }
        })
    }

    private fun observeData() = with(binding) {
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                is WordListViewModelState.IsLoading -> {

                }
                is WordListViewModelState.LoadSuccess -> {
                    if (uiState.wordList.isEmpty()) {
                        nothingFoundContainer.root.visibility = View.VISIBLE
                        wordListRv.visibility = View.GONE
                    } else {
                        nothingFoundContainer.root.visibility = View.GONE
                        wordListRv.visibility = View.VISIBLE
                    }

                    if (uiState.dictionaryIsEmpty) {
                        emptyListContainer.root.visibility = View.VISIBLE
                        wordListContainer.visibility = View.GONE
                    } else {
                        emptyListContainer.root.visibility = View.GONE
                        wordListContainer.visibility = View.VISIBLE
                    }

                    wordListAdapter.submitList(uiState.wordList) {
                        binding.wordListRv.scrollToPosition(0)
                    }
                }
            }
        }
    }

    private fun clickListeners() = with(binding) {
        searchWord.setOnClickListener { searchWord.isIconified = false }
        addNewWord.setOnClickListener { launchAddWordScreen() }
        emptyListContainer.addFirstWord.setOnClickListener { launchAddWordScreen() }

        wordListAdapter.onClickListener = object : WordListAdapter.OnClickListener {
            override fun onRootClickListener(wordId: Long) {
                launchEditWordScreen(wordId)
            }
        }
    }

    private fun launchAddWordScreen() {
        findNavController().navigate(
            WordListFragmentDirections.actionWordListFragmentToModifyWordFragment(ModifyWordModes.MODE_ADD)
        )
    }

    private fun launchEditWordScreen(wordId: Long) {
        findNavController().navigate(
            WordListFragmentDirections.actionWordListFragmentToModifyWordFragment(
                mode = ModifyWordModes.MODE_EDIT,
                wordId = wordId
            )
        )
    }

    private fun setAdapter() {
        binding.wordListRv.itemAnimator = null

        wordListAdapter
            .apply { binding.wordListRv.adapter = this }

        val callback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = wordListAdapter.currentList[viewHolder.adapterPosition]
                viewModel.deleteWordById(item.id)
            }
        }

        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.wordListRv)
    }

}