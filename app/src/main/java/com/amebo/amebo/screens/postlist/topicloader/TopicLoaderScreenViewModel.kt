package com.amebo.amebo.screens.postlist.topicloader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.toResource
import com.amebo.core.Nairaland
import com.amebo.core.domain.TopicPostListDataPage
import kotlinx.coroutines.launch
import javax.inject.Inject

class TopicLoaderScreenViewModel @Inject constructor(private val nairaland: Nairaland) :
    ViewModel() {
    private val _event = MutableLiveData<Resource<TopicPostListDataPage>>()
    val event: LiveData<Resource<TopicPostListDataPage>> = _event

    fun load(postId: String) {
        viewModelScope.launch {
            _event.value = Resource.Loading()
            _event.value = nairaland.sources.postLists.fetchPageWithPost(postId).toResource(null)
        }
    }

}