package com.amebo.amebo.common

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.amebo.amebo.screens.newpost.editor.EditAction
import com.amebo.amebo.screens.newpost.editor.EditActionSetting
import com.amebo.core.domain.*

interface Pref {

    var showFollowedBoardHint: Boolean

    var showUnFollowTopicHint: Boolean

    var canAskForReview: Boolean

    var timeFirstLaunch: Long

    var numOfTimesLaunchedApp: Long

    var followedBoardSort: Sort

    var userName: String?

    val user: User?

    var isLoggedIn: Boolean

    val isLoggedOut get() = !isLoggedIn


    var isFirstLaunch: Boolean

    var editActions: List<EditActionSetting>

    var version: Int

    val isMigrationNeeded: Boolean

    val useDeviceEmojis: Boolean

    val markReadTopics: Boolean

    val homePageTopicList: TopicList

    var acceptedNairalandRules: Boolean

    val confirmExit: Boolean

    val crashlyticsEnabled: Boolean

    operator fun <T : Any> set(key: String, value: T)

    operator fun <T : Any> get(key: String, defaultValue: T): T

    fun userDisplayName(context: Context): String

    fun defaultSortOf(topicList: TopicList): Sort?

    /**
     * @return true if user should refresh list of followed boards
     */
    fun isFollowedBoardsSyncDue(): Boolean

    fun setFollowedBoardsSyncTime()

    fun clearFollowedBoardsSyncTime()

    fun logOut() {
        userName = null
        isLoggedIn = false
    }

    fun changeAccount(userAccount: UserAccount) {
        when (userAccount) {
            is RealUserAccount -> {
                userName = userAccount.user.slug
                isLoggedIn = userAccount.isLoggedIn
            }
            is AnonymousAccount -> {
                userName = null
                isLoggedIn = false
            }
        }

    }

    fun allVisibleEditActions(): List<EditAction>

    fun isCurrentAccount(userAccount: UserAccount) = when (userAccount) {
        is AnonymousAccount -> userName == null
        is RealUserAccount -> userAccount.user.slug.equals(userName, true)
    }

    fun isCurrentAccount(user: User) = user.slug.equals(userName, true)

    fun notifyTopicHistoryCleared()

    fun migrate()
    fun initialize()

    class Observable {
        private val subscribedKeys: MutableMap<String, Int?> = HashMap()
        private var observer: Observer? = null
        private val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (subscribedKeys.containsKey(key)) {
                    notifyChanged(subscribedKeys[key]!!)
                }
            }

        fun subscribe(
            context: Context,
            observer: Observer,
            vararg preferenceKeys: Int
        ) {
            this.observer = observer
            setSubscription(context, preferenceKeys)
            PreferenceManager.getDefaultSharedPreferences(context)
                .registerOnSharedPreferenceChangeListener(listener)
        }

        fun unsubscribe(context: Context?) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .unregisterOnSharedPreferenceChangeListener(listener)
        }

        private fun setSubscription(
            context: Context,
            preferenceKeys: IntArray
        ) {
            subscribedKeys.clear()
            for (preferenceKey in preferenceKeys) {
                subscribedKeys[context.getString(preferenceKey)] = preferenceKey
            }
        }

        private fun notifyChanged(key: Int) {
            observer?.onPreferenceChanged(key, true)
        }


    }

    interface Observer {
        fun onPreferenceChanged(@StringRes key: Int, contextChanged: Boolean)
    }

    companion object {
        const val CURRENT_THEME = "current_theme"
        const val DARK_MODE = "dark_mode"
    }
}


