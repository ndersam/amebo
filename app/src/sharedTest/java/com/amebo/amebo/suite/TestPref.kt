package com.amebo.amebo.suite

import android.content.Context
import com.amebo.amebo.common.Pref
import com.amebo.amebo.screens.newpost.editor.EditAction
import com.amebo.amebo.screens.newpost.editor.EditActionSetting
import com.amebo.core.domain.*

class TestPref : Pref {
    override var isLoggedIn: Boolean = false
    override var showFollowedBoardHint: Boolean = false
    override var showUnFollowTopicHint: Boolean = false
    override var canAskForReview: Boolean = false
    override var timeFirstLaunch: Long = 0
    override var numOfTimesLaunchedApp: Long = 0
    override var followedBoardSort: Sort = TopicListSorts.CREATION
    override var userName: String? = null
    override var user: User? = null

    override var isFirstLaunch: Boolean = false
    override var editActions: List<EditActionSetting> = emptyList()
    override var version: Int = -1
    override val isMigrationNeeded: Boolean = false
    override val useDeviceEmojis: Boolean = false
    override val homePageTopicList: TopicList = Featured
    override var acceptedNairalandRules: Boolean = false
    override var confirmExit: Boolean = false
    override var crashlyticsEnabled: Boolean = false
    override var markReadTopics: Boolean = false

    override fun <T : Any> set(key: String, value: T) {

    }

    override operator fun <T : Any> get(key: String, defaultValue: T): T {
        return defaultValue
    }

    override fun userDisplayName(context: Context): String {
        return "Anonymous"
    }

    override fun defaultSortOf(topicList: TopicList): Sort? = defaultSort

    override fun allVisibleEditActions(): List<EditAction> = emptyList()

    override fun notifyTopicHistoryCleared() {

    }

    override fun migrate() {

    }

    override fun initialize() {
    }

    override fun isFollowedBoardsSyncDue(): Boolean {
        return false
    }

    override fun setFollowedBoardsSyncTime() {

    }

    override fun clearFollowedBoardsSyncTime() {

    }

    var defaultSort: Sort? = null
}