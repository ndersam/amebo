package com.amebo.amebo.common

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.common.Pref.Companion.CURRENT_THEME
import com.amebo.amebo.common.Pref.Companion.DARK_MODE
import com.amebo.amebo.common.PrefImpl.PrefMigration.ALWAYS_LOAD_LAST_PAGE
import com.amebo.amebo.common.PrefImpl.PrefMigration.APP_TEXT_SIZE
import com.amebo.amebo.common.PrefImpl.PrefMigration.CURRENT_USER
import com.amebo.amebo.common.PrefImpl.PrefMigration.CUSTOM_TABS
import com.amebo.amebo.common.PrefImpl.PrefMigration.DEFAULT_HOME_BOARD
import com.amebo.amebo.common.PrefImpl.PrefMigration.DEFAULT_HOME_BOARD_LOGGED_OUT
import com.amebo.amebo.common.PrefImpl.PrefMigration.DEFAULT_SORT_OPTION
import com.amebo.amebo.common.PrefImpl.PrefMigration.DEFAULT_TIMELINE_ITEM
import com.amebo.amebo.common.PrefImpl.PrefMigration.EDIT_ACTIONS
import com.amebo.amebo.common.PrefImpl.PrefMigration.IS_FIRST_LAUNCH
import com.amebo.amebo.common.PrefImpl.PrefMigration.IS_SYNC_PROBLEM
import com.amebo.amebo.common.PrefImpl.PrefMigration.LOGIN_STATUS
import com.amebo.amebo.common.PrefImpl.PrefMigration.PAGE_COUNT_THRESHOLD
import com.amebo.amebo.common.PrefImpl.PrefMigration.actionsToString
import com.amebo.core.domain.Board
import com.amebo.core.domain.FollowedTopics
import com.amebo.core.domain.NewTopics
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import com.amebo.core.migration.old.EditAction as OldEditAction

@RunWith(AndroidJUnit4::class)
class PrefImplTest {


    @Test
    fun migration_migrates_all_preferences_appropriately() {
        // setUp
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = prefs.edit()


        edit.putString(DEFAULT_HOME_BOARD_LOGGED_OUT, "2") // New
            .putString(DEFAULT_HOME_BOARD, "4")   // Followed Topics
            .putString(DEFAULT_SORT_OPTION, "New")
            .putString(DEFAULT_TIMELINE_ITEM, "0")    // Timeline
            .putBoolean(CUSTOM_TABS, true)
            .putBoolean(ALWAYS_LOAD_LAST_PAGE, true)
            .putInt(PAGE_COUNT_THRESHOLD, 15)
            .putString(APP_TEXT_SIZE, "2")
            .putBoolean(IS_SYNC_PROBLEM, true)
            .putString(CURRENT_THEME, "Dark")
            .putBoolean(LOGIN_STATUS, true)
            .putString(CURRENT_USER, "nairalander")
            .putBoolean(IS_FIRST_LAUNCH, false)
            .putString(EDIT_ACTIONS, actionsToString(OldEditAction.defaultActions(true)))
            .commit()

        // action
        val impl = PrefImpl(context)
        impl.migrate()

        // verify
        assertThat(impl.defaultSortOf(Board("", ""))!!.name).isEqualTo("New")
        assertThat(impl.editActions.map { it.editAction.identifier }).containsAtLeastElementsIn(
            OldEditAction.defaultActions(true).map { it.type.ordinal })
        assertThat(prefs.getInt(CURRENT_THEME, -1)).isEqualTo(0)
        assertThat(prefs.getBoolean(LOGIN_STATUS, false)).isTrue()
        assertThat(prefs.getString(CURRENT_USER, null)).isEqualTo("nairalander")
        assertThat(prefs.getBoolean(IS_FIRST_LAUNCH, true)).isFalse()
        assertThat(prefs.getBoolean(DARK_MODE, false)).isTrue()

        // assert deletions
        assertThat(prefs.contains(DEFAULT_TIMELINE_ITEM)).isFalse()
        assertThat(prefs.contains(DEFAULT_HOME_BOARD_LOGGED_OUT)).isFalse()
        assertThat(prefs.contains(DEFAULT_HOME_BOARD)).isFalse()
        assertThat(prefs.contains(DEFAULT_SORT_OPTION)).isFalse()
        assertThat(prefs.contains(CUSTOM_TABS)).isFalse()
        assertThat(prefs.contains(ALWAYS_LOAD_LAST_PAGE)).isFalse()
        assertThat(prefs.contains(PAGE_COUNT_THRESHOLD)).isFalse()
        assertThat(prefs.contains(APP_TEXT_SIZE)).isFalse()
        assertThat(prefs.contains(IS_SYNC_PROBLEM)).isFalse()
    }


    @Test
    fun migration_migrates_topic_list_preferences_appropriately() {
        // setUp
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = prefs.edit()


        edit.putString(DEFAULT_HOME_BOARD_LOGGED_OUT, "2") // New
            .putString(DEFAULT_HOME_BOARD, "4")   // Followed Topics
            .commit()

        // action
        val impl = PrefImpl(context)
        impl.migrate()

        // verify
        impl.isLoggedIn = false
        assertThat(impl.homePageTopicList).isEqualTo(NewTopics)
        impl.isLoggedIn = true
        assertThat(impl.homePageTopicList).isEqualTo(FollowedTopics)
    }
}