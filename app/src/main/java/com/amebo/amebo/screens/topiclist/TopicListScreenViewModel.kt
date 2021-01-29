package com.amebo.amebo.screens.topiclist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.R
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.toResource
import com.amebo.core.Nairaland
import com.amebo.core.domain.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

class TopicListScreenViewModel @Inject constructor(
    private val nairaland: Nairaland,
    application: Application
) : AndroidViewModel(application), Pref.Observer {

    private var _dataPageEvent = MutableLiveData<Event<Resource<BaseTopicListDataPage>>>()
    val dataPageEvent: LiveData<Event<Resource<BaseTopicListDataPage>>> = _dataPageEvent

    private var viewedTopics = mutableSetOf<Int>()
    private val _viewedTopicsLoadedEvent = MutableLiveData<Event<Unit>>()
    val viewedTopicsLoadedEvent: LiveData<Event<Unit>> = _viewedTopicsLoadedEvent

    private val _topicListMetaEvent = MutableLiveData<Event<TopicListMeta>>()
    val topicListMetaEvent: LiveData<Event<TopicListMeta>> = _topicListMetaEvent

    private val _followingBoardEvent = MutableLiveData<Event<Resource<Pair<Board, Boolean>>>>()
    val followingBoardEvent: LiveData<Event<Resource<Pair<Board, Boolean>>>> = _followingBoardEvent

    private val _unFollowTopicEvent = MutableLiveData<Event<Resource<Topic>>>()
    val unFollowTopicEvent: LiveData<Event<Resource<Topic>>> = _unFollowTopicEvent


    private val prefObservable = Pref.Observable().apply {
        subscribe(
            getApplication(),
            this@TopicListScreenViewModel,
            R.string.key_current_user,
            R.string.key_clear_read_topics
        )
    }


    private var job: Job? = null


    private lateinit var topicList: TopicList


    private var request: Request? = null
        set(value) {
            field = value
            if (value != null) {
                handleRequest(value)
            }
        }

    // Paging stats
    private val manager = DataManager()

    val dataPage: BaseTopicListDataPage? get() = manager.dataPage

    val sort: Sort? get() = manager.sort

    val hasNextPage: Boolean get() = manager.hasNextPage

    val hasPrevPage: Boolean get() = manager.hasPrevPage


    override fun onCleared() {
        super.onCleared()
        prefObservable.unsubscribe(getApplication())
    }

    override fun onPreferenceChanged(key: Int, contextChanged: Boolean) {
        when (key) {
            R.string.key_current_user -> {
                manager.reset()
            }
            R.string.key_clear_read_topics -> {
                viewedTopics.clear()
                _viewedTopicsLoadedEvent.value = Event(Unit)
            }
        }
    }

    fun loadNextPage() {
        request = Request(manager.next, sort = manager.sort)
    }

    fun loadPrevPage() {
        request = Request(manager.previous, sort = manager.sort)
    }

    fun refreshPage() {
        request = Request(manager.current, sort = manager.sort)
    }

    fun loadFirstPage() {
        request = Request(0, manager.sort)
    }

    fun loadLastPage() {
        request = Request(manager.last!!, manager.sort)
    }

    fun retry() {
        checkNotNull(request)
        request = request
    }


    fun initialize(
        topicList: TopicList, sort: Sort?, page: Int = 0
    ) {
        this.topicList = topicList
        viewModelScope.launch {
            viewedTopics = nairaland.sources.postLists.allViewedTopicIds()
            _viewedTopicsLoadedEvent.value = Event(Unit)
        }

        when (val dataPage = manager.dataPage) {
            null -> loadPage(page, sort)
            else -> {
                postMeta()
                _dataPageEvent.value = Event(Resource.Success(dataPage))
            }
        }
    }

    fun sortBy(sort: Sort) {
        val request = request ?: return
        this.request = Request(request.page, sort)
    }

    fun unFollowTopic(topic: Topic) {
        viewModelScope.launch {
            _unFollowTopicEvent.value = Event(Resource.Loading())
            val resource =
                nairaland.sources.submissions.unFollowTopic(topic).toResource(manager.dataPage)
            if (resource is Resource.Success) {
                manager.update(resource.content)
            }
            postMeta()
            _dataPageEvent.value = Event(resource)
            _unFollowTopicEvent.value = Event(
                when (resource) {
                    is Resource.Success -> Resource.Success(topic)
                    is Resource.Error -> Resource.Error(resource.cause, topic)
                    else -> return@launch // IllegalState
                }
            )
        }
    }


    fun loadPage(page: Int, sort: Sort?) {
        request = Request(page, sort)
    }

    fun loadPage(page: Int) {
        val request = request ?: return
        this.request = Request(page, request.sort)
    }

    fun unFollowBoard(board: Board) {
        val dataPage = manager.dataPage as FollowedBoardsDataPage
        _followingBoardEvent.value = Event(Resource.Loading())

        viewModelScope.launch {
            when (val result = nairaland.sources.submissions.unFollowBoard(dataPage, board)) {
                is ResultWrapper.Success -> {
                    manager.update(result.data)
                    postMeta()
                    _dataPageEvent.value = Event(result.toResource(manager.dataPage))
                    _followingBoardEvent.value = Event(Resource.Success(board to false))
                }
                is ResultWrapper.Failure -> {
                    _followingBoardEvent.value = Event(Resource.Error(result.data))
                }
            }
        }
    }

    fun setData(data: TopicListDataPage) {
        if (job?.isActive == true) {
            job?.cancel()
        }
        manager.update(data)
        _dataPageEvent.value = Event(Resource.Success(data))
        postMeta()
    }

    fun toggleBoardFollowing() {
        val board = topicList as? Board ?: return
        val dataPage = manager.dataPage as BoardsDataPage
        val follow = !dataPage.isFollowing

        _followingBoardEvent.value = Event(Resource.Loading())

        viewModelScope.launch {
            val result = if (follow) {
                nairaland.sources.submissions.followBoard(dataPage)
            } else {
                nairaland.sources.submissions.unFollowBoard(dataPage)
            }
            when (result) {
                is ResultWrapper.Success -> {
                    manager.update(result.data)
                    postMeta()
                    _dataPageEvent.value = Event(result.toResource(manager.dataPage))
                    _followingBoardEvent.value = Event(Resource.Success(board to follow))
                }
                is ResultWrapper.Failure -> {
                    _followingBoardEvent.value = Event(Resource.Error(result.data))
                }
            }
        }
    }


    private fun indicateLoading() {
        _dataPageEvent.value = Event(Resource.Loading(manager.dataPage))
        _topicListMetaEvent.value = Event(manager.meta)
    }

    private fun postMeta() {
        _topicListMetaEvent.value = Event(manager.meta)
    }

    fun hasViewedTopic(topic: Topic): Boolean = viewedTopics.contains(topic.id)


    private fun handleRequest(request: Request) {
        if (job?.isActive == true) {
            job?.cancel()
        }

        job = viewModelScope.launch {
            manager.clearIfNotRefresh(request)

            // fetch data from cache and update manager
            if (manager.isLoadFromCache(request)) {
                manager.update(
                    nairaland.sources.topicLists.fetchCached(
                        topicList, request.page, sort
                    )
                )
            }

            indicateLoading()

            val result = nairaland.sources.topicLists.fetch(topicList, request.page, request.sort)
            if (result is ResultWrapper.Success) {
                manager.update(result.data)
                postMeta()
            }
            _dataPageEvent.value = Event(result.toResource(manager.dataPage))
        }
    }

    /**
     * Keeps track of the current page visited, and the last page number (highest known page number)
     * recorded.
     */
    private class DataManager(
        var current: Int = 0,
        var last: Int? = null,
        var sort: Sort? = null,
        var dataPage: BaseTopicListDataPage? = null
    ) {
        val previous: Int get() = current - 1

        val next: Int get() = current + 1

        val meta get() = TopicListMeta(page = current, lastPage = last, sort = sort)

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
                this.sort = request.sort
            }
        }

        fun reset() {
            current = 0
            dataPage = null
            last = null
        }


        fun update(dataPage: BaseTopicListDataPage?) {
            this.dataPage = dataPage
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
            return request.page == this.current && request.sort == this.sort
        }

        fun isLoadFromCache(request: Request): Boolean {
            return !isRefresh(request) || dataPage == null
        }
    }

    /**
     * Models a simple page request
     */
    private class Request(val page: Int, val sort: Sort?)
}

