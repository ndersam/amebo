package com.amebo.amebo.screens.explore

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.common.Resource
import com.amebo.amebo.databinding.ExploreScreenBinding
import com.amebo.amebo.screens.explore.adapters.TopicListAdapter
import com.amebo.amebo.suite.TestPref
import com.amebo.amebo.suite.isGone
import com.amebo.amebo.suite.launchTestFragmentInTestActivity
import com.amebo.amebo.suite.makeVisible
import com.amebo.core.domain.ErrorResponse
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

class ExploreViewTest {

    @RunWith(AndroidJUnit4::class)
    class LoggedIn {
        private lateinit var pref: TestPref
        private lateinit var listener: ExploreView.Listener
        private lateinit var view: ExploreView
        private lateinit var adapter: TopicListAdapter

        @Before
        fun before() {
            pref = TestPref().apply {
                isLoggedIn = true
            }
            listener = mock()
            adapter = mock()

            launchTestFragmentInTestActivity(R.layout.explore_screen) { _, view ->
                val binding = ExploreScreenBinding.bind(view)
                this.view = ExploreView(pref, binding, listener, adapter)
            }
        }

        @Test
        fun onInit_toolbarProgress_visible_and_btnSync_hidden() {
            onView(withId(R.id.btnSync)).check(matches(isDisplayed()))
            onView(withId(R.id.toolbarProgress)).check(matches(isGone()))
        }

        @Test
        fun onExploreDataSet_dataDisplayedCorrectly() {
            val data = ExploreData(
                TopicListData(
                    recent = emptyList(),
                    allBoards = listOf(mock(), mock()),
                    followed = listOf(mock(), mock())
                ),
                mutableListOf()
            )

            view.setExploreData(data)
            verify(adapter).setData(any())
        }

        @Test
        fun onFollowedBoardsLoading_viewUpdatedAppropriately() {
            view.onFetchedFollowedBoardsLoading(Resource.Loading(null))

            onView(withId(R.id.toolbarProgress)).check(matches(isDisplayed()))
            onView(withId(R.id.btnSync)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        }

        @Test
        fun onFollowedBoardsSuccess_viewUpdatedAppropriately() {
            view.onFetchedFollowedBoardsSuccess(Resource.Success(listOf()))

            onView(withId(R.id.toolbarProgress)).check(matches(withEffectiveVisibility(Visibility.GONE)))
            onView(withId(R.id.btnSync)).check(matches(isDisplayed()))
            verify(adapter).setFollowedBoards(any())
        }

        @Test
        fun onFollowedBoardsError_viewUpdatedAppropriately() {
            view.onFetchedFollowedBoardsError(Resource.Error(ErrorResponse.Network, listOf()))

            onView(withId(R.id.toolbarProgress)).check(matches(withEffectiveVisibility(Visibility.GONE)))
            onView(withId(R.id.btnSync)).check(matches(isDisplayed()))
            verify(adapter).setFollowedBoards(any())
        }

        @Test
        fun onClickSync_fetchFollowedBoards() {
            view.onFetchedFollowedBoardsSuccess(Resource.Success(listOf()))
            onView(withId(R.id.btnSync)).perform(makeVisible(), click())
            verify(listener).fetchFollowedBoards()
        }

        @Test
        fun onClickSearchBox_routeToSearch() {
            onView(withId(R.id.searchBox)).perform(click())
            verify(listener).showSearch(any())
        }
    }

    @RunWith(AndroidJUnit4::class)
    class LoggedOut {

        private lateinit var pref: TestPref
        private lateinit var listener: ExploreView.Listener
        private lateinit var view: ExploreView
        private lateinit var adapter: TopicListAdapter

        @Before
        fun before() {
            pref = TestPref().apply {
                isLoggedIn = false
            }
            listener = mock()
            adapter = mock()

            launchTestFragmentInTestActivity(R.layout.explore_screen) { _, view ->
                val binding = ExploreScreenBinding.bind(view)
                this.view = ExploreView(pref, binding, listener, adapter)
            }
        }

        @Test
        fun onInit_toolbarProgress_and_btnSync_hidden() {
            onView(withId(R.id.btnSync)).check(matches(isGone()))
            onView(withId(R.id.toolbarProgress)).check(matches(isGone()))
        }
    }
}