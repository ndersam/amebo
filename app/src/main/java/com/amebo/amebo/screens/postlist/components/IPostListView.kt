package com.amebo.amebo.screens.postlist.components

import android.os.Parcelable
import android.view.View
import com.amebo.amebo.common.Resource
import com.amebo.amebo.screens.postlist.PostListMeta
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.core.domain.PostListDataPage

interface IPostListView {
    fun onLoading(loading: Resource.Loading<PostListDataPage>)
    fun onSuccess(success: Resource.Success<PostListDataPage>)
    fun onError(state: Resource.Error<PostListDataPage>)
    fun setTitle(title: String)
    fun scrollToPost(postId: String)
    fun restoreState(parcelable: Parcelable, lastImagePosition: Int?, lastPostPosition: Int?)
    fun saveState(): Parcelable
    fun expandAllPosts()
    fun collapseAllPosts()
    fun collapsePostAt(postPosition: Int)
    fun setHasNextPage(hasNextPage: Boolean)
    fun setHasPrevPage(hasPrevPage: Boolean)
    fun setPostListMeta(meta: PostListMeta)

    interface Listener : PostListAdapterListener {
        val shouldHighlightPost: Boolean
        fun onNextClicked()
        fun onPrevClicked()
        fun onFirstClicked()
        fun onLastClicked()
        fun onMoreClicked(view: View)
        fun onNavigationClicked()
        fun onRefreshTriggered()
        fun viewPost(postId: String)
    }

}