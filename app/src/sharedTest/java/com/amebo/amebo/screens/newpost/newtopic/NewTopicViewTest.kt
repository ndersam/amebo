package com.amebo.amebo.screens.newpost.newtopic

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.databinding.NewTopicScreenBinding
import com.amebo.amebo.common.Pref
import com.amebo.amebo.suite.*
import com.amebo.core.domain.Board
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewTopicViewTest {

    lateinit var view: NewTopicView
    lateinit var pref: Pref
    var board: Board? = null
    lateinit var listener: NewTopicView.Listener

    @Before
    fun before() {
        pref = mock()
        whenever(pref.allVisibleEditActions()).thenReturn(emptyList())
        listener = mock()
        launchTestFragmentInTestActivity(R.layout.new_topic_screen) { fragment, view ->
            val binding = NewTopicScreenBinding.bind(view)
            this.view = NewTopicView(fragment, pref, binding, board, listener)
        }
    }


    @Test
    fun stateLayoutSetupCorrectly() {
        onView(withId(R.id.stateLayout)).perform(stateLayoutAction { it.content() })
        onView(withId(R.id.content)).check(matches(isDisplayed()))
        onViews(withId(R.id.progress), withId(R.id.ouchView)).check(matches(isGone()))

        onView(withId(R.id.stateLayout)).perform(stateLayoutAction { it.loading() })
        onView(withId(R.id.progress)).check(matches(isDisplayed()))
        onViews(withId(R.id.content), withId(R.id.ouchView)).check(matches(isGone()))

        onView(withId(R.id.stateLayout)).perform(stateLayoutAction { it.failure() })
        onView(withId(R.id.ouchView)).check(matches(isDisplayed()))
        onViews(withId(R.id.content), withId(R.id.progress)).check(matches(isGone()))
    }
}