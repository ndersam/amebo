package com.amebo.amebo.screens.postlist

import com.amebo.core.domain.SimplePost


interface BasePostListViewModel<T : Any> {
    val hasNextPage: Boolean
    val hasPrevPage: Boolean
    val collapsedItems: MutableSet<Int>

    /**
     * Map of Post-position to current image recyclerView position
     */
    val currentImageRecyclerViewPosition: MutableMap<Int, Int>
    fun likePost(post: SimplePost, like: Boolean)
    fun sharePost(post: SimplePost, share: Boolean)
    fun retry()
    fun loadNextPage()
    fun loadPrevPage()
    fun loadFirstPage()
    fun loadLastPage()
    fun refreshPage()
    fun loadPage(page: Int)
    fun initialize(identifier: T, initialPage: Int)
    fun cancelLoading()
}