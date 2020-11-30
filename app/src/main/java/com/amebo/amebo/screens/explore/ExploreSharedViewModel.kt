package com.amebo.amebo.screens.explore

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amebo.amebo.common.Event
import com.amebo.core.domain.Board
import javax.inject.Inject

class ExploreSharedViewModel @Inject constructor() : ViewModel() {
    var board: Board? = null
    var onlyTopics: Boolean = false
    var onlyImages: Boolean = false
    var query: String = ""
    val optionsReady =  MutableLiveData<Event<Boolean>>()
    val selectBoardReady = MutableLiveData<Event<Boolean>>()
}