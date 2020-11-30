package com.amebo.amebo.screens.topiclist

import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.application.TestFragmentActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TopicListScreenTest {

//    @get:Rule
//    val activityRule = ActivityTestRule(TestFragmentActivity::class.java)
    val scenario = launchActivity<TestFragmentActivity>()

    @Before
    fun before(){

    }

    @Test
    fun test_on_create_board_loads() {
//        activityRule.activity.replaceFragment(TopicListScreen.newInstance(Featured))
    }
}