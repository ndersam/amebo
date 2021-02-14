package com.amebo.amebo.screens.topiclist

import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.view.MotionEvent
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.coroutineScope
import com.amebo.amebo.common.*
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.core.domain.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

abstract class BaseTopicListScreen(@LayoutRes layoutRes: Int) : BaseFragment(layoutRes),
    BaseTopicListView.Listener {
    val topicList by lazy { requireArguments().getParcelable<TopicList>(TOPIC_LIST)!! }
    private val initialSort get() = pref.defaultSortOf(topicList)
    private val initialPage get() = requireArguments().getInt(PAGE)
    val viewModel: TopicListScreenViewModel by viewModels()

    private var lastLoadTime: Long = 0L


    private var refreshJob: Job? = null

    @Inject
    lateinit var topicListViewProvider: Provider<BaseTopicListView>
    protected open lateinit var baseTopicListView: BaseTopicListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(FragKeys.RESULT_SELECTED_SORT) { _, bundle ->
            viewModel.sortBy(bundle.getParcelable(FragKeys.BUNDLE_SELECTED_SORT)!!)
        }
    }


    @CallSuper
    override fun onViewCreated(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            lastLoadTime = it.getLong(LAST_LOAD_TIME)
        }
        initializeViewBindings()
        viewModel.initialize(topicList, initialSort, initialPage)
        viewModel.dataPageEvent.observe(viewLifecycleOwner, EventObserver(::onEventContentChanged))
        viewModel.topicListMetaEvent.observe(
            viewLifecycleOwner,
            EventObserver(baseTopicListView::onTopicListMetaChanged)
        )
        viewModel.viewedTopicsLoadedEvent.observe(viewLifecycleOwner, EventObserver {
            baseTopicListView.onViewedTopicsLoaded()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val activity = requireActivity()
        if (activity is TouchEventDispatcher) {
            activity.unRegister(::onActivityTouchEvent)
        }
        refreshJob = null
    }

    override fun onPause() {
        super.onPause()
        saveLayoutState()
    }

    override fun onResume() {
        super.onResume()
        restoreLayoutState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(LAST_LOAD_TIME, lastLoadTime)
    }

    private fun restoreLayoutState(bundle: Bundle = requireArguments()) {
        val parcelable = bundle.getParcelable<Parcelable?>(BUNDLE_RECYCLER_VIEW_STATE)
            ?: return
        baseTopicListView.restoreLayoutState(parcelable)
    }


    private fun saveLayoutState(bundle: Bundle = requireArguments()) {
        bundle.putParcelable(BUNDLE_RECYCLER_VIEW_STATE, baseTopicListView.saveLayoutState())
    }

    @CallSuper
    open fun initializeViewBindings() {
        baseTopicListView = topicListViewProvider.get()!!
    }

    override fun hasNextPage(): Boolean = viewModel.hasNextPage

    override fun hasPrevPage(): Boolean = viewModel.hasPrevPage

    @CallSuper
    open fun onEventContentChanged(content: Resource<BaseTopicListDataPage>) {
        when (content) {
            is Resource.Success -> {
                baseTopicListView.onSuccess(content)
                if (AppReview.schedule(requireView(), pref)) {
                    val activity = requireActivity()
                    if (activity is TouchEventDispatcher) {
                        activity.register(::onActivityTouchEvent)
                    }
                }
            }
            is Resource.Error -> baseTopicListView.onError(content)
            is Resource.Loading -> baseTopicListView.onLoading(content)

        }
        baseTopicListView.setPageAvailability(
            hasPrevPage = viewModel.hasPrevPage,
            hasNextPage = viewModel.hasNextPage
        )

        when {
            content is Resource.Loading -> {
                cancelRefreshJob()
                lastLoadTime = System.currentTimeMillis()

                refreshJob = viewLifecycleOwner.lifecycle.coroutineScope.launchWhenResumed {
                    delay(REFRESH_INTERVAL_MILLIS)
                    viewModel.refreshPage()
                }
            }
            refreshJob.let { it == null || !it.isActive } -> {
                refreshJob = viewLifecycleOwner.lifecycle.coroutineScope.launchWhenResumed {
                    val timeDiff = (System.currentTimeMillis() - lastLoadTime).coerceAtMost(REFRESH_INTERVAL_MILLIS)
                    delay(REFRESH_INTERVAL_MILLIS - timeDiff)
                    viewModel.refreshPage()
                }
            }
        }
    }

    private fun cancelRefreshJob() {
        when (val job = refreshJob) {
            is Job -> {
                if (job.isActive) {
                    job.cancel()
                }
            }
        }
    }

    private fun onActivityTouchEvent(e: MotionEvent) {
        if (e.action == MotionEvent.ACTION_DOWN) {
            val snackBar = AppReview.snackBar?.get() ?: return
            val rect = Rect()
            snackBar.view.getHitRect(rect)
            if (!rect.contains(e.x.toInt(), e.y.toInt())) {
                snackBar.dismiss()
            }
        }
    }

    override fun onTopicClicked(topic: Topic) = router.toTopic(topic, page = topic.linkedPage)


    override fun onMoreClicked() = router.toExplore()

    override fun onAuthorClicked(user: User) = router.toUser(user, preview = true)

    override fun loadNextPage() = viewModel.loadNextPage()

    override fun loadPrevPage() = viewModel.loadPrevPage()

    override fun refreshPage() = viewModel.refreshPage()

    override fun loadFirstPage() = viewModel.loadFirstPage()

    override fun loadLastPage() = viewModel.loadLastPage()

    override fun retryLastRequest() = viewModel.retry()

    override fun newTopic() {
        when {
            pref.isLoggedOut -> router.toAccountList()
            topicList is Board -> router.toNewTopic(topicList as Board)
            else -> router.toNewTopic()
        }
    }


    override fun onBoardClicked(board: Board) = router.toBoard(board)

    override fun hasViewedTopic(topic: Topic): Boolean =
        pref.markReadTopics && viewModel.hasViewedTopic(topic)


    override fun changePageOrSort() {
        val dataPage = viewModel.dataPage ?: return
        router.toTopicListPageSelection(topicList, dataPage)
    }

    override fun onSortSelected(sort: Sort) = viewModel.sortBy(sort)


    companion object {
        const val TOPIC_LIST = "topicList"
        const val BUNDLE_RECYCLER_VIEW_STATE = "firstVisibleTopic"
        private const val LAST_LOAD_TIME = "last_load_time"
        private const val PAGE = "page"
        private const val REFRESH_INTERVAL_MILLIS = 1_000L * 60 * 7

        fun newBundle(topicList: TopicList, page: Int = 0) =
            bundleOf(
                TOPIC_LIST to topicList,
                PAGE to page
            )
    }
}
