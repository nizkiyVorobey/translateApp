package com.example.ttanslateapp.presentation.word_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ttanslateapp.domain.model.WordRV
import com.example.ttanslateapp.domain.use_case.GetSearchedWordListUseCase
import com.example.ttanslateapp.domain.use_case.GetSearchedWordListUseCase.Companion.WORD_LIST_PAGE_SIZE
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


sealed interface WordListViewModelState {
    data class IsLoading(val isLoading: Boolean) : WordListViewModelState
    data class LoadSuccess(val wordList: List<WordRV>, val dictionaryIsEmpty: Boolean) :
        WordListViewModelState

    data class LoadedNewPage(val wordList: List<WordRV>) : WordListViewModelState

    data class RestoreUI(val wordList: List<WordRV>, val dictionaryIsEmpty: Boolean) :
        WordListViewModelState
}

data class WordListState(
    val wordList: List<WordRV> = emptyList(),
    val isVisited: Boolean = false
)

class WordListViewModel @Inject constructor(
    private val getSearchedWordListUseCase: GetSearchedWordListUseCase,
) : ViewModel() {
    private val _uiState = MutableLiveData<WordListViewModelState>()
    val uiState: LiveData<WordListViewModelState> = _uiState

    private var state = WordListState()

    private var searchJob: Job? = null
    private var dictionaryIsEmpty = false

    var searchInputValue = ""

    init {
        _uiState.value = WordListViewModelState.IsLoading(true)
        searchDebounced("")
    }

    // if we visited screen in the past in our session restore ui, if it is the first time - ignore
    fun restoreUI() {
        if (!state.isVisited) return

        _uiState.value = WordListViewModelState.RestoreUI(
            wordList = state.wordList,
            dictionaryIsEmpty = dictionaryIsEmpty
        )
    }

    //  searchJob?.cancel() help avoid multiple flow update. We save only last request
    fun loadNewPage(position: Int) {
        if (state.wordList.size - position > 5) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            getSearchedWordListUseCase.loadNextPage(searchInputValue)?.collectLatest {
                state = state.copy(
                    wordList = it
                )

                _uiState.value = WordListViewModelState.LoadedNewPage(
                    wordList = state.wordList,
                )
            }
        }
    }

    private suspend fun searchWord(searchValue: String) {
        getSearchedWordListUseCase.loadData(searchValue)
            .collectLatest {
                val list = it
                    .map { it.copy(translates = it.translates.filter { it.isHidden == false }) }
                    .apply {
                        if (searchValue.isEmpty()) it else it.sortedBy { it.value.length }
                    }

                if (searchValue.isEmpty()) {
                    dictionaryIsEmpty = it.isEmpty()
                }
                state = state.copy(wordList = list, isVisited = true)
                _uiState.value = WordListViewModelState.LoadSuccess(
                    wordList = list,
                    dictionaryIsEmpty = dictionaryIsEmpty
                )
            }
    }

    fun searchDebounced(searchText: String) {
        getSearchedWordListUseCase.resetExamWordListCurrentPage()
        state = state.copy(
            wordList = emptyList()
        )

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            searchWord(searchText)
        }
    }

}