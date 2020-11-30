package com.amebo.amebo.screens.topiclist.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amebo.core.Nairaland
import com.amebo.core.domain.Topic
import kotlinx.coroutines.launch
import javax.inject.Inject

class ViewedTopicsViewModel @Inject constructor(private val nairaland: Nairaland) : ViewModel() {
    private val _dataEvent = MutableLiveData<List<Topic>>()
    val dataEvent: LiveData<List<Topic>> = _dataEvent

    fun load() {
        viewModelScope.launch {
            _dataEvent.value = nairaland.sources.postLists.allViewedTopics()
        }
    }

    fun removeTopic(topic: Topic, reLoad: Boolean = true) {
        viewModelScope.launch {
            nairaland.sources.postLists.removeVisitedTopic(topic)
        }
        if (reLoad) load()
    }

}