package com.amebo.amebo.screens.postlist.topic

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Pref
import com.amebo.amebo.screens.postlist.PostListScreenViewModel
import com.amebo.core.Nairaland
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.PostListDataPage
import com.amebo.core.domain.Topic
import com.amebo.core.domain.TopicPostListDataPage
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.launch
import javax.inject.Inject


class TopicViewModel @Inject constructor(
    nairaland: Nairaland,
    pref: Pref,
    application: Application
) :
    PostListScreenViewModel<Topic>(nairaland, pref, application) {
    val isClosed get() = (dataPage as? TopicPostListDataPage)?.isClosed
    val isHidden get() = (dataPage as? TopicPostListDataPage)?.isHiddenFromUser
    val isFollowing get() = (dataPage as? TopicPostListDataPage)?.isFollowingTopic == true

    private val _addViewedTopicEvent = MutableLiveData<Event<Unit>>()
    val addViewedTopicEvent: LiveData<Event<Unit>> = _addViewedTopicEvent


    val dataExists get() = dataPage != null

    override suspend fun onDataPageFetched(result: Result<PostListDataPage, ErrorResponse>) {
        super.onDataPageFetched(result)
        if (result is Ok) {
            val dataPage = result.value as TopicPostListDataPage
            nairaland.sources.postLists.updateViewedTopic(dataPage.topic)
        }
    }


    fun followTopic(follow: Boolean) {
        viewModelScope.launch {
            val dataPage = dataPage as TopicPostListDataPage
            val result = if (follow)
                nairaland.sources.submissions.followTopic(dataPage)
            else
                nairaland.sources.submissions.unFollowTopic(dataPage)
            setResult(result)
        }
    }

    fun markVisited() {
        viewModelScope.launch {
            nairaland.sources.postLists.addViewedTopic(postList)
            _addViewedTopicEvent.value = Event(Unit)
        }
    }
}