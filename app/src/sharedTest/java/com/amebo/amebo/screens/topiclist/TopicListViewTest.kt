package com.amebo.amebo.screens.topiclist

import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.application.TestFragmentActivity
import com.amebo.amebo.common.Resource
import com.amebo.amebo.data.TestData
import com.amebo.amebo.databinding.TopicListScreenBinding
import com.amebo.amebo.screens.topiclist.adapters.ItemAdapter
import com.amebo.amebo.screens.topiclist.main.TopicListView
import com.amebo.amebo.suite.*
import com.amebo.core.domain.*
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertEquals
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TopicListViewTest {

    private lateinit var scenario: ActivityScenario<TestFragmentActivity>
    private lateinit var fragment: TestFragment
    private lateinit var topicListView: TopicListView
    private lateinit var topicListViewListener: TopicListView.Listener
    private val recyclerViewMatcher get() = withIndexOfType(withId(R.id.recyclerView), 0)


    @Before
    fun before() {
        scenario = launchActivity()
        topicListViewListener = mock()
    }

    @Test
    fun onCreate_viewDisplaysCorrectly() {
        initializeFragment()
        onView(withId(R.id.toolbar)).check(matches(withToolbarTitle(R.string.featured)))
        onView(withId(R.id.toolbar)).check(matches(withToolbarSubtitle("")))
    }

    @Test
    fun viewReactsCorrectlyToLoadingEventWithNoExistingData() {
        initializeFragment()
        topicListView.onLoading(Resource.Loading(null))

        onView(recyclerViewMatcher).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.progress)).check(matches(isDisplayed()))
        onView(withId(R.id.swipeRefreshLayout)).check(matches(isNotRefreshing()))
    }

    @Test
    fun viewReactsCorrectlyToLoadingEventWithExistingData() {
        initializeFragment()
        val count = 33
        val currentPage = 0
        val lastPage = 19
        val dataPage = TopicListDataPage(
            TestData.generateFeaturedTopicList(count), currentPage, lastPage
        )
        topicListView.onLoading(Resource.Loading(dataPage))

        onView(recyclerViewMatcher).check(matches(isDisplayed()))
        onView(withId(R.id.progress)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.swipeRefreshLayout)).check(matches(isRefreshing()))
        onView(recyclerViewMatcher).check(matches(withItemCount(count + 1))) // +1 for footer
    }

    @Test
    fun viewReactsCorrectlyToSuccessfulDataLoad() {
        initializeFragment()
        val count = 33
        val currentPage = 0
        val lastPage = 19
        val dataPage = TopicListDataPage(
            TestData.generateFeaturedTopicList(count), currentPage, lastPage
        )
        topicListView.onSuccess(Resource.Success(dataPage))

        onView(recyclerViewMatcher).check(matches(isDisplayed()))
        onView(withId(R.id.progress)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.swipeRefreshLayout)).check(matches(isNotRefreshing()))
        onView(recyclerViewMatcher).check(matches(withItemCount(count + 1))) // +1 for footer
    }

    @Test
    fun viewReactsCorrectlyToFeaturedTopicListMetaEvent() {
        initializeFragment()
        topicListView.onTopicListMetaChanged(TopicListMeta(0, null, null))
        onView(withId(R.id.toolbar)).check(matches(withToolbarSubtitle("")))

        val currentPage = 0
        val lastPage = 19
        topicListView.onTopicListMetaChanged(TopicListMeta(currentPage, lastPage, null))
        onView(withId(R.id.toolbar)).check(matches(withToolbarSubtitle("Page ${currentPage + 1} of ${lastPage + 1}")))
    }

    @Test
    fun onClickButtons_theyWorkAppropriately() {
        initializeFragment()

        onView(withId(R.id.btnNextPage)).perform(click())
        verify(topicListViewListener, times(1)).loadNextPage()

        onView(withId(R.id.btnPrevPage)).perform(click())
        verify(topicListViewListener, times(1)).loadPrevPage()

        onView(withId(R.id.btnRefreshPage)).perform(click())
        verify(topicListViewListener, times(1)).refreshPage()

        onView(withId(R.id.toolbar)).perform(click())
        verify(topicListViewListener, times(1)).changePageOrSort()

        onView(withId(R.id.btnMore)).perform(click())
//        verify(topicListViewListener, times(1)).showExplore()

        onView(withId(R.id.btnNewTopic)).perform(click())
        verify(topicListViewListener, times(1)).newTopic()

        onView(withId(R.id.swipeRefreshLayout)).perform(pullRefresh())
        verify(topicListViewListener, times(1)).refreshPage()

//        onView(withId(R.id.ouch_view)).perform(ouchViewButtonClick())
//        verify(topicListViewListener, times(1)).retryLastRequest()
    }

    @Test
    fun viewReactsAppropriatelyTo_viewedTopicsLoadedEvent() {
        // attempting to check whether the recyclerview refreshes its content (calls notifyDatasetChanged)
        // by counting the calls its adapter makes to the `haveViewedTopic` method
        var expectedCount = 0
        whenever(topicListViewListener.hasViewedTopic(any())).then {
            expectedCount++
            false
        }

        initializeFragment()
        val dataPage = TopicListDataPage(
            TestData.generateFeaturedTopicList(33), 0, 19
        )
        topicListView.onSuccess(Resource.Success(dataPage))
        verify(topicListViewListener, atLeastOnce()).hasViewedTopic(any())


        // on viewed topics data loaded
        var counter = 0
        whenever(topicListViewListener.hasViewedTopic(any())).then {
            counter++
            false
        }
        topicListView.onViewedTopicsLoaded()
        assertEquals(expectedCount, counter)
    }

    @Test
    fun recyclerViewAdapterDisplayingFeaturedTopicItems_reactsAppropriatelyToEvents() {
        initializeFragment()
        val dataPage = TopicListDataPage(
            TestData.generateFeaturedTopicList(33), 0, 19
        )
        topicListView.onSuccess(Resource.Success(dataPage))

        onView(
            recyclerViewMatcher


        ).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        verify(topicListViewListener, times(1)).onTopicClicked(dataPage.data.first())
    }

    @Test
    fun recyclerViewAdapterDisplayingBoardTopicItems_reactsAppropriatelyToEvents() {
        initializeFragment(Board("Politics", "politics"))
        val dataPage = BoardsDataPage(
            data = TestData.generateDetailedTopicList(33),
            page = 0,
            last = 19,
            usersViewing = emptyList(),
            relatedBoards = "",
            numGuestsViewing = 0,
            numUsersViewing = 0,
            isFollowing = false,
            moderators = emptyList(),
            boardInfo = ""
        )
        topicListView.onSuccess(Resource.Success(dataPage))


        onView(recyclerViewMatcher).perform(
            actionOnItemAtPosition<ItemAdapter.BoardTopicItemVH>(
                0,
                click()
            )
        )
        verify(topicListViewListener, times(1)).onTopicClicked(dataPage.data.first())

        onView(recyclerViewMatcher).perform(
            actionOnItemAtPosition<ItemAdapter.BoardTopicItemVH>(
                0,
                boardTopicItemUserClick()
            )
        )
        verify(topicListViewListener, times(1)).onAuthorClicked(dataPage.data.first().author!!)
    }

    @Test
    fun recyclerViewAdapterDisplayingTrendingOrNewTopicItems_reactsAppropriatelyToEvents() {
        initializeFragment(Trending)
        val dataPage = TopicListDataPage(
            data = TestData.generateDetailedTopicList(33),
            page = 0,
            last = 19
        )
        topicListView.onSuccess(Resource.Success(dataPage))


        onView(recyclerViewMatcher).perform(
            actionOnItemAtPosition<ItemAdapter.BoardTopicItemVH>(
                0,
                click()
            )
        )
        verify(topicListViewListener, times(1)).onTopicClicked(dataPage.data.first())


        onView(recyclerViewMatcher).perform(
            actionOnItemAtPosition<ItemAdapter.BoardTopicItemVH>(
                0,
                boardTopicItemUserClick()
            )
        )
        verify(topicListViewListener, times(1)).onAuthorClicked(dataPage.data.first().author!!)


        onView(recyclerViewMatcher).perform(
            actionOnItemAtPosition<ItemAdapter.BoardTopicItemVH>(
                0,
                topicItemBoardClick()
            )
        )
        verify(topicListViewListener, times(1)).onBoardClicked(dataPage.data.first().mainBoard!!)
    }

    private fun initializeFragment(topicList: TopicList = Featured) {
        fragment = TestFragment()
        fragment.lifecycleScope.launchWhenStarted {
            val binding = TopicListScreenBinding.bind(fragment.requireView())
            // FIXME: pref mock
//            topicListView = TopicListView(mock(), binding, fragment.viewLifecycleOwner, topicList, topicListViewListener)
        }
        scenario.setFragment(fragment, TestFragment.newBundle(R.layout.topic_list_screen))
    }

    companion object {
        fun boardTopicItemUserClick() = object : ViewAction {
            override fun getDescription(): String {
                return "Click topic author"
            }

            override fun getConstraints(): Matcher<View>? {
                return null
            }

            override fun perform(uiController: UiController?, view: View?) {
                val author = view!!.findViewById<View>(R.id.story_author)
                author.performClick()
            }
        }

        fun topicItemBoardClick() = object : ViewAction {
            override fun getDescription(): String {
                return "Click topic board"
            }

            override fun getConstraints(): Matcher<View>? {
                return null
            }

            override fun perform(uiController: UiController?, view: View?) {
                val author = view!!.findViewById<View>(R.id.txtBoard)
                author.performClick()
            }
        }
    }
}