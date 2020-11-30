package com.amebo.amebo.screens.postlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amebo.amebo.common.Event
import javax.inject.Inject

class SelectedPageSharedViewModel @Inject constructor(): ViewModel() {
    val liveData = MutableLiveData<Event<Int>>()
}