package com.amebo.amebo.screens.postlist.adapters

import android.view.View
import androidx.lifecycle.Lifecycle
import com.amebo.amebo.screens.postlist.adapters.image.ImageLoadingListener
import com.amebo.core.domain.Board
import com.amebo.core.domain.SimplePost
import com.amebo.core.domain.Topic
import com.amebo.core.domain.User

interface PostListAdapterListener : AltAdapter.Listener, HeaderAdapter.Listener {
    val listenerLifecycle: Lifecycle
    fun hasNextPage(): Boolean
    fun nextPage()
    fun likePost(post: SimplePost, like: Boolean)
    fun sharePost(post: SimplePost, share: Boolean)
    fun showContextMenuOnPost(post: SimplePost, postPosition: Int, anchor: View)
    fun replyPost(post: SimplePost)
    fun onUserClicked(user: User)
    fun onPostTopicClick(topic: Topic, view: View)
    fun onTopicLinkClick(topic: Topic)
    override fun onBoardClicked(board: Board)
    fun onImageClicked(imageView: View, position: Int, images: List<String>, postPosition: Int)
    fun onImageClicked(textView: View, postPosition: Int, url: String)
    val imageLoadingListener: ImageLoadingListener
    fun onItemCollapsed(position: Int)
    fun isItemCollapsed(position: Int): Boolean
    fun onItemExpanded(position: Int)
    fun onYoutubeUrlClick(videoId: String)
    fun onReferencedPostClick(
        view: View,
        postPosition: Int,
        postID: String,
        author: String
    )

    fun onPostLinkClick(view: View, postPosition: Int, postID: String)
    fun onUnknownLinkClick(url: String)
}