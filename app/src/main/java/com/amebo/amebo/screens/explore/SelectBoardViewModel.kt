package com.amebo.amebo.screens.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Pref
import com.amebo.core.Nairaland
import com.amebo.core.domain.Board
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectBoardViewModel @Inject constructor(
    private val server: Nairaland,
    private val pref: Pref,
    app: Application
) : AndroidViewModel(app) {

    private val isLoggedIn: Boolean get() = pref.isLoggedIn
    private val userString: String? get() = pref.userName
    private val exploreData = MutableLiveData<TopicListData>()
    private val suggestions = MutableLiveData<Event<List<Board>>>()
    private var searchJob: Job? = null

    private fun load() {
        viewModelScope.launch {
            val followed = if (isLoggedIn)
                server.sources.boards.loadFollowedFromDisk(userString!!)
            else
                server.sources.boards.loadNairalandPicks()
            val recent = server.sources.boards.loadRecent()
            val allBoards = server.sources.boards.loadAll()
            exploreData.value =
                TopicListData(recent, followed, allBoards)
        }
    }


    fun setView(observer: (MutableLiveData<TopicListData>, MutableLiveData<Event<List<Board>>>) -> Unit) {
        observer(exploreData, suggestions)
        load()
    }

    fun findSuggestions(query: String) {
        if (searchJob?.isActive == true) {
            searchJob?.cancel()
        }

        searchJob = viewModelScope.launch {
            suggestions.value = Event(server.sources.boards.search("%${query}%"))
        }
    }
}
