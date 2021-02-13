package com.amebo.amebo.screens.postlist.components

import android.os.Parcelable
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amebo.amebo.R
import com.amebo.amebo.common.CenterSmoothScroller
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.dividerDrawable
import com.amebo.amebo.common.extensions.getMessage
import com.amebo.amebo.common.extensions.getPostListTitle
import com.amebo.amebo.databinding.PostListScreenBinding
import com.amebo.amebo.screens.postlist.PostListMeta
import com.amebo.amebo.screens.postlist.adapters.AltAdapter
import com.amebo.amebo.screens.postlist.adapters.HeaderAdapter
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.core.domain.PostList
import com.amebo.core.domain.PostListDataPage
import com.amebo.core.domain.TopicPostListDataPage
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference

open class PostListView(
    fragment: Fragment,
    contentAdapter: ItemAdapter,
    swipeRefreshLayout: SwipeRefreshLayout,
    recyclerView: RecyclerView,
    toolbar: Toolbar,
    nextPage: View,
    prevPage: View,
    refreshPage: View,
    showContext: View,
    protected val postList: PostList,
    protected val listener: IPostListView.Listener
) : IPostListView {


    constructor(
        fragment: Fragment,
        binding: PostListScreenBinding,
        contentAdapter: ItemAdapter,
        postList: PostList,
        listener: IPostListView.Listener
    ) : this(
        fragment,
        contentAdapter,
        binding.swipeRefreshLayout,
        binding.recyclerView,
        binding.toolbar,
        binding.bottomBar.btnNextPage,
        binding.bottomBar.btnPrevPage,
        binding.bottomBar.btnRefreshPage,
        binding.bottomBar.btnMore,
        postList, listener
    )

    private val rootViewRef = WeakReference(fragment.view)
    private val swipeRefreshLayoutRef = WeakReference(swipeRefreshLayout)
    private val recyclerViewRef = WeakReference(recyclerView)
    private val toolbarRef = WeakReference(toolbar)
    protected val toolbar get() = toolbarRef.get()!!
    private val nextPageRef = WeakReference(nextPage)
    private val prevPageRef = WeakReference(prevPage)
    private val refreshPageRef = WeakReference(refreshPage)
    private val showContextRef = WeakReference(showContext)

    protected val rootView get() = rootViewRef.get()!!
    protected val swipeRefreshLayout get() = swipeRefreshLayoutRef.get()!!
    protected val recyclerView get() = recyclerViewRef.get()!!
    protected val nextPage get() = nextPageRef.get()!!
    protected val prevPage get() = prevPageRef.get()!!
    protected val refreshPage get() = refreshPageRef.get()!!
    protected val showContext get() = showContextRef.get()!!

    private var _contentAdapter: ItemAdapter? = contentAdapter

    private val contentAdapter get() = _contentAdapter!!

    private val altAdapter = AltAdapter(listener)
    private val headerAdapter = HeaderAdapter(postList, listener)
    private val concatAdapter = ConcatAdapter(headerAdapter, contentAdapter, altAdapter)
    private val scroller = CenterSmoothScroller(recyclerView.context)

    private var resetScroll = false

    init {
        nextPage.setOnClickListener { listener.onNextClicked() }
        nextPage.setOnLongClickListener { listener.onLastClicked(); true }

        prevPage.setOnClickListener { listener.onPrevClicked() }
        prevPage.setOnLongClickListener { listener.onFirstClicked(); true }

        refreshPage.setOnClickListener { listener.onRefreshTriggered() }
        showContext.setOnClickListener(listener::onMoreClicked)
        toolbar.setNavigationOnClickListener { listener.onNavigationClicked() }
        toolbar.setOnClickListener { scrollToPosition(0) }

        swipeRefreshLayout.setOnRefreshListener { listener.onRefreshTriggered() }

        val context = toolbar.context
        toolbar.post {
            setTitle(postList.getPostListTitle(context))
        }

        recyclerView.dividerDrawable(R.drawable.divider)
        recyclerView.adapter = concatAdapter
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when(newState){
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        resetScroll = false
                    }
                }
            }
        })
    }


    override fun onLoading(loading: Resource.Loading<PostListDataPage>) {
        resetScroll = true
        val isFullLoad = loading.content?.data.isNullOrEmpty()
        if (isFullLoad) {
            contentAdapter.clear()
            scrollToPosition(0)
            altAdapter.setLoading()
            swipeRefreshLayout.isRefreshing = false
        } else {
            if (contentAdapter.postListDataPage != null && contentAdapter.postListDataPage!!.page != loading.content!!.page) {
                contentAdapter.clear()
                scrollToPosition(0)
            }
            setContent(loading.content!!)
            swipeRefreshLayout.isRefreshing = true
        }
    }

    override fun onSuccess(success: Resource.Success<PostListDataPage>) {
        val scrollTo =
            if (success.content is TopicPostListDataPage &&
                success.content.page == success.content.topic.linkedPage &&
                success.content.topic.refPost != null &&
                (contentAdapter.postListDataPage == null || contentAdapter.postListDataPage!!.data.isNotEmpty())
            ) {
                success.content.topic.refPost
            } else {
                null
            }
        swipeRefreshLayout.isRefreshing = false
        setContent(success.content)

        if (listener.shouldHighlightPost && scrollTo != null) {
            recyclerView.postDelayed({
                val idx = contentAdapter.findPostPosition(scrollTo)
                if (idx != -1) {
                    scrollToPosition(idx + headerAdapter.itemCount, smoothScroll = true)
                    contentAdapter.highlightItem(idx)
                }
            }, 0)
        } else if (resetScroll) {
            scrollToPosition(0)
        }
        resetScroll = false
    }

    override fun onError(state: Resource.Error<PostListDataPage>) {
        swipeRefreshLayout.isRefreshing = false
        when (val dataPage = state.content) {
            null -> {
                scrollToPosition(0)
                altAdapter.setError(state.cause)
            }
            else -> {
                Snackbar.make(
                    rootView,
                    state.cause.getMessage(rootView.context),
                    Snackbar.LENGTH_LONG
                )
                    .show()
                setContent(dataPage)
            }
        }
    }


    override fun scrollToPost(postId: String) {
        val position = contentAdapter.findPostPosition(postId)
        if (position != -1) {
            scrollToPosition(position + headerAdapter.itemCount, smoothScroll = true)
            contentAdapter.highlightItem(position)
        } else {
            listener.viewPost(postId)
        }
    }

    override fun saveState(): Parcelable {
        return recyclerView.layoutManager!!.onSaveInstanceState()!!
    }

    override fun restoreState(
        parcelable: Parcelable,
        lastImagePosition: Int?,
        lastPostPosition: Int?
    ) {
        val manager = recyclerView.layoutManager!!
        manager.onRestoreInstanceState(parcelable)
        if (lastImagePosition != null && lastPostPosition != null) {
            contentAdapter.scrollToImageAt(
                recyclerView,
                lastPostPosition + headerAdapter.itemCount,
                lastImagePosition
            )
        }
    }

    override fun setTitle(title: String) {
        toolbar.title = title
    }


    override fun setPostListMeta(meta: PostListMeta) {
        toolbar.subtitle = meta.toString(toolbar.context)
    }


    override fun setHasNextPage(hasNextPage: Boolean) {
        nextPage.isEnabled = hasNextPage
    }

    override fun setHasPrevPage(hasPrevPage: Boolean) {
        prevPage.isEnabled = hasPrevPage
    }


    protected open fun setContent(dataPage: PostListDataPage) {
        headerAdapter.setData(dataPage)
        contentAdapter.postListDataPage = dataPage
        altAdapter.clear(isEmpty = dataPage.data.isEmpty())
    }

    override fun expandAllPosts() {
        contentAdapter.expandAllPosts()
    }

    override fun collapseAllPosts() {
        contentAdapter.collapseAllPosts()
    }


    override fun collapsePostAt(postPosition: Int) {
        contentAdapter.collapsePost(postPosition)
    }

    open fun scrollToPosition(position: Int = 0, smoothScroll: Boolean = false) {
        recyclerView.postDelayed({
            if (smoothScroll) {
                scroller.targetPosition = position
                recyclerView.layoutManager!!.startSmoothScroll(scroller)
            } else {
                recyclerView.scrollToPosition(position)
            }
        }, 100)
    }
}

