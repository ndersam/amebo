package com.amebo.amebo.screens.leftdrawer

import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.databinding.ActivityMainBinding
import com.amebo.amebo.suite.TestPref
import com.amebo.amebo.suite.launchTestFragmentInTestActivity
import com.amebo.amebo.suite.waitFor
import com.amebo.core.domain.User
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

class DrawerLayoutViewTest {

    @RunWith(AndroidJUnit4::class)
    class LoggedIn {
        private lateinit var view: DrawerLayoutView
        private lateinit var pref: TestPref

        @Before
        fun before() {
            pref = TestPref().apply {
                userName = "random"
                user = User("random")
                isLoggedIn = true
            }
            launchTestFragmentInTestActivity(R.layout.activity_main) { fragment, view ->
                val binding = ActivityMainBinding.bind(view)
                this.view = DrawerLayoutView(
                    username = pref.userName!!,
                    isLoggedIn = pref.isLoggedIn,
                    binding = binding.leftDrawer,
                )
                fragment.childFragmentManager.beginTransaction()
                    .replace(R.id.hostFragment, ToolbarFragment())
                    .commit()
            }
            onView(isRoot()).perform(waitFor(500))
        }

        @Test
        fun onCreate_DrawerLayoutSetupCorrectly() {
            onView(withId(R.id.drawerLayout)).perform(DrawerActions.open())

            onView(withText(R.string.topics)).check(matches(isDisplayed()))
            onView(withText(R.string.recent_posts)).check(matches(isDisplayed()))
            onView(withText(R.string.go_to_profile)).check(matches(isDisplayed()))
            onView(withText(R.string.profile)).check(matches(isDisplayed()))
            onView(withText(R.string.inbox)).check(matches(isDisplayed()))
            onView(withText(R.string.mentions)).check(matches(isDisplayed()))
            onView(withText(R.string.likes_and_shares)).check(matches(isDisplayed()))
            onView(withText(R.string.shared_with_me)).check(matches(isDisplayed()))
            onView(withText(R.string.following)).check(matches(isDisplayed()))
            onView(withText(R.string.my_likes)).check(matches(isDisplayed()))
            onView(withText(R.string.my_shared_posts)).check(matches(isDisplayed()))
            onView(withText(R.string.followers)).check(matches(isDisplayed()))
            onView(withText(R.string.settings)).check(matches(isDisplayed()))
        }


    }

    @RunWith(AndroidJUnit4::class)
    class LoggedOut {

    }

    class ToolbarFragment : Fragment(R.layout.topic_list_screen)
}