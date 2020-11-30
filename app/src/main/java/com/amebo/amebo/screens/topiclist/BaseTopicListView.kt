package com.amebo.amebo.screens.topiclist

import android.os.Parcelable
import com.amebo.amebo.common.Resource
import com.amebo.amebo.screens.topiclist.adapters.AltAdapter
import com.amebo.amebo.screens.topiclist.adapters.HeaderAdapter
import com.amebo.amebo.screens.topiclist.adapters.TopicListAdapterListener
import com.amebo.core.domain.BaseTopicListDataPage
import com.amebo.core.domain.Board

interface BaseTopicListView {
    fun restoreLayoutState(parcelable: Parcelable)
    fun saveLayoutState(): Parcelable?
    fun onSuccess(success: Resource.Success<BaseTopicListDataPage>)
    fun onError(error: Resource.Error<BaseTopicListDataPage>)
    fun onLoading(loading: Resource.Loading<BaseTopicListDataPage>)
    fun setPageAvailability(hasPrevPage: Boolean, hasNextPage: Boolean)
    fun onViewedTopicsLoaded()
    fun onTopicListMetaChanged(meta: TopicListMeta)


    interface Listener : TopicListAdapterListener, HeaderAdapter.Listener, AltAdapter.Listener {
        fun refreshPage()
        override fun loadPrevPage()
        override fun loadFirstPage()
        override fun loadNextPage()
        override fun loadLastPage()
        fun newTopic()
        override fun onBoardClicked(board: Board)
        fun onMoreClicked()
        fun retryLastRequest()
        override fun onRetryClicked() = retryLastRequest()
        fun changePageOrSort()
    }
}