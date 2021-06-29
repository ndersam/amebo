package com.amebo.amebo.screens.user

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.*
import com.amebo.amebo.R
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.toResource
import com.amebo.core.Nairaland
import com.amebo.core.domain.User
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.launch
import javax.inject.Inject


class UserScreenViewModel @Inject constructor(
    private val nairaland: Nairaland,
    application: Application
) : AndroidViewModel(application), Pref.Observer {

    private val _dataEvent = MutableLiveData<Event<Resource<User.Data>>>()
    val dataEvent: LiveData<Event<Resource<User.Data>>> = _dataEvent

    private val _followUserEvent = MutableLiveData<Event<Resource<User.Data>>>()
    val followUserEvent: LiveData<Event<Resource<User.Data>>> = _followUserEvent

    private lateinit var user: User
    private var hasBeenLoaded = false
    private var userData: User.Data? = null

    private val prefObservable = Pref.Observable().apply {
        subscribe(
            getApplication(),
            this@UserScreenViewModel,
            R.string.key_current_user
        )
    }

    override fun onCleared() {
        super.onCleared()
        prefObservable.unsubscribe(getApplication())
    }

    override fun onPreferenceChanged(key: Int, contextChanged: Boolean) {
        if (key == R.string.key_current_user) {
            hasBeenLoaded = false
            userData = null
        }
    }

    fun initialize(user: User) {
        this.user = user
        if (userData == null && user.data != null) {
            userData = user.data
        }
    }

    fun loadConditionally() {
        if (!hasBeenLoaded) {
            load()
        } else {
            _dataEvent.value = Event(Resource.Success(userData!!))
        }
    }

    fun load() {
        viewModelScope.launch {
            userData = nairaland.sources.users.fetchCached(user)
            _dataEvent.value = Event(Resource.Loading(userData))

            val response = nairaland.sources.users.fetchData(user)
            if (response is Ok) {
                userData = response.value
                hasBeenLoaded = true
            }
            _dataEvent.value = Event(response.toResource(userData))
        }
    }

    fun follow(follow: Boolean) {
        viewModelScope.launch {
            _followUserEvent.value = Event(Resource.Loading())
            val resource =
                nairaland.sources.submissions.followUser(user, follow).toResource(userData)
            if (resource is Resource.Success) {
                user.data = resource.content
            }
            _followUserEvent.value = Event(resource)
        }
    }

    fun viewTwitter(context: Context, rawText: String) {
        var text = rawText.trim()
        if (text.startsWith("@")) {
            text = text.substringAfter("2").trim()
        }
        if (text.contains("twitter")) {
            text = text.substringAfter("twitter")
        }
        if (text.lastIndexOf('/') != text.length - 1) {
            text = text.substringAfterLast('/')
        }
        val intent: Intent = try {
            // this will fail if twitter is not installed
            context.packageManager.getPackageInfo("com.twitter.android", 0)
            Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=$text"))
        } catch (e: Exception) {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/$text"))
        }
        context.startActivity(intent)
    }
}
