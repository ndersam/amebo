package com.amebo.amebo.screens.topiclist.simple

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.common.Event
import com.amebo.core.Nairaland
import com.amebo.core.domain.Topic
import kotlinx.coroutines.launch
import javax.inject.Inject

class SimpleTopicListScreenViewModel @Inject constructor(
    private val nairaland: Nairaland
) : ViewModel() {


    private var viewedTopics = mutableSetOf<Int>()
    private val _viewedTopicsLoadedEvent = MutableLiveData<Event<Unit>>()
    val viewedTopicsLoadedEvent: LiveData<Event<Unit>> = _viewedTopicsLoadedEvent


    fun initialize() {
        viewModelScope.launch {
            viewedTopics = nairaland.sources.postLists.allViewedTopicIds()
            _viewedTopicsLoadedEvent.value = Event(Unit)
        }
    }

    fun hasViewedTopic(topic: Topic): Boolean = viewedTopics.contains(topic.id)
}

