package com.amebo.amebo.screens.postlist.topic

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.widgets.StateLayout
import com.amebo.amebo.data.TestData
import com.amebo.amebo.databinding.TopicScreenBinding
import com.amebo.amebo.screens.postlist.PostListMeta
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.amebo.screens.postlist.components.IPostListView
import com.amebo.amebo.suite.*
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.SimplePost
import com.amebo.core.domain.Topic
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TopicPostListViewTest {

    lateinit var view: TopicPostListView
    lateinit var listener: IPostListView.Listener
    lateinit var topic: Topic
    lateinit var itemAdapter: ItemAdapter

    @Before
    fun before() {
        topic = TestData.topics.first()
        listener = mock()
        itemAdapter = mock()
        launchTestFragmentInTestActivity(R.layout.topic_screen) { fragment, view ->
            val binding = TopicScreenBinding.bind(view)
            this.view =
                TopicPostListView(
                    fragment,
                    itemAdapter,
                    binding,
                    topic,
                    listener
                )
        }
    }


    @Test
    fun onCreateView_viewIsInitialized() {
        onView(withId(R.id.stateLayout)).check(matches(withState(StateLayout.State.Progress)))
        onView(withId(R.id.toolbar)).check(matches(withToolbarTitle(topic.title)))
        onView(withId(R.id.btnMore)).check(matches(isEnabled()))
        onView(withId(R.id.btnReply)).check(matches(isEnabled()))
        onView(withId(R.id.btnRefreshPage)).check(matches(isEnabled()))
        onView(withId(R.id.btnNextPage)).check(matches(isDisabled()))
        onView(withId(R.id.btnPrevPage)).check(matches(isDisabled()))
    }

    @Test
    fun onBtnNextClick_loadNextPage() {
        onView(withId(R.id.btnNextPage)).perform(click())
        verify(listener, times(1)).onNextClicked()
    }

    @Test
    fun onBtnNextLongPress_loadLastPage() {
        onView(withId(R.id.btnNextPage)).perform(longClick())
        verify(listener, times(1)).onLastClicked()
    }

    @Test
    fun onBtnPrevClick_loadPrevPage() {
        onView(withId(R.id.btnPrevPage)).perform(click())
        verify(listener, times(1)).onPrevClicked()
    }

    @Test
    fun onBtnPrevLongPress_loadFirstPage() {
        onView(withId(R.id.btnPrevPage)).perform(longClick())
        verify(listener, times(1)).onFirstClicked()
    }

    @Test
    fun onBtnRefreshClick_refreshPage() {
        onView(withId(R.id.btnRefreshPage)).perform(click())
        verify(listener, times(1)).onRefreshTriggered()
    }

    @Test
    fun onSwipe_refreshPage() {
        onView(withId(R.id.stateLayout)).perform(stateLayoutAction { it.content() })
        onView(withId(R.id.swipeRefreshLayout)).perform(
            (withCustomConstraints(
                swipeDown(),
                isDisplayingAtLeast(85)
            ))
        )
        verify(listener, times(1)).onRefreshTriggered()
    }

    @Test
    fun onLoadingRefresh_viewReactsAccordingly() {
        view.onLoading(Resource.Loading(null))

        onView(withId(R.id.stateLayout)).check(matches(withState(StateLayout.State.Progress)))
        onView(withId(R.id.swipeRefreshLayout)).check(matches(isNotRefreshing()))
        verify(itemAdapter, times(1)).clear()
    }

    @Test
    fun onLoadingWithStateContent_viewReactsAccordingly() {
        val data = TestData.fetchPostListOnTopic(topic)
        view.onLoading(Resource.Loading(data))

        onView(withId(R.id.swipeRefreshLayout)).check(matches(isRefreshing()))
        onView(withId(R.id.stateLayout)).check(matches(withState(StateLayout.State.Content)))
        verify(itemAdapter, times(1)).postListDataPage = data
    }

    @Test
    fun onPostListMeta_SubtitleSet() {
        arrayOf(
            PostListMeta(0, 1),
            PostListMeta(1, 1)
        ).forEach { meta ->
            val expectedSubtitle = when (meta.lastPage) {
                is Int -> "Page ${meta.currentPage + 1}"
                else -> "Page ${meta.currentPage + 1} of ${meta.lastPage!! + 1}"
            }
            view.setPostListMeta(meta)


            onView(withId(R.id.toolbar)).check(matches(withToolbarSubtitle(expectedSubtitle)))
        }
    }

    @Test
    fun navigationOnClick_backPressTriggered() {
        onView(withId(R.id.toolbar)).perform(navigationClick())

        verify(listener, times(1)).onNavigationClicked()
    }

    @Test
    fun onBtnMoreClicked_showPopup() {
        onView(withId(R.id.btnMore)).perform(click())
        verify(listener, times(1)).onMoreClicked(any())
    }

    @Test
    fun onExpandPosts_appropriateAdapterMethodCalled() {
        view.expandAllPosts()
        verify(itemAdapter, times(1)).expandAllPosts()
    }

    fun onCollapsePosts_appropriateAdapterMethodCalled() {
        view.collapseAllPosts()
        verify(itemAdapter, times(1)).collapseAllPosts()
    }

    @Test
    fun onSetHasPrevPage_btnPrevUpdated() {
        arrayOf(false, true).forEach {
            view.setHasPrevPage(it)
            onView(withId(R.id.btnPrevPage)).check(
                matches(
                    if (it) isEnabled() else isDisabled()
                )
            )
        }
    }

    @Test
    fun onSetHasNextPage_btnNextUpdated() {
        arrayOf(false, true).forEach {
            view.setHasNextPage(it)
            onView(withId(R.id.btnNextPage)).check(
                matches(
                    if (it) isEnabled() else isDisabled()
                )
            )
        }
    }

    @Test
    fun onScrollToPost_adapterMethodCalledAppropriately() {
        // setup
        val dataPage = TestData.fetchPostListOnTopic(topic, postCount = 22)
        val postToPositionList = dataPage.data.mapIndexed { index, post ->
            (post as SimplePost) to index
        }.shuffled()
        view.onSuccess(Resource.Success(dataPage))

        val captor = argumentCaptor<String>()


        // action
        postToPositionList.forEach {
            val (post, expectedPos) = it
            whenever(itemAdapter.findPostPosition(captor.capture())).thenReturn(expectedPos)

            // action
            view.scrollToPost(post.id)

            // verify
            assertThat(captor.lastValue).isEqualTo(post.id)
            onData(`is`(withItemCount(postToPositionList.size + 1))) // for footer
                .atPosition(expectedPos)
                .check(matches(isDisplayed()))
        }


        verify(itemAdapter, times(postToPositionList.size)).findPostPosition(any())
    }

    @Test
    fun onSuccess_viewUpdated() {
        val data = TestData.fetchPostListOnTopic(topic)
        view.onSuccess(Resource.Success(data))

        onView(withId(R.id.stateLayout)).check(matches(withState(StateLayout.State.Content)))
        onView(withId(R.id.swipeRefreshLayout)).check(matches(isNotRefreshing()))
        verify(itemAdapter, times(1)).postListDataPage = data
    }

    @Test
    fun onErrorNoContent_viewUpdated() {
        view.onError(Resource.Error(ErrorResponse.Network, null))

        onView(withId(R.id.stateLayout)).check(matches(withState(StateLayout.State.Failure)))
        onView(withId(R.id.swipeRefreshLayout)).check(matches(isNotRefreshing()))
    }

    @Test
    fun stateLayoutContentState_isCorrectlySetup() {
        onView(withId(R.id.stateLayout)).perform(stateLayoutAction { it.content() })

        onView(withId(R.id.recyclerView)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun stateLayoutFailureState_isCorrectlySetup() {
        onView(withId(R.id.stateLayout)).perform(stateLayoutAction { it.failure() })

        onViews(withId(R.id.recyclerView), withId(R.id.progress)).check(
            matches(
                withEffectiveVisibility(Visibility.GONE)
            )
        )
    }

    @Test
    fun stateLayoutProgressState_isCorrectlySetup() {
        onView(withId(R.id.stateLayout)).perform(stateLayoutAction { it.loading() })

        onViews(withId(R.id.recyclerView), withId(R.id.ouchView)).check(
            matches(
                withEffectiveVisibility(Visibility.GONE)
            )
        )
    }
}