package com.amebo.amebo.application

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.common.Optional
import com.amebo.amebo.common.Event
import com.amebo.core.Nairaland
import com.amebo.core.domain.IntentParseResult
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val nairaland: Nairaland) : ViewModel() {
    private val _unknownUriResultEvent = MutableLiveData<Event<Optional<IntentParseResult>>>()
    val unknownUriResultEvent: LiveData<Event<Optional<IntentParseResult>>> = _unknownUriResultEvent

    fun handleUri(uri: Uri) {
        viewModelScope.launch {
            val result = nairaland.sources.misc.parseIntent(uri.toString())
            _unknownUriResultEvent.value = Event(Optional(result))
        }
    }
}

