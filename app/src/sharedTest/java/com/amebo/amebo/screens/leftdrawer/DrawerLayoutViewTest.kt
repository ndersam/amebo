package com.amebo.amebo.screens.leftdrawer

import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.common.routing.TabItem
import com.amebo.amebo.databinding.ActivityMainBinding
import com.amebo.amebo.suite.TestFragment
import com.amebo.amebo.suite.launchTestFragment
import com.amebo.amebo.suite.withNavigationIcon
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DrawerLayoutViewTest {
    private lateinit var scenario: FragmentScenario<TestFragment>
    private lateinit var view: DrawerLayoutView

    @Test
    fun onCreate_DrawerLayoutSetupCorrectly() {
        initialize()
        onView(withId(R.id.toolbar)).check(matches(withNavigationIcon(R.drawable.ic_menu_black_24dp)))
    }

    private fun initialize(
        currentItem: TabItem = TabItem.Topics,
        isLoggedIn: Boolean = false,
        username: String = "Anonymous"
    ) {
        scenario = launchTestFragment(R.layout.activity_main) { fragment, view ->
            val binding = ActivityMainBinding.bind(view)
//            this.binding = binding.leftDrawer
//            this.view = DrawerLayoutView(
//                currentItem = currentItem,
//                username = username,
//                isLoggedIn = isLoggedIn,
//                binding = binding.leftDrawer,
//                fragment = fragment
//            )
        }
    }
}