package com.amebo.amebo.screens.postlist.topic

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.application.TestFragmentActivity
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.amebo.data.TestData
import com.amebo.amebo.di.TopicPostListViewModule
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.amebo.amebo.screens.postlist.PostListMeta
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
    private lateinit var topicViewModel: TopicViewModel
    private lateinit var userManagementViewModel: UserManagementViewModel
    private lateinit var view: TopicPostListView
    private lateinit var dataEvent: MutableLiveData<Event<Resource<PostListDataPage>>>
    private lateinit var metaEvent: MutableLiveData<Event<PostListMeta>>
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

    }

    @Test
    fun onCreateView_fragmentInitializedCorrectly() {
        initialize()
        verify(topicViewModel, times(1)).dataEvent
        verify(topicViewModel.dataEvent, times(1)).observe(any(), any())
        verify(topicViewModel.metaEvent, times(1)).observe(any(), any())
        scenario.onFragment<TopicScreen> {
            assertThat(it.postListView).isEqualTo(view)
        }
    }

    @Test
    fun onNextClicked_initiatedNextPageLoading() {
        initialize()

        fragment.onNextClicked()

        verify(topicViewModel, times(1)).loadNextPage()
    }

    @Test
    fun onPrevClicked_initiatedPrevPageLoading() {
        initialize()

        fragment.onPrevClicked()

        verify(topicViewModel, times(1)).loadPrevPage()
    }

    @Test
    fun onFirstClicked_initiatedFirstPageLoading() {
        initialize()

        fragment.onFirstClicked()

        verify(topicViewModel, times(1)).loadFirstPage()
    }

    @Test
    fun onLastClicked_initiatedLastPageLoading() {
        initialize()

        fragment.onLastClicked()

        verify(topicViewModel, times(1)).loadLastPage()
    }

    @Test
    fun onMoreClicked_displayedPopup() {
        initialize()

        fragment.onMoreClicked(View(ApplicationProvider.getApplicationContext()))


        onView(withText(R.id.collapse_all)).inRoot(isPlatformPopup())
    }

    @Test
    fun onNavigationClicked_routesBackToPreviousScreen() {
        initialize()

        fragment.onNavigationClicked()

        verify(fragment.router, times(1)).back()
    }

    @Test
    fun onRetryClicked_retryInitiated() {
        initialize()

        fragment.onRetryClicked()

        verify(topicViewModel, times(1)).retry()
    }

    @Test
    fun onRefreshTriggered_refreshInitiated() {
        initialize()

        fragment.onRefreshTriggered()

        verify(topicViewModel, times(1)).refreshPage()
    }

    @Test
    fun onLikePost_likePostInitiated() {
        initialize()
        val post = TestData.newPost()

        fragment.likePost(post, true)

        val postCaptor = argumentCaptor<SimplePost>()
        val boolCaptor = argumentCaptor<Boolean>()
        verify(topicViewModel, times(1)).likePost(postCaptor.capture(), boolCaptor.capture())
        assertThat(postCaptor.firstValue).isEqualTo(post)
        assertThat(boolCaptor.firstValue).isTrue()
    }

    @Test
    fun onSharePost_sharePostInitiated() {
        initialize()
        val post = TestData.newPost()

        fragment.sharePost(post, false)

        val postCaptor = argumentCaptor<SimplePost>()
        val boolCaptor = argumentCaptor<Boolean>()
        verify(topicViewModel, times(1)).sharePost(postCaptor.capture(), boolCaptor.capture())
        assertThat(postCaptor.firstValue).isEqualTo(post)
        assertThat(boolCaptor.firstValue).isFalse()
    }


    @Test
    fun onTopicClicked_routesToTopicScreen() {
        initialize()
        val topic = TestData.generateTopic()

        fragment.onPostTopicClick(topic, fragment.requireView())//FIXME

        val captor = argumentCaptor<Topic>()
        verify(fragment.router, times(1)).toTopic(captor.capture())
        assertThat(captor.firstValue).isEqualTo(topic)
    }

    @Test
    fun onUserClicked_routesToUserScreen() {
        initialize()
        val user = TestData.generateUser()

        fragment.onUserClicked(user)

        val captor = argumentCaptor<User>()
        verify(fragment.router, times(1)).toUser(captor.capture(), false)
        assertThat(captor.firstValue).isEqualTo(user)
    }

    @Test
    fun onItemCollapsed_collapsedItemAdded() {
        initialize()
        val collapsedItems = mutableSetOf<Int>()
        whenever(topicViewModel.collapsedItems).thenReturn(collapsedItems)
        val position = 99

        fragment.onItemCollapsed(position)

        assertThat(collapsedItems).containsExactly(position)
    }

    @Test
    fun onItemExpanded_collapsedItemRemoved() {
        initialize()
        val collapsedItems = mutableSetOf(99, 91, 0)
        whenever(topicViewModel.collapsedItems).thenReturn(collapsedItems)
        val position = 99

        fragment.onItemExpanded(position)

        assertThat(collapsedItems).doesNotContain(position)
        assertThat(collapsedItems).hasSize(2)
    }

    @Test
    fun checkForCollapsedItem_returnsDesiredResult() {
        initialize()
        val collapsedItems = mutableSetOf(99, 91, 0)
        whenever(topicViewModel.collapsedItems).thenReturn(collapsedItems)


        collapsedItems.forEach {
            assertThat(fragment.isItemCollapsed(it)).isTrue()
        }
        assertThat(fragment.isItemCollapsed(20)).isFalse()
        assertThat(fragment.isItemCollapsed(92)).isFalse()
        assertThat(fragment.isItemCollapsed(100)).isFalse()
    }

    @Test
    fun onYoutubeLinkClicked_routesToYoutubeScreen() {
        initialize()
        val videoId = "abcdef"

        fragment.onYoutubeUrlClick(videoId)

        val captor = argumentCaptor<String>()
        verify(fragment.router, times(1)).toYoutubeScreen(captor.capture())
        assertThat(captor.firstValue).isEqualTo(videoId)
    }

    @Test
    fun hasNextPageRequest_appropriateViewModelMethodCalled() {
        initialize()
        whenever(topicViewModel.hasNextPage).thenReturn(true)

        assertThat(fragment.hasNextPage()).isTrue()
        verify(topicViewModel, times(1)).hasNextPage
    }

    @Test
    fun nextPageRequestFromFooterItem_initiatedNextPage() {
        initialize()

        fragment.nextPage()

        verify(topicViewModel, times(1)).loadNextPage()
    }

    @Test
    fun onTopicLongPressed_routesToTopicScreenWithPageInfo() {
        initialize()
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
        initialize()
        val board = TestData.generateBoard()

        fragment.onBoardClicked(board)

        val captor = argumentCaptor<Board>()
        verify(fragment.router, times(1)).toBoard(captor.capture())
        assertThat(captor.firstValue).isEqualTo(board)
    }

    @Test
    fun onReplyPostClicked_routesToNewPostScreen_onlyIfTopicIsOpenAndUserIsLoggedIn() {
        initialize()
        val post = TestData.newPost()

        val topicIsOpenAndIsLoggedInPairs = listOf(
            false to false,
            false to true,
            false to true,
            true to true
        )

        topicIsOpenAndIsLoggedInPairs.forEach {
            val (topicIsOpen, isLoggedIn) = it
            whenever(topicViewModel.isClosed).thenReturn(!topicIsOpen)
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

    private fun initialize(useMocks: Boolean = true) {
        injectIntoTestApp()
        if (useMocks) initLiveDataWithMocks() else initLiveData()
        setupViewModels()
        setupViewModels()
        scenario = launchFragmentInTestActivity(
            TopicScreen(),
            TopicScreen.bundle(topic)
        )
        view = TopicPostListViewModule.postListView
    }

    private fun setupViewModels() {
        topicViewModel = mock()
        userManagementViewModel = mock()
        whenever(topicViewModel.dataEvent).thenReturn(dataEvent)
        whenever(topicViewModel.metaEvent).thenReturn(metaEvent)
        setupViewModelFactory(topicViewModel)
        setupViewModelFactory(userManagementViewModel)
    }

    private fun initLiveData() {
        dataEvent = MutableLiveData()
        metaEvent = MutableLiveData()
    }

    private fun initLiveDataWithMocks() {
        dataEvent = mock()
        metaEvent = mock()
    }
}