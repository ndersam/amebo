package com.amebo.amebo.screens.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.toResource
import com.amebo.core.Nairaland
import com.amebo.core.domain.Board
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExploreScreenViewModel @Inject constructor(
    private val nairaland: Nairaland,
    private val pref: Pref,
    application: Application
) : AndroidViewModel(application) {

    private val _exploreData = MutableLiveData<ExploreData>()
    val exploreData: LiveData<ExploreData> = _exploreData

    /**
     * Stores boards followed by the current authenticated  account.
     */
    private val _fetchedFollowedBoardsEvent = MutableLiveData<Event<Resource<List<Board>>>>()
    val fetchedFollowedBoardsEvent: LiveData<Event<Resource<List<Board>>>> =
        _fetchedFollowedBoardsEvent


    fun loadBoards() {
        viewModelScope.launch {
            val followed = if (pref.isLoggedIn) {
                // load cached boards
                nairaland.sources.boards.loadFollowedFromDisk(pref.userName!!)
            } else
                emptyList()
            val recent = nairaland.sources.boards.loadRecent()
            val search = nairaland.sources.misc.searchHistory()
            _exploreData.value =
                ExploreData(
                    TopicListData(
                        recent,
                        followed,
                        nairaland.sources.boards.loadNairalandPicks()
                    ), search.toMutableList()
                )
        }
    }

    fun fetchFollowedBoards() {
        if(pref.isLoggedOut) return
        viewModelScope.launch {
            val existing = _exploreData.value?.topicListData?.followed
            _fetchedFollowedBoardsEvent.value = Event(
                Resource.Loading(_exploreData.value?.topicListData?.followed)
            )
            val resp = nairaland.sources.boards.fetchFollowedBoards()
            if (resp is Ok) {
                nairaland.sources.boards.updateFollowedBoards(pref.userName!!, resp.value)
            }
            _fetchedFollowedBoardsEvent.value = Event(resp.toResource(existing))
        }
    }



}
