package com.amebo.amebo.common

import android.content.Context
import androidx.preference.PreferenceManager
import com.amebo.amebo.BuildConfig
import com.amebo.amebo.R
import com.amebo.amebo.screens.newpost.editor.EditAction
import com.amebo.amebo.screens.newpost.editor.EditActionSetting
import com.amebo.core.domain.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.TimeUnit
import com.amebo.core.migration.old.EditAction as OldEditAction

class PrefImpl(context: Context) : Pref {
    private val context = context.applicationContext
    private val prefs = PreferenceManager.getDefaultSharedPreferences(this.context)

    override var showFollowedBoardHint: Boolean
        get() = prefs.getBoolean("show_followed_board_hint", true)
        set(value) {
            prefs.edit().putBoolean("show_followed_board_hint", value).apply()
        }

    override var canAskForReview: Boolean
        get() = prefs.getBoolean("can_ask_for_review", true)
        set(value) {
            prefs.edit().putBoolean("can_ask_for_review", value).apply()
        }

    override var numOfTimesLaunchedApp: Long
        get() = prefs.getLong("app_launch_count", 0)
        set(value) {
            prefs.edit().putLong("app_launch_count", value).apply()
        }

    override var timeFirstLaunch: Long
        get() = prefs.getLong("time_first_launch", System.currentTimeMillis())
        set(value) {
            prefs.edit().putLong("time_first_launch", value).apply()
        }

    override var followedBoardSort: Sort
        get() = when (val value =
            prefs.getString("followed_board_sort", TopicListSorts.CREATION.value)!!) {
            TopicListSorts.UPDATE.value -> TopicListSorts.UPDATE
            TopicListSorts.CREATION.value -> TopicListSorts.CREATION
            else -> throw IllegalArgumentException("Illegal followed topic list sort $value")
        }
        set(value) {
            prefs.edit().putString("followed_board_sort", value.value).apply()
        }

    override var userName: String?
        get() {
            return prefs.getString(context.getString(R.string.key_current_user), null)
        }
        set(value) {
            prefs.edit().putString(context.getString(R.string.key_current_user), value).apply()
        }


    override val user: User?
        get() {
            val userString = userName ?: return null
            return User(userString)
        }

    override var isLoggedIn: Boolean
        get() {
            return prefs.getBoolean("login_status", false)
        }
        set(value) {
            prefs.edit().putBoolean("login_status", value).apply()
        }


    override var isFirstLaunch: Boolean
        get() {
            return prefs.getBoolean("initialized_first_time", true)
        }
        set(value) {
            prefs.edit().putBoolean("initialized_first_time", value).apply()
        }

    override var version: Int
        get() = prefs.getInt("app_version", -1)
        set(value) {
            require(value == BuildConfig.VERSION_CODE)
            prefs.edit().putInt("app_version", value).apply()
        }


    override val isMigrationNeeded: Boolean get() = !isFirstLaunch && version == -1


    override val homePageTopicList: TopicList
        get() {
            val value: Int = if (isLoggedIn) {
                get(context.getString(R.string.key_topiclist), 0)
            } else {
                get(context.getString(R.string.key_topiclist_anonymous), 0)
            }
            return when (value) {
                0 -> Featured
                1 -> Trending
                2 -> NewTopics
                3 -> FollowedBoards
                4 -> FollowedTopics
                else -> throw IllegalArgumentException()
            }
        }

    override var acceptedNairalandRules: Boolean
        get() = prefs.getBoolean("acceptedNairalandRules", false)
        set(value) {
            prefs.edit().putBoolean("acceptedNairalandRules", value).apply()
        }

    override val confirmExit: Boolean
        get() = get(context.getString(R.string.key_confirm_exit), true)

    private val defaultBoardSort: Sort
        get() {
            return when (get(context.getString(R.string.key_board_sort), 0)) {
                0 -> TopicListSorts.UPDATED
                1 -> TopicListSorts.NEW
                2 -> TopicListSorts.POSTS
                3 -> TopicListSorts.VIEWS
                else -> throw IllegalArgumentException()
            }
        }

    override val useDeviceEmojis: Boolean
        get() {
            val key = context.getString(R.string.key_use_device_emojis)
            return prefs.getBoolean(key, false)
        }

    override val markReadTopics: Boolean
        get() {
            val key = context.getString(R.string.key_mark_topics_as_read)
            return prefs.getBoolean(key, true)
        }

    override val crashlyticsEnabled: Boolean
        get() = prefs.getBoolean(context.getString(R.string.key_enable_crashlytics), true)

    override var editActions: List<EditActionSetting>
        get() {
            val key = context.getString(R.string.key_edit_actions)
            return when (val set = prefs.getString(key, null)) {
                is String -> {
                    set.split(",").map {
                        val data = it.split("|")
                        val identifier = data[0].trim().toInt()
                        val isVisible = data[1].trim().toBoolean()
                        val editAction = EditAction.getAction(identifier)
                        EditActionSetting(editAction, isVisible)
                    }.toList()
                }
                else -> EditAction.defaultList.map { EditActionSetting(it, isVisible = true) }
            }
        }
        set(value) {
            // e.g. 0|true,1|false,2|true, ...
            val key = context.getString(R.string.key_edit_actions)
            val data =
                value.joinToString(separator = ",") { it.editAction.identifier.toString() + "|" + it.isVisible.toString() }
            prefs.edit().putString(key, data).apply()
        }


    override fun allVisibleEditActions(): List<EditAction> {
        val visibleActions = editActions.filter { it.isVisible }.map { it.editAction }
        return EditAction.precedingList + visibleActions + EditAction.endingList
    }

    @Suppress("UNCHECKED_CAST")
    override operator fun <T : Any> set(key: String, value: T) {
        val editor = prefs.edit()
        when {
            value is Int -> editor.putInt(key, value)
            value is String -> editor.putString(key, value)
            value is Float -> editor.putFloat(key, value)
            value is Long -> editor.putLong(key, value)
            value is Boolean -> editor.putBoolean(key, value)
            (value as? Set<String>) != null -> editor.putStringSet(key, value)
            else -> throw IllegalArgumentException("Unsupported value type ${value.javaClass}")
        }
        editor.apply()
    }

    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    override operator fun <T : Any> get(key: String, defaultValue: T): T {
        return when {
            defaultValue is Int -> prefs.getInt(key, defaultValue)
            defaultValue is String -> prefs.getString(key, defaultValue)
            defaultValue is Float -> prefs.getFloat(key, defaultValue)
            defaultValue is Long -> prefs.getLong(key, defaultValue)
            defaultValue is Boolean -> prefs.getBoolean(key, defaultValue)
            (defaultValue as? Set<String>) != null -> prefs.getStringSet(key, defaultValue) as T
            else -> throw IllegalArgumentException("Unsupported value type ${defaultValue.javaClass}")
        } as T
    }

    override fun userDisplayName(context: Context): String = when (userName) {
        is String -> userName!!
        else -> context.getString(R.string.anonymous)
    }

    override fun defaultSortOf(topicList: TopicList) = when (topicList) {
        is Board -> defaultBoardSort
        is FollowedBoards -> followedBoardSort
        else -> null
    }

    override fun isFollowedBoardsSyncDue(): Boolean {
        val username = userName ?: return false
        val lastTime: Long = this[followedBoardsKey(username), -1]
        return (System.currentTimeMillis() - lastTime) >= TimeUnit.DAYS.toMillis(1)
    }

    override fun setFollowedBoardsSyncTime() {
        val username = userName ?: return
        this[followedBoardsKey(username)] = System.currentTimeMillis()
    }

    override fun clearFollowedBoardsSyncTime() {
        val username = userName ?: return
        this[followedBoardsKey(username)] = -1L
    }

    override fun notifyTopicHistoryCleared() {
        val key = context.getString(R.string.key_clear_read_topics)
        val readTopicsEvent = prefs.getBoolean(key, false)
        prefs.edit().putBoolean(key, readTopicsEvent.not()).apply()
    }

    override fun initialize() {
        prefs.edit().putInt(Pref.CURRENT_THEME, 0)
            .putBoolean(Pref.DARK_MODE, false)
            .apply()
    }

    override fun migrate() = PrefMigration.migrate(context, this)


    private fun followedBoardsKey(username: String) = "${username}_followedboards_last_sync_time"


    object PrefMigration {

        const val DEFAULT_HOME_BOARD = "default_home_board"
        const val DEFAULT_HOME_BOARD_LOGGED_OUT = "default_home_board_logged_out"
        const val DEFAULT_SORT_OPTION = "default_sort_option"
        const val DEFAULT_TIMELINE_ITEM = "default_timeline_item"
        const val CUSTOM_TABS = "custom_tabs"
        const val ALWAYS_LOAD_LAST_PAGE = "always_load_last_page"
        const val PAGE_COUNT_THRESHOLD = "page_count_threshold"
        const val APP_TEXT_SIZE = "app_text_size"

        const val IS_SYNC_PROBLEM = "is_sync_problem"
        const val EDIT_ACTIONS = "edit_actions"
        const val CURRENT_USER = "current_user"
        const val LOGIN_STATUS = "login_status"
        const val IS_FIRST_LAUNCH = "initialized_first_time"

        fun actionsToString(actions: List<OldEditAction>): String {
            val gson = Gson()
            val type = object : TypeToken<List<OldEditAction>>() {}.type
            return gson.toJson(actions, type)
        }

        private fun actionsFromString(string: String): List<OldEditAction> {
            val gson = Gson()
            val type = object : TypeToken<List<OldEditAction>>() {}.type
            return gson.fromJson(string, type)
        }

        fun migrate(context: Context, pref: Pref) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val edit = prefs.edit()

            run {
                val keyAnon = context.getString(R.string.key_topiclist_anonymous)
                val key = context.getString(R.string.key_topiclist)

                when (val homeBoard = pref[DEFAULT_HOME_BOARD, ""]) {
                    "" -> {
                        // No pref saved. Nothing to do
                    }
                    else -> {
                        edit.putInt(key, homeBoard.toInt())
                    }
                }

                when (val homeBoard = pref[DEFAULT_HOME_BOARD_LOGGED_OUT, ""]) {
                    "" -> {
                        // No pref saved. Nothing to do
                    }
                    else -> {
                        edit.putInt(keyAnon, homeBoard.toInt())
                    }
                }
            }

            run {
                val key = context.getString(R.string.key_board_sort)
                when (val sort = pref[DEFAULT_SORT_OPTION, ""]) {
                    "" -> {
                        // No pref saved. Nothing to do
                    }
                    else -> {
                        val idx = TopicListSorts.BoardSorts.indexOfFirst { it.name == sort }
                        if (idx != -1) {
                            edit.putInt(key, idx)
                        }
                    }
                }
            }

            run {
                // Previously was stored as String, now as Int
                edit.remove(Pref.CURRENT_THEME)
                edit.putInt(Pref.CURRENT_THEME, 0)
                val isDarkMode = pref[Pref.CURRENT_THEME, ""] == "Dark"
                if (isDarkMode) {
                    edit.putBoolean(Pref.DARK_MODE, true)
                }
            }

            run {
                when (val json = pref[EDIT_ACTIONS, ""]) {
                    "" -> {

                    }
                    else -> {
                        val actions = actionsFromString(json).map { it.type.ordinal }
                        pref.editActions = EditAction.defaultList.map {
                            EditActionSetting(it, isVisible = actions.contains(it.identifier))
                        }
                    }
                }
            }

            // Remove unused keys (if exist)
            run {
                arrayOf(
                    DEFAULT_HOME_BOARD,
                    DEFAULT_HOME_BOARD_LOGGED_OUT,
                    DEFAULT_SORT_OPTION,
                    DEFAULT_TIMELINE_ITEM,
                    CUSTOM_TABS,
                    ALWAYS_LOAD_LAST_PAGE,
                    PAGE_COUNT_THRESHOLD,
                    APP_TEXT_SIZE,
                    IS_SYNC_PROBLEM
                ).forEach(edit::remove)
            }


            edit.apply()
        }

//        fun migrate(context: Context, pref: Pref) {
//            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
//            val edit = prefs.edit()
//
//            val newPrefs = mutableMapOf<String, Any>()
//
//            run {
//                val keyAnon = context.getString(R.string.key_topiclist_anonymous)
//                val key = context.getString(R.string.key_topiclist)
//
//                when (val homeBoard = pref[DEFAULT_HOME_BOARD, ""]) {
//                    "" -> {
//                        // No pref saved. Nothing to do
//                    }
//                    else -> {
//                        newPrefs[key] = homeBoard.toInt()
//                    }
//                }
//
//                when (val homeBoard = pref[DEFAULT_HOME_BOARD_LOGGED_OUT, ""]) {
//                    "" -> {
//                        // No pref saved. Nothing to do
//                    }
//                    else -> {
//                        newPrefs[keyAnon] = homeBoard.toInt()
//                    }
//                }
//            }
//
//            run {
//                val key = context.getString(R.string.key_board_sort)
//                when (val sort = pref[DEFAULT_SORT_OPTION, ""]) {
//                    "" -> {
//                        // No pref saved. Nothing to do
//                    }
//                    else -> {
//                        val idx = TopicListSorts.BoardSorts.indexOfFirst { it.name == sort }
//                        if (idx != -1) {
//                            newPrefs[key] = idx
//                        }
//                    }
//                }
//            }
//
//            run {
//                // Previously was stored as String, now as Int
//                edit.remove(Pref.CURRENT_THEME)
//                newPrefs[Pref.CURRENT_THEME] = 0
//                val isDarkMode = pref[Pref.CURRENT_THEME, ""] == "Dark"
//                if (isDarkMode) {
//                    newPrefs[Pref.DARK_MODE] = true
//                }
//            }
//
//            run {
//                when (val json = pref[EDIT_ACTIONS, ""]) {
//                    "" -> {
//
//                    }
//                    else -> {
//                        val actions = actionsFromString(json).map { it.type.ordinal }
//                        val value = EditAction.defaultList.map {
//                            EditActionSetting(it, isVisible = actions.contains(it.identifier))
//                        }
//                        newPrefs[EDIT_ACTIONS] =
//                            value.joinToString(separator = ",") { it.editAction.identifier.toString() + "|" + it.isVisible.toString() }
//                    }
//                }
//            }
//
//
//            run {
//                when (val currentUser = pref.userName) {
//                    is String -> {
//                        newPrefs[CURRENT_USER] = currentUser
//                    }
//                }
//
//                newPrefs[LOGIN_STATUS] = pref.isLoggedIn
//                newPrefs[IS_FIRST_LAUNCH] = pref.isFirstLaunch
//            }
//
//            // Remove unused keys (if exist)
//            run {
//                edit.clear().apply()
//
//                newPrefs.forEach { entry ->
//                    val (k, v) = entry
//                    when (v) {
//                        is Int -> {
//                            edit.putInt(k, v)
//                        }
//                        is Long -> {
//                            edit.putLong(k, v)
//                        }
//                        is Boolean -> {
//                            edit.putBoolean(k, v)
//                        }
//                        is String -> {
//                            edit.putString(k, v)
//                        }
//                        is Set<*> -> {
//                            edit.putStringSet(k, v as? Set<String> ?: return@forEach)
//                        }
//                    }
//                }
//
//            }
//
//
//            edit.apply()
//        }
    }
}


