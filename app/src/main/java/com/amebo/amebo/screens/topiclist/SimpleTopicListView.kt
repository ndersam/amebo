package com.amebo.amebo.screens.topiclist

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Parcelable
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amebo.amebo.R
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.dividerDrawable
import com.amebo.amebo.common.extensions.getMessage
import com.amebo.amebo.common.extensions.getTitle
import com.amebo.amebo.databinding.TopicListScreenBinding
import com.amebo.amebo.screens.topiclist.adapters.AltAdapter
import com.amebo.amebo.screens.topiclist.adapters.HeaderAdapter
import com.amebo.amebo.screens.topiclist.adapters.ItemAdapter
import com.amebo.core.domain.BaseTopicListDataPage
import com.amebo.core.domain.Sort
import com.amebo.core.domain.TopicList
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.lang.ref.WeakReference

open class SimpleTopicListView(
    val topicList: TopicList,
    private var sort: Sort?,
    private val listener: BaseTopicListView.Listener,
    recyclerView: RecyclerView,
    swipeRefreshLayout: SwipeRefreshLayout,
    txtTitle: TextView,
    txtPageInfo: TextView,
    btnNextPage: View,
    btnPrevPage: View,
    btnRefreshPage: View,
    btnMore: View,
    btnNewTopic: View,
    toolbar: Toolbar,
) : BaseTopicListView {

    constructor(
        topicList: TopicList,
        sort: Sort?,
        listener: BaseTopicListView.Listener,
        binding: TopicListScreenBinding
    ) : this(
        topicList = topicList,
        sort = sort,
        listener = listener,
        recyclerView = binding.recyclerView,
        swipeRefreshLayout = binding.swipeRefreshLayout,
        txtTitle = binding.title,
        txtPageInfo = binding.txtPageInfo,
        btnNextPage = binding.btnNextPage,
        btnPrevPage = binding.btnPrevPage,
        btnRefreshPage = binding.btnRefreshPage,
        btnMore = binding.btnMore,
        btnNewTopic = binding.btnNewTopic,
        toolbar = binding.toolbar
    )

    private val recyclerViewRef = WeakReference(recyclerView)
    private val recyclerView: RecyclerView get() = recyclerViewRef.get()!!

    private val swipeRefreshLayoutRef = WeakReference(swipeRefreshLayout)
    private val swipeRefreshLayout: SwipeRefreshLayout get() = swipeRefreshLayoutRef.get()!!

    private val btnNextPageRef = WeakReference(btnNextPage)
    private val btnNextPage: View get() = btnNextPageRef.get()!!

    private val btnPrevPageRef = WeakReference(btnPrevPage)
    private val btnPrevPage: View get() = btnPrevPageRef.get()!!

    private val btnRefreshPageRef = WeakReference(btnRefreshPage)
    private val btnRefreshPage: View get() = btnRefreshPageRef.get()!!


    private val btnMoreRef = WeakReference(btnMore)
    private val btnMore: View get() = btnMoreRef.get()!!

    private val btnNewTopicRef = WeakReference(btnNewTopic)
    private val btnNewTopic: View get() = btnNewTopicRef.get()!!

    private val txtPageInfoRef = WeakReference(txtPageInfo)
    private val txtPageInfo get() = txtPageInfoRef.get()!!


    private val context get() = swipeRefreshLayout.context

    private val headerAdapter = HeaderAdapter(topicList, sort, listener)
    val itemAdapter = ItemAdapter(listener, topicList)
    private val altAdapter = AltAdapter(listener)

    private val adapter = ConcatAdapter(headerAdapter, altAdapter, itemAdapter)
    private var ignoreSavedState = false
    private var resetScroll = false

    init {
        recyclerView.adapter = adapter
        recyclerView.dividerDrawable(R.drawable.divider, Color.TRANSPARENT)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        resetScroll = false
                    }
                }
            }
        })

        swipeRefreshLayout.setOnRefreshListener { listener.refreshPage() }
        btnPrevPage.setOnClickListener { listener.loadPrevPage() }
        btnPrevPage.setOnLongClickListener { listener.loadFirstPage(); true }
        btnNextPage.setOnClickListener { listener.loadNextPage() }
        btnNextPage.setOnLongClickListener { listener.loadLastPage(); true }
        btnRefreshPage.setOnClickListener { listener.refreshPage() }
        btnMore.setOnClickListener { listener.onMoreClicked() }
        btnNewTopic.setOnClickListener { listener.newTopic() }

        toolbar.setOnClickListener { recyclerView.scrollToPosition(0) }
        txtTitle.text = topicList.getTitle(context)
        txtPageInfo.setOnClickListener { listener.changePageOrSort() }
    }

    override fun restoreLayoutState(parcelable: Parcelable) {
        if (ignoreSavedState) {
            ignoreSavedState = false
            return
        }
        recyclerView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                recyclerView.removeOnLayoutChangeListener(this)
                recyclerView.layoutManager!!.onRestoreInstanceState(parcelable)
            }
        })
    }

    override fun saveLayoutState() = recyclerView.layoutManager?.onSaveInstanceState()


    override fun onSuccess(success: Resource.Success<BaseTopicListDataPage>) {
        val page = success.content
        swipeRefreshLayout.isRefreshing = false
        setContent(page)
        if (resetScroll) {
            resetScroll = false
            recyclerView.postDelayed({
                recyclerView.scrollToPosition(0)
            }, 200)
        }
    }

    override fun onError(error: Resource.Error<BaseTopicListDataPage>) {
        swipeRefreshLayout.isRefreshing = false

        if (error.content == null) {
            recyclerView.scrollToPosition(0)
            itemAdapter.clear()
            altAdapter.setError(error.cause)
        } else {
            Snackbar.make(recyclerView, error.cause.getMessage(context), Snackbar.LENGTH_LONG)
                .show()
            setContent(error.content)
        }
    }


    override fun onLoading(loading: Resource.Loading<BaseTopicListDataPage>) {
        ignoreSavedState = true
        resetScroll = true
        val isFullLoad = loading.content?.data.isNullOrEmpty()
        if (isFullLoad) {
            itemAdapter.clear()
            altAdapter.setLoading()
            recyclerView.scrollToPosition(0)
            swipeRefreshLayout.isRefreshing = false
        } else {
            if (itemAdapter.page != null && itemAdapter.page!!.page != loading.content!!.page) {
                itemAdapter.clear()
                recyclerView.scrollToPosition(0)
            }
            setContent(loading.content!!)
            swipeRefreshLayout.isRefreshing = true
        }
    }

    override fun setPageAvailability(hasPrevPage: Boolean, hasNextPage: Boolean) {
        btnPrevPage.isEnabled = hasPrevPage
        btnNextPage.isEnabled = hasNextPage
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewedTopicsLoaded() {
        adapter.notifyDataSetChanged()
    }

    override fun onTopicListMetaChanged(meta: TopicListMeta) {
        when (val last = if (meta.lastPage != null) (meta.lastPage + 1).toString() else null) {
            is String -> {
                val current = (meta.page + 1).toString()
                txtPageInfo.text = context.getString(R.string.page_x_of_x, current, last)
            }
            else -> {
                val current = meta.page + 1
                txtPageInfo.text = context.getString(R.string.page_x, current)
            }
        }
        headerAdapter.setItems(meta.sort)
    }


    protected open fun setContent(page: BaseTopicListDataPage) {
        itemAdapter.page = page
        altAdapter.clear(isEmpty = page.data.isEmpty())
    }

}