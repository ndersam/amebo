package com.amebo.amebo.screens.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.common.Event
import com.amebo.core.Nairaland
import com.amebo.core.domain.Board
import com.amebo.core.domain.SearchQuery
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModel @Inject constructor(private val nairaland: Nairaland) : ViewModel() {
    private val _searchHistoryEvent = MutableLiveData<Event<List<String>>>()
    val searchHistoryEvent: LiveData<Event<List<String>>> = _searchHistoryEvent

    private val _suggestionsEvent = MutableLiveData<Event<List<Board>>>()
    val suggestionsEvent: LiveData<Event<List<Board>>> = _suggestionsEvent

    fun initialize() {
        viewModelScope.launch {
            _searchHistoryEvent.value = Event(nairaland.sources.misc.searchHistory())
        }
    }

    fun findSuggestions(query: String) {
        viewModelScope.launch {
            _suggestionsEvent.value = Event(nairaland.sources.boards.search("%${query}%"))
        }
    }

    fun removeSearchHistory(term: String) {
        viewModelScope.launch {
            nairaland.sources.misc.removeSearch(term)
        }
    }

    fun saveSearch(query: SearchQuery) {
        viewModelScope.launch {
            nairaland.sources.misc.saveSearch(query.query)
        }
    }
}