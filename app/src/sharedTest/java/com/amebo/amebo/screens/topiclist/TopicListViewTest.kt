package com.amebo.amebo.screens.topiclist

import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
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
import com.amebo.amebo.screens.topiclist.adapters.HeaderAdapter
import com.amebo.amebo.screens.topiclist.adapters.ItemAdapter
import com.amebo.amebo.screens.topiclist.main.TopicListView
import com.amebo.amebo.suite.*
import com.amebo.core.domain.*
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsInstanceOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


class TopicListViewTest {

    @RunWith(AndroidJUnit4::class)
    class Featured {
        private lateinit var scenario: ActivityScenario<TestFragmentActivity>
        private lateinit var topicListView: TopicListView
        private lateinit var topicListViewListener: TopicListView.Listener
        private lateinit var pref: TestPref
        private val recyclerViewMatcher get() = withIndexOfType(withId(R.id.recyclerView), 0)


        @Before
        fun before() {
            pref = TestPref()
            topicListViewListener = mock()
            scenario =
                launchTestFragmentInTestActivity(R.layout.topic_list_screen) { fragment, _ ->
                    val binding = TopicListScreenBinding.bind(fragment.requireView())
                    topicListView = TopicListView(
                        pref,
                        binding,
                        fragment.viewLifecycleOwner,
                        Featured,
                        topicListViewListener
                    )
                }
        }


        @Test
        fun onCreate_viewDisplaysCorrectly() {
            onView(withId(R.id.title)).check(matches(withText(R.string.featured)))
            onView(withId(R.id.txtPageInfo)).check(matches(withText("")))
        }

        @Test
        fun onLoadingEventNoData() {
            topicListView.onLoading(Resource.Loading(null))

            onView(recyclerViewMatcher).check(matches(withItemCount(1)))
            onView(recyclerViewMatcher).check(matches(atPosition(0, hasDescendant(allOf(withId(R.id.progress), isDisplayed())))))
            onView(withId(R.id.swipeRefreshLayout)).check(matches(isNotRefreshing()))
        }

        @Test
        fun onLoadingEvent_WithData() {
            val count = 33
            val currentPage = 0
            val lastPage = 19
            val dataPage = TopicListDataPage(
                TestData.generateFeaturedTopicList(count), currentPage, lastPage
            )
            topicListView.onLoading(Resource.Loading(dataPage))

            onView(recyclerViewMatcher).check(matches(isDisplayed()))
            onView(withId(R.id.swipeRefreshLayout)).check(matches(isRefreshing()))
            onView(recyclerViewMatcher).check(matches(withItemCount(count + 1))) // +1 for footer
        }

        @Test
        fun onDataEvent_dataDisplayedRight() {
            val count = 33
            val currentPage = 0
            val lastPage = 19
            val dataPage = TopicListDataPage(
                TestData.generateFeaturedTopicList(count), currentPage, lastPage
            )
            topicListView.onSuccess(Resource.Success(dataPage))

            onView(recyclerViewMatcher).check(matches(isDisplayed()))
            onView(withId(R.id.swipeRefreshLayout)).check(matches(isNotRefreshing()))
            onView(recyclerViewMatcher).check(matches(withItemCount(count + 1))) // +1 for footer
        }

        @Test
        fun onMetaEvent_viewsUpdatedAppropriately() {
            var meta = TopicListMeta(0, null, null)
            topicListView.onTopicListMetaChanged(meta)
            onView(withId(R.id.txtPageInfo)).check(matches(withText("Page ${meta.page + 1}")))

            meta = TopicListMeta(0, 19, null)
            topicListView.onTopicListMetaChanged(meta)
            onView(withId(R.id.txtPageInfo)).check(matches(withText("Page ${meta.page + 1} of ${meta.lastPage!! + 1}")))

            meta = TopicListMeta(7, 20, TopicListSorts.UPDATED)
            topicListView.onTopicListMetaChanged(meta)
            onView(withId(R.id.txtPageInfo)).check(matches(withText("Page ${meta.page + 1} of ${meta.lastPage!! + 1}")))
        }

        @Test
        fun onClickButtons_theyWorkAppropriately() {
            onView(withId(R.id.btnNextPage)).perform(click())
            verify(topicListViewListener, times(1)).loadNextPage()

            onView(withId(R.id.btnPrevPage)).perform(click())
            verify(topicListViewListener, times(1)).loadPrevPage()

            onView(withId(R.id.btnRefreshPage)).perform(click())
            verify(topicListViewListener, times(1)).refreshPage()

            onView(withId(R.id.txtPageInfo)).perform(click())
            verify(topicListViewListener, times(1)).changePageOrSort()

            onView(withId(R.id.btnMore)).perform(click())
            verify(topicListViewListener, times(1)).onMoreClicked()

            onView(withId(R.id.btnNewTopic)).perform(click())
            verify(topicListViewListener, times(1)).newTopic()

            onView(withId(R.id.swipeRefreshLayout)).perform(pullRefresh())
            verify(topicListViewListener, times(1)).refreshPage()

//        onView(withId(R.id.ouch_view)).perform(ouchViewButtonClick())
//        verify(topicListViewListener, times(1)).retryLastRequest()
        }

        @Test
        fun onViewedTopicsLoadedEvent_viewsUpdatedAppropriately() {
            // attempting to check whether the recyclerview refreshes its content (calls notifyDatasetChanged)
            // by counting the calls its adapter makes to the `haveViewedTopic` method
            var expectedCount = 0
            whenever(topicListViewListener.hasViewedTopic(any())).then {
                expectedCount++
                false
            }

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
            assertThat(counter).isEqualTo(expectedCount)
        }

        @Test
        fun recyclerViewAdapterDisplayingFeaturedTopicItems_reactsAppropriatelyToEvents() {
            val dataPage = TopicListDataPage(
                TestData.generateFeaturedTopicList(33), 0, 19
            )
            topicListView.onSuccess(Resource.Success(dataPage))

            onView(
                recyclerViewMatcher


            ).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
            verify(topicListViewListener, times(1)).onTopicClicked(dataPage.data.first())
        }
    }

    @RunWith(AndroidJUnit4::class)
    class Board {
        private lateinit var scenario: ActivityScenario<TestFragmentActivity>
        private lateinit var topicListView: TopicListView
        private lateinit var topicListViewListener: TopicListView.Listener
        private lateinit var pref: TestPref
        private val recyclerViewMatcher get() = withIndexOfType(withId(R.id.recyclerView), 0)


        @Before
        fun before() {
            pref = TestPref().apply {
                defaultSort = TopicListSorts.BoardSorts.first()
            }
            topicListViewListener = mock()
            scenario = launchTestFragmentInTestActivity(R.layout.topic_list_screen) { fragment, _ ->
                val binding = TopicListScreenBinding.bind(fragment.requireView())
                topicListView = TopicListView(
                    pref,
                    binding,
                    fragment.viewLifecycleOwner,
                    Board("Politics", "politics"),
                    topicListViewListener
                )
            }
        }

        @Test
        fun onSortItemClick_ListenerTriggered() {
            val dataPage = newDataPage()
            topicListView.onSuccess(Resource.Success(dataPage))
            onView(recyclerViewMatcher).perform(
                actionOnItemAtPosition<HeaderAdapter.SortVH>(
                    0,
                    onViewHolderItem(withText(R.string.updated), click())
                )
            )

            val captor = argumentCaptor<Sort>()
            verify(topicListViewListener).onSortSelected(captor.capture())
            assertThat(captor.firstValue.name).isEqualTo(pref.defaultSort?.name)
        }

        @Test
        fun onTopicClick_ListenerTriggered() {
            val dataPage = newDataPage()
            topicListView.onSuccess(Resource.Success(dataPage))

            onView(recyclerViewMatcher).perform(
                actionOnItemAtPosition<ItemAdapter.BoardTopicItemVH>(
                    1,
                    click()
                )
            )
            verify(topicListViewListener, times(1)).onTopicClicked(dataPage.data.first())
        }


        @Test
        fun onTopicAuthorClick_ListenerTriggered() {
            val dataPage = newDataPage()
            topicListView.onSuccess(Resource.Success(dataPage))

            onView(recyclerViewMatcher).perform(
                actionOnItemAtPosition<ItemAdapter.BoardTopicItemVH>(
                    1,
                    boardTopicItemUserClick()
                )
            )
            verify(topicListViewListener, times(1)).onAuthorClicked(dataPage.data.first().author!!)
        }

        private fun newDataPage() = BoardsDataPage(
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
    }

    @RunWith(AndroidJUnit4::class)
    class Trending {
        private lateinit var scenario: ActivityScenario<TestFragmentActivity>
        private lateinit var topicListView: TopicListView
        private lateinit var topicListViewListener: TopicListView.Listener
        private lateinit var pref: TestPref
        private val recyclerViewMatcher get() = withIndexOfType(withId(R.id.recyclerView), 0)


        @Before
        fun before() {
            pref = TestPref()
            topicListViewListener = mock()
            scenario = launchTestFragmentInTestActivity(R.layout.topic_list_screen) { fragment, _ ->
                val binding = TopicListScreenBinding.bind(fragment.requireView())
                topicListView = TopicListView(
                    pref,
                    binding,
                    fragment.viewLifecycleOwner,
                    Trending,
                    topicListViewListener
                )
            }
        }

        @Test
        fun onTopicClick() {
            val dataPage = newDataPage()
            topicListView.onSuccess(Resource.Success(dataPage))

            onView(recyclerViewMatcher).perform(
                actionOnItemAtPosition<ItemAdapter.BoardTopicItemVH>(
                    0,
                    click()
                )
            )
            verify(topicListViewListener, times(1)).onTopicClicked(dataPage.data.first())
        }

        @Test
        fun onTopicAuthorClick() {
            val dataPage = newDataPage()
            topicListView.onSuccess(Resource.Success(dataPage))

            onView(recyclerViewMatcher).perform(
                actionOnItemAtPosition<ItemAdapter.BoardTopicItemVH>(
                    0,
                    boardTopicItemUserClick()
                )
            )
            verify(topicListViewListener, times(1)).onAuthorClicked(dataPage.data.first().author!!)
        }

        @Test
        fun onBoardClick() {
            val dataPage = newDataPage()
            topicListView.onSuccess(Resource.Success(dataPage))


            onView(recyclerViewMatcher).perform(
                actionOnItemAtPosition<ItemAdapter.BoardTopicItemVH>(
                    0,
                    topicItemBoardClick()
                )
            )
            verify(
                topicListViewListener,
                times(1)
            ).onBoardClicked(dataPage.data.first().mainBoard!!)
        }

        private fun newDataPage() = TopicListDataPage(
            data = TestData.generateDetailedTopicList(33),
            page = 0,
            last = 19
        )
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