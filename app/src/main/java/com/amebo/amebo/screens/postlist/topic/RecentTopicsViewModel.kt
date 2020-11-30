package com.amebo.amebo.screens.postlist.topic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amebo.core.Nairaland
import com.amebo.core.domain.Topic
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecentTopicsViewModel @Inject constructor(private val nairaland: Nairaland) : ViewModel() {
    private val _recentTopicsLiveData = MutableLiveData<List<Topic>>()
    val recentTopicsLiveData: LiveData<List<Topic>> = _recentTopicsLiveData

    fun load() {
        viewModelScope.launch {
            _recentTopicsLiveData.value = nairaland.sources.postLists.recentTopics(10)
        }
    }

    fun removeTopic(topic: Topic, reLoad: Boolean = true) {
        viewModelScope.launch {
            nairaland.sources.postLists.removeVisitedTopic(topic)
        }
        if (reLoad) load()
    }

}