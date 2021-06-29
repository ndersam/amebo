package com.amebo.amebo.screens.accounts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.R
import com.amebo.amebo.common.AvatarGenerator
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.toResource
import com.amebo.core.Nairaland
import com.amebo.core.domain.*
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserManagementViewModel @Inject constructor(
    private val observable: NairalandSessionObservable,
    private val nairaland: Nairaland,
    private val pref: Pref,
    application: Application
) : AndroidViewModel(application), NairalandSessionObservable.Observer, Pref.Observer {

    private val _accountListEvent = MutableLiveData<Event<List<UserAccount>>>()
    val accountListEvent: LiveData<Event<List<UserAccount>>> = _accountListEvent

    private val _loginEvent = MutableLiveData<Event<Resource<Unit>>>()
    val loginEvent: LiveData<Event<Resource<Unit>>> = _loginEvent

    private val _removeUserAccountEvent = MutableLiveData<Event<Pair<RealUserAccount, Boolean>>>()
    val removeUserAccountEvent: LiveData<Event<Pair<RealUserAccount, Boolean>>> =
        _removeUserAccountEvent

    private val _userLoggedOutEvent = MutableLiveData<Event<Unit>>()
    val userLoggedOutEvent: LiveData<Event<Unit>> = _userLoggedOutEvent

    private val _displayPhotoEvent = MutableLiveData<Event<Resource<DisplayPhoto>>>()
    val displayPhotoEvent: LiveData<Event<Resource<DisplayPhoto>>> = _displayPhotoEvent
    private var displayPhoto: DisplayPhoto? = null

    private val _sessionEvent = MutableLiveData<Session>()
    val sessionEvent: LiveData<Session> = _sessionEvent

    private val user: User? get() = pref.user
    private val prefObservable = Pref.Observable()

    init {
        observable.addSessionObserver(this)
        prefObservable.subscribe(getApplication(), this, R.string.key_current_user)
    }

    override fun onCleared() {
        super.onCleared()
        observable.removeSessionObserver(this)
        prefObservable.unsubscribe(getApplication())
    }

    override fun onPreferenceChanged(key: Int, contextChanged: Boolean) {
        if (key == R.string.key_current_user) {
            displayPhoto = null
        }
    }

    override fun onSessionChanged(session: Session) {
        if (session.activeUser != pref.user) return
        _sessionEvent.postValue(session)
    }

    override fun onUserLoggedOut() {
        viewModelScope.launch {
            when (val user = user) {
                is User -> nairaland.sources.accounts.logout(user)
            }
            _userLoggedOutEvent.value = Event(Unit)
        }
    }

    fun loadAccounts() {
        viewModelScope.launch {
            _accountListEvent.value =
                Event(nairaland.sources.accounts.loadAccountUsers() + arrayListOf(AnonymousAccount))
        }
    }

    fun loadDisplayPhotoOrAvatar() {
        if (pref.isLoggedIn) {
            loadDisplayPhotoAuth()
        } else {
            loadAvatar()
        }
    }


    private fun loadDisplayPhotoAuth() {
        val user = user ?: return
        viewModelScope.launch {
            _displayPhotoEvent.value = Event(Resource.Loading(displayPhoto))

            val resource = when (val result = nairaland.sources.accounts.displayPhoto(user)) {
                is Ok -> {
                    try {
                        displayPhoto = if (result.value is NoDisplayPhoto) { // user has no url?
                            DisplayPhotoBitmap(
                                bitmap = AvatarGenerator.getForUser(
                                    getApplication(),
                                    pref.userName ?: "throwaway"
                                )
                            )
                        } else {
                            result.value
                        }
                        Resource.Success(displayPhoto!!)
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance()
                            .log("Error AvatarGenerator for '${pref.userName ?: "throwaway"}': $e")
                        Resource.Error(cause = ErrorResponse.Unknown("Unable to generate avatar for user"))
                    }
                }
                is Err -> {
                    Resource.Error(cause = result.error, content = displayPhoto)
                }
            }
            _displayPhotoEvent.value = Event(resource)
        }
    }

    private fun loadAvatar() {
        viewModelScope.launch {
            val name = pref.userName ?: "throwaway"

            try {
                val bitmap = AvatarGenerator.getForUser(
                    getApplication(),
                    name
                )
                _displayPhotoEvent.value =
                    Event(Resource.Success(DisplayPhotoBitmap(bitmap = bitmap)))
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().log("Error AvatarGenerator for '$name': $e")
            }
        }
    }

    fun setUser(username: String) {
        pref.apply {
            userName = username.trim()
            isLoggedIn = true
        }
        viewModelScope.launch {
            nairaland.reset()
        }
    }

    fun setUser(userAccount: UserAccount) {
        pref.changeAccount(userAccount)
        viewModelScope.launch {
            nairaland.reset()
        }
    }

    fun removeUser(account: RealUserAccount, isCurrentUser: Boolean) {
        viewModelScope.launch {
            nairaland.sources.accounts.removeUser(account)
            nairaland.reset()
            _removeUserAccountEvent.value = Event(account to isCurrentUser)
        }
    }


    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginEvent.value = Event(Resource.Loading(null))

            val result = nairaland.auth.login(username, password)
            _loginEvent.value = Event(result.toResource(null))
        }
    }

}

