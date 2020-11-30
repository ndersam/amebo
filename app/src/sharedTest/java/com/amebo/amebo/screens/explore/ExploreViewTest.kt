package com.amebo.amebo.screens.explore

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.databinding.ExploreScreenBinding
import com.amebo.amebo.screens.explore.adapters.TopicListAdapter
import com.amebo.amebo.suite.RecyclerViewMatcher
import com.amebo.amebo.suite.injectIntoTestApp
import com.amebo.amebo.suite.launchTestFragmentInTestActivity
import com.amebo.core.domain.ErrorResponse
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
//@LooperMode(LooperMode.Mode.PAUSED)
class ExploreViewTest {
    lateinit var pref: Pref
    lateinit var listener: ExploreView.Listener
    lateinit var view: ExploreView
    lateinit var adapter: TopicListAdapter

    @Before
    fun before() {
        pref = mock()
        listener = mock()
        adapter = mock()

        injectIntoTestApp()
        launchTestFragmentInTestActivity(R.layout.explore_screen) { _, view ->
            val binding = ExploreScreenBinding.bind(view)
            this.view = ExploreView(pref, binding, listener, adapter)
        }
    }

    @Test
    fun onExploreDataSet_dataDisplayedCorrectly() {
//        shadowOf(Looper.getMainLooper()).idle()

        val data = ExploreData(
            TopicListData(
                recent = listOf(mock(), mock(), mock()),
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

//        shadowOf(Looper.getMainLooper()).idle()


        onView(
            RecyclerViewMatcher(R.id.rvBoards)
                .atPositionOnView(0, R.id.toolbarProgress)
        )
            .check(matches(isDisplayed()))
//        onView(withId(R.id.recyclerView)).check(matches(atPosition(0, hasDescendant(withId(R.id.toolbarProgress)))))
//        onView(withId(R.id.toolbarProgress)).check(matches(isDisplayed()))
//        onView(withId(R.id.btnSync)).check(matches(withEffectiveVisibility(Visibility.GONE)))
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
    fun onClickSync_fetchFollowedBoards(){
        onView(withId(R.id.btnSync)).perform(click())

        verify(listener).fetchFollowedBoards()
    }

    @Test
    fun onClickSearchBox_routeToSearch(){
        onView(withId(R.id.searchBox)).perform(click())
        verify(listener).showSearch(any())
    }
}