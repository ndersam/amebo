package com.amebo.core.domain

import com.amebo.core.apis.util.SoupConverterFactory
import com.amebo.core.data.cookies.PersistentCookieJar
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*

data class Session(
    var isLoggedIn: Boolean = false,
    var activeUser: User? = null,
    var sharedWithMe: Int = 0,
    var followedTopics: Int = 0,
    var followedBoards: Int = 0,
    var likesAndShares: Int = 0,
    var mentions: Int = 0,
    var following: Int = 0,
    var mailNotificationForm: DismissMailNotificationForm? = null
) {
    val count: Int get() = sharedWithMe + followedTopics + followedBoards + likesAndShares + mentions + following
}

/**
 * Used to monitor current [Session] data parsed from [SoupConverterFactory] and also
 * session value stored in cookies.
 *
 * @see [SoupConverterFactory]
 * @see [PersistentCookieJar]
 */
class NairalandSessionObservable : Observable() {
    private val observers = mutableSetOf<WeakReference<Observer>>()

    internal var value: Session? = null
        set(value) {
            field = value
            if (value != null) {
                notifySessionChanged(value)
            }
        }

    internal var cookieSession: String? = null
        set(value) {
            field = value
            Timber.d("Session: $value")
            if (value.isNullOrBlank() || value == "\"\"") {
                notifyUserLoggedOut()
            }
        }

    fun addSessionObserver(observer: Observer) {
        removeSessionObserver(observer)
        val value = this.value
        if (value != null) {
            observer.onSessionChanged(value)
        }
        observers.add(WeakReference(observer))
    }

    fun removeSessionObserver(observer: Observer) {
        observers.removeAll { it.get() == null || it.get() == observer }
    }

    private fun notifySessionChanged(session: Session) {
        observers.forEach {
            it.get()?.onSessionChanged(session)
        }
    }

    private fun notifyUserLoggedOut() {
        Timber.d("Notifying `UserLoggedOut`")
        observers.forEach {
            it.get()?.onUserLoggedOut()
        }
    }

    interface Observer {
        fun onSessionChanged(session: Session)
        fun onUserLoggedOut()
    }
}