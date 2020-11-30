package com.amebo.amebo.screens.settings

import androidx.lifecycle.*
import com.amebo.core.Nairaland
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(private val nairaland: Nairaland) : ViewModel() {

    fun clearSearchHistory(viewLifecycleOwner: LifecycleOwner, completionListener: () -> Unit) {
        viewModelScope.launch {
            nairaland.sources.misc.removeAllSearchHistory()
            MutableLiveData(Unit).apply {
                value = Unit
                observe(viewLifecycleOwner, Observer { completionListener() })
            }
        }
    }

    fun clearTopicHistory(viewLifecycleOwner: LifecycleOwner, completionListener: () -> Unit) {
        viewModelScope.launch {
            nairaland.sources.postLists.removeAllTopics()
            MutableLiveData(Unit).apply {
                value = Unit
                observe(viewLifecycleOwner, Observer { completionListener() })
            }
        }
    }

}