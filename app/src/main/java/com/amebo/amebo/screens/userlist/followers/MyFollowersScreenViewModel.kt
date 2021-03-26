package com.amebo.amebo.screens.userlist.followers

import android.app.Application
import androidx.lifecycle.*
import com.amebo.amebo.R
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.toResource
import com.amebo.core.Nairaland
import com.amebo.core.domain.ResultWrapper
import com.amebo.core.domain.User
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyFollowersScreenViewModel @Inject constructor(
    private val nairaland: Nairaland,
    application: Application
) : AndroidViewModel(application), Pref.Observer {

    private val _dataEvent = MutableLiveData<Event<Resource<List<User>>>>()
    val dataEvent: LiveData<Event<Resource<List<User>>>> = _dataEvent
    private var users: List<User>? = null

    private val prefObservable = Pref.Observable().apply {
        subscribe(getApplication(), this@MyFollowersScreenViewModel, R.string.key_current_user)
    }

    override fun onPreferenceChanged(key: Int, contextChanged: Boolean) {
        if (key == R.string.key_current_user) {
            users = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        prefObservable.unsubscribe(getApplication())
    }

    fun load() {
        viewModelScope.launch {
            _dataEvent.value = Event(Resource.Loading(users))

            val result = nairaland.sources.users.fetchFollowers()
            if (result is ResultWrapper.Success) {
                users = result.data
            }
            _dataEvent.value = Event(result.toResource(users))
        }
    }
}