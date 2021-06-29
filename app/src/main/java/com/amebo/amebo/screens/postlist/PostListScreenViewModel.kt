package com.amebo.amebo.screens.postlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.R
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Optional
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.toResource
import com.amebo.core.Nairaland
import com.amebo.core.common.Values
import com.amebo.core.domain.*
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max

abstract class PostListScreenViewModel<T : PostList>(
    protected val nairaland: Nairaland,
    private val pref: Pref,
    application: Application
) : AndroidViewModel(application),
    BasePostListViewModel<T>, Pref.Observer {

    private val prefObservable = Pref.Observable().apply {
        subscribe(getApplication(), this@PostListScreenViewModel, R.string.key_current_user)
    }
    private var job: Job? = null

    protected lateinit var postList: T

    protected val dataPage get() = manager.dataPage
    private val _dataEvent = MutableLiveData<Event<Resource<PostListDataPage>>>()
    val dataEvent: LiveData<Event<Resource<PostListDataPage>>> = _dataEvent

    private val _metaEvent = MutableLiveData<Event<PostListMeta>>()
    val metaEvent: LiveData<Event<PostListMeta>> = _metaEvent

    private val _unknownUriResultEvent =
        MutableLiveData<Event<Pair<String, Optional<IntentParseResult>>>>()
    val unknownUriResultEvent: LiveData<Event<Pair<String, Optional<IntentParseResult>>>> =
        _unknownUriResultEvent


    private val manager = DataManager()

    val currentPage get() = manager.current

    val lastPage get() = manager.last

    override val collapsedItems get() = manager.collapsedItems

    override val currentImageRecyclerViewPosition get() = manager.collapsedImageItems

    override val hasNextPage: Boolean get() = manager.hasNextPage

    override val hasPrevPage: Boolean get() = manager.hasPrevPage

    private var request: Request? = null
        set(value) {
            field = value
            if (value != null) {
                handleRequest(value)
            }
        }

    open suspend fun onDataPageFetched(result: Result<PostListDataPage, ErrorResponse>) {
        if (result is Ok) {
            manager.update(result.value)
            postMeta()
        }
        // post value to observer
        _dataEvent.value = Event(result.toResource(manager.dataPage))
    }

    private fun postMeta(current: Int, last: Int?) {
        _metaEvent.value = Event(PostListMeta(current, last))
    }

    private fun postMeta() {
        _metaEvent.value = Event(manager.meta)
    }

    override fun onCleared() {
        super.onCleared()
        prefObservable.unsubscribe(getApplication())
    }

    override fun onPreferenceChanged(key: Int, contextChanged: Boolean) {
        if (key == R.string.key_current_user) {
            manager.reset()
        }
    }

    fun setData(postListDataPage: PostListDataPage) {
        cancelLoading()
        manager.update(postListDataPage)
        _dataEvent.value = Event(Resource.Success(postListDataPage))
        postMeta()
    }

    fun handleUnknownUrl(url: String) {
        viewModelScope.launch {
            if (url.startsWith(Values.URL) || url.startsWith("/") || url.startsWith("www.nairaland.com") || url.startsWith(
                    "http://www.nairaland.com"
                )
            ) {
                val result = nairaland.sources.misc.parseIntent(url)
                _unknownUriResultEvent.value = Event(url to Optional(result))
            } else {
                _unknownUriResultEvent.value = Event(url to Optional(null))
            }
        }
    }

    protected fun setResult(result: Result<PostListDataPage, ErrorResponse>) {
        if (result is Ok) {
            manager.update(result.value)
        }
        _dataEvent.value = Event(result.toResource(manager.dataPage))
        postMeta()
    }


    override fun loadNextPage() {
        request = Request(manager.next)
    }

    override fun loadPrevPage() {
        request = Request(manager.previous)
    }

    override fun loadFirstPage() {
        request = Request(0)
    }

    override fun loadLastPage() {
        request = Request(manager.last!!)
    }

    override fun refreshPage() {
        request = Request(manager.current)
    }

    override fun loadPage(page: Int) {
        request = Request(page)
    }

    override fun retry() {
        checkNotNull(request)
        request = request
    }

    override fun initialize(
        identifier: T,
        initialPage: Int
    ) {
        this.postList = identifier
        when (val data = manager.dataPage) {
            null -> loadPage(initialPage)
            else -> {
                when (val metaEvent = _metaEvent.value) {
                    is Event -> {
                        _metaEvent.value = Event(metaEvent.peekContent())
                    }
                }
                _dataEvent.value = Event(Resource.Success(data))
            }
        }
    }


    override fun cancelLoading() {
        if (job?.isActive == true) {
            job?.cancel()
        }
    }

    override fun likePost(post: SimplePost, like: Boolean) {
        viewModelScope.launch {
            val result = if (like)
                nairaland.sources.submissions.likePost(post)
            else
                nairaland.sources.submissions.unLikePost(post)
            if (result is Ok) {
                manager.update(result.value)
            }
            _dataEvent.value = Event(result.toResource(manager.dataPage))
        }
    }

    override fun sharePost(post: SimplePost, share: Boolean) {
        viewModelScope.launch {
            val result = if (share)
                nairaland.sources.submissions.sharePost(post)
            else
                nairaland.sources.submissions.unSharePost(post)
            if (result is Ok) {
                manager.update(result.value)
            }
            _dataEvent.value = Event(result.toResource(manager.dataPage))
        }
    }


    private fun handleRequest(request: Request) {
        cancelLoading()

        job = viewModelScope.launch {
            manager.clearIfNotRefresh(request)

            if (manager.isLoadFromCache(request)) {
                manager.update(
                    nairaland.sources.postLists.fetchCached(postList, request.page)
                )
            }

            indicateLoading()
            val result = nairaland.sources.postLists.fetch(postList, request.page)
            onDataPageFetched(result)
        }
    }

    /**
     * Make sure to update the contents of [DataManager] before calling this method
     */
    private fun indicateLoading() {
        _dataEvent.value = Event(
            Resource.Loading(manager.dataPage)
        )
        postMeta()
    }

    /**
     * Keeps track of the current page visited, and the last page number (highest known page number)
     * recorded.
     */
    private class DataManager(
        var current: Int = 0,
        var last: Int? = null,
        var dataPage: PostListDataPage? = null,
        val collapsedItems: MutableSet<Int> = mutableSetOf(),
        val collapsedImageItems: MutableMap<Int, Int> = mutableMapOf()
    ) {
        val previous: Int get() = current - 1

        val next: Int get() = current + 1

        val meta get() = PostListMeta(currentPage = current, lastPage = last)

        val hasNextPage: Boolean
            get() = when (val last = last) {
                null -> false
                else -> current < last
            }

        val hasPrevPage: Boolean
            get() = when (last) {
                null -> false
                else -> current > 0
            }

        fun clearIfNotRefresh(request: Request) {
            if (!isRefresh(request)) {
                this.current = request.page
                this.dataPage = null
                this.collapsedItems.clear()
                this.collapsedImageItems.clear()
            }
        }

        fun reset() {
            current = 0
            dataPage = null
            last = null
            collapsedItems.clear()
            collapsedImageItems.clear()
        }


        fun update(dataPage: PostListDataPage?) {
            this.dataPage = dataPage
            collapsedItems.clear()
            collapsedImageItems.clear()
            if (dataPage != null) {
                this.current = dataPage.page
                this.last = when (val last = this.last) {
                    null -> dataPage.last
                    else -> max(last, dataPage.last)
                }
            }
        }

        /**
         * @return true if we are refreshing the content of an existing page
         */
        fun isRefresh(request: Request): Boolean {
            return request.page == this.current
        }

        fun isLoadFromCache(request: Request): Boolean {
            return !isRefresh(request) || dataPage == null
        }
    }

    private class Request(val page: Int)
}
