package com.amebo.amebo.screens.postlist.topic

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.application.TestFragmentActivity
import com.amebo.amebo.data.TestData
import com.amebo.amebo.di.mocks.Mocks
import com.amebo.amebo.di.mocks.Mocks.TopicScreen.view
import com.amebo.amebo.di.mocks.Mocks.TopicScreen.vm
import com.amebo.amebo.suite.*
import com.amebo.core.domain.*
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TopicScreenTest {
    private lateinit var scenario: ActivityScenario<TestFragmentActivity>
    private val topic = TestData.topics.first()
    private val fragment: TopicScreen
        get() {
            var frag: TopicScreen? = null
            scenario.onFragment<TopicScreen> {
                frag = it
            }
            return frag!!
        }

    @Before
    fun before() {
        injectIntoTestApp()

        Mocks.TopicScreen.createVM()
        setupViewModelFactory(vm)
        setupViewModelFactory(Mocks.UserManagement.new())

        scenario = launchFragmentInTestActivity(
            TopicScreen(),
            TopicScreen.bundle(topic)
        )
    }

    @Test
    fun onCreateView_fragmentInitializedCorrectly() {
        verify(vm, times(1)).dataEvent
        verify(vm.dataEvent, times(1)).observe(any(), any())
        verify(vm.metaEvent, times(1)).observe(any(), any())
        scenario.onFragment<TopicScreen> {
            assertThat(it.postListView).isEqualTo(view)
        }
    }

    @Test
    fun onNextClicked_initiatedNextPageLoading() {
        fragment.onNextClicked()

        verify(vm, times(1)).loadNextPage()
    }

    @Test
    fun onPrevClicked_initiatedPrevPageLoading() {
        fragment.onPrevClicked()

        verify(vm, times(1)).loadPrevPage()
    }

    @Test
    fun onFirstClicked_initiatedFirstPageLoading() {
        fragment.onFirstClicked()

        verify(vm, times(1)).loadFirstPage()
    }

    @Test
    fun onLastClicked_initiatedLastPageLoading() {
        fragment.onLastClicked()

        verify(vm, times(1)).loadLastPage()
    }

    @Test
    fun onMoreClicked_displayedPopup() {
        fragment.onMoreClicked(View(ApplicationProvider.getApplicationContext()))


        onView(withText(R.id.collapse_all)).inRoot(isPlatformPopup())
    }

    @Test
    fun onNavigationClicked_routesBackToPreviousScreen() {
        fragment.onNavigationClicked()

        verify(fragment.router, times(1)).back()
    }

    @Test
    fun onRetryClicked_retryInitiated() {
        fragment.onRetryClicked()

        verify(vm, times(1)).retry()
    }

    @Test
    fun onRefreshTriggered_refreshInitiated() {
        fragment.onRefreshTriggered()

        verify(vm, times(1)).refreshPage()
    }

    @Test
    fun onLikePost_likePostInitiated() {
        val post = TestData.newPost()

        fragment.likePost(post, true)

        val postCaptor = argumentCaptor<SimplePost>()
        val boolCaptor = argumentCaptor<Boolean>()
        verify(vm, times(1)).likePost(postCaptor.capture(), boolCaptor.capture())
        assertThat(postCaptor.firstValue).isEqualTo(post)
        assertThat(boolCaptor.firstValue).isTrue()
    }

    @Test
    fun onSharePost_sharePostInitiated() {
        val post = TestData.newPost()

        fragment.sharePost(post, false)

        val postCaptor = argumentCaptor<SimplePost>()
        val boolCaptor = argumentCaptor<Boolean>()
        verify(vm, times(1)).sharePost(postCaptor.capture(), boolCaptor.capture())
        assertThat(postCaptor.firstValue).isEqualTo(post)
        assertThat(boolCaptor.firstValue).isFalse()
    }


    @Test
    fun onTopicClicked_routesToTopicScreen() {
        val topic = TestData.generateTopic()

        fragment.onPostTopicClick(topic, fragment.requireView())//FIXME

        val captor = argumentCaptor<Topic>()
        verify(fragment.router, times(1)).toTopic(captor.capture())
        assertThat(captor.firstValue).isEqualTo(topic)
    }

    @Test
    fun onUserClicked_routesToUserScreen() {
        val user = TestData.generateUser()

        fragment.onUserClicked(user)

        val captor = argumentCaptor<User>()
        verify(fragment.router, times(1)).toUser(captor.capture(), false)
        assertThat(captor.firstValue).isEqualTo(user)
    }

    @Test
    fun onItemCollapsed_collapsedItemAdded() {
        val collapsedItems = mutableSetOf<Int>()
        whenever(vm.collapsedItems).thenReturn(collapsedItems)
        val position = 99

        fragment.onItemCollapsed(position)

        assertThat(collapsedItems).containsExactly(position)
    }

    @Test
    fun onItemExpanded_collapsedItemRemoved() {
        val collapsedItems = mutableSetOf(99, 91, 0)
        whenever(vm.collapsedItems).thenReturn(collapsedItems)
        val position = 99

        fragment.onItemExpanded(position)

        assertThat(collapsedItems).doesNotContain(position)
        assertThat(collapsedItems).hasSize(2)
    }

    @Test
    fun checkForCollapsedItem_returnsDesiredResult() {
        val collapsedItems = mutableSetOf(99, 91, 0)
        whenever(vm.collapsedItems).thenReturn(collapsedItems)


        collapsedItems.forEach {
            assertThat(fragment.isItemCollapsed(it)).isTrue()
        }
        assertThat(fragment.isItemCollapsed(20)).isFalse()
        assertThat(fragment.isItemCollapsed(92)).isFalse()
        assertThat(fragment.isItemCollapsed(100)).isFalse()
    }

    @Test
    fun onYoutubeLinkClicked_routesToYoutubeScreen() {
        val videoId = "abcdef"

        fragment.onYoutubeUrlClick(videoId)

        val captor = argumentCaptor<String>()
        verify(fragment.router, times(1)).toYoutubeScreen(captor.capture())
        assertThat(captor.firstValue).isEqualTo(videoId)
    }

    @Test
    fun hasNextPageRequest_appropriateViewModelMethodCalled() {
        whenever(vm.hasNextPage).thenReturn(true)

        assertThat(fragment.hasNextPage()).isTrue()
        verify(vm, times(1)).hasNextPage
    }

    @Test
    fun nextPageRequestFromFooterItem_initiatedNextPage() {
        fragment.nextPage()

        verify(vm, times(1)).loadNextPage()
    }

    @Test
    fun onTopicLongPressed_routesToTopicScreenWithPageInfo() {
        val page = 20
        val topic = TestData.generateTopic(linkedPage = page)

        // FIXME
//        fragment.onTopicLinkClick(topic)

        val topicCaptor = argumentCaptor<Topic>()
        val pageNumCaptor = argumentCaptor<Int>()
        verify(fragment.router, times(1)).toTopic(topicCaptor.capture(), pageNumCaptor.capture())
        assertThat(topicCaptor.firstValue).isEqualTo(topic)
        assertThat(pageNumCaptor.firstValue).isEqualTo(page)
    }

    @Test
    fun onBoardClicked_routesToBoardScreen() {
        val board = TestData.generateBoard()

        fragment.onBoardClicked(board)

        val captor = argumentCaptor<Board>()
        verify(fragment.router, times(1)).toBoard(captor.capture())
        assertThat(captor.firstValue).isEqualTo(board)
    }

    @Test
    fun onReplyPostClicked_routesToNewPostScreen_onlyIfTopicIsOpenAndUserIsLoggedIn() {
        val post = TestData.newPost()

        val topicIsOpenAndIsLoggedInPairs = listOf(
            false to false,
            false to true,
            false to true,
            true to true
        )

        topicIsOpenAndIsLoggedInPairs.forEach {
            val (topicIsOpen, isLoggedIn) = it
            whenever(vm.isClosed).thenReturn(!topicIsOpen)
            with(fragment.pref as TestPref) {
                this.isLoggedIn = isLoggedIn
            }

            // action
            fragment.replyPost(post)
        }

        val captor = argumentCaptor<SimplePost>()
        // isLoggedIn && topicIsOpen --> 1
        verify(fragment.router, times(1)).toReplyPost(captor.capture())
        assertThat(captor.lastValue).isEqualTo(post)

        // !isLoggedIn && topicIsOpen -->
        verify(fragment.router, times(1)).toAccountList()
    }

}