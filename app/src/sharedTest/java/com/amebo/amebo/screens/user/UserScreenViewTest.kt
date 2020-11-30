package com.amebo.amebo.screens.user

import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.application.TestFragmentActivity
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.widgets.StateLayout
import com.amebo.amebo.databinding.UserScreenBinding
import com.amebo.amebo.suite.launchTestFragmentInTestActivity
import com.amebo.amebo.suite.withState
import com.amebo.core.domain.User
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserScreenViewTest {

    lateinit var view: UserScreenView
    lateinit var scenario: ActivityScenario<TestFragmentActivity>
    lateinit var user: User
    lateinit var listener: UserScreenView.Listener
    lateinit var pref: Pref

    @Before
    fun before() {
        user = User("random")
        pref = mock()
        listener = mock()
    }

    @After
    fun after() {

    }

    @Test
    fun onCreateView_viewInitializedCorrectly() {
        val isLoggedIn = arrayOf(true, true, false)
        val isCurrentUser = arrayOf(true, false, false)

        fun isVisible(bool: Boolean) =
            if (bool) isDisplayed() else withEffectiveVisibility(Visibility.GONE)

        for (i in isLoggedIn.indices) {
            whenever(pref.isLoggedIn).thenReturn(isLoggedIn[i])
            whenever(pref.isCurrentAccount(user)).thenReturn(isCurrentUser[i])
            launchFragment()

            val isNotCurrentUser = !isCurrentUser[i]

            onView(withId(R.id.userName)).check(matches(withText(user.name)))
            // onView(withId(R.id.collapsing_toolbar)).check(matches(withCollapsingToolbarTitle(user.name)))
            onView(withId(R.id.btn_follow)).check(matches(isVisible(isNotCurrentUser)))
            onView(withId(R.id.btn_send_mail)).check(matches(isVisible(isNotCurrentUser)))
            onView(withId(R.id.stateLayout)).check(matches(withState(StateLayout.State.Progress)))
        }
    }

    private fun launchFragment() {
        scenario = launchTestFragmentInTestActivity(R.layout.user_screen) { frag, view ->
            val binding = UserScreenBinding.bind(view)
            this.view = UserScreenView(pref, binding, user, listener, frag.lifecycleScope)
        }
    }
}
