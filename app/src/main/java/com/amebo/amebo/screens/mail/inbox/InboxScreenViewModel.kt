package com.amebo.amebo.screens.mail.inbox

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.common.extensions.toResource
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.core.Nairaland
import com.amebo.core.domain.DismissMailNotificationForm
import kotlinx.coroutines.launch
import javax.inject.Inject

class InboxScreenViewModel @Inject constructor(private val nairaland: Nairaland): ViewModel() {

    var form: DismissMailNotificationForm? = null
    private val _dismissMailEvent = MutableLiveData<Event<Resource<Unit>>>()
    val dismissMailEvent: LiveData<Event<Resource<Unit>>> = _dismissMailEvent

    fun dismiss(){
        val form = form ?: return
        viewModelScope.launch {
            _dismissMailEvent.value = Event(Resource.Loading())
            _dismissMailEvent.value = Event(
                nairaland.sources.submissions.dismissMailNotification(form).toResource(null)
            )
        }
    }
}