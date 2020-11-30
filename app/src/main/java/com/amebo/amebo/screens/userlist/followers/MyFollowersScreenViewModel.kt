package com.amebo.amebo.screens.userlist.followers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.common.extensions.toResource
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.core.Nairaland
import com.amebo.core.domain.ResultWrapper
import com.amebo.core.domain.User
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyFollowersScreenViewModel @Inject constructor(private val nairaland: Nairaland) :
    ViewModel() {

    private val _dataEvent = MutableLiveData<Event<Resource<List<User>>>>()
    val dataEvent: LiveData<Event<Resource<List<User>>>> = _dataEvent
    private var users: List<User>? = null

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