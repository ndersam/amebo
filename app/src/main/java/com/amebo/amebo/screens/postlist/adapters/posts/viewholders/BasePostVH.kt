package com.amebo.amebo.screens.postlist.adapters.posts.viewholders

import android.content.res.ColorStateList
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.common.*
import com.amebo.amebo.common.extensions.genderDrawable
import com.amebo.amebo.common.extensions.setDrawableEnd
import com.amebo.amebo.databinding.LayoutPostActionsBinding
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.image.ImageAdapter
import com.amebo.amebo.screens.postlist.components.RichPostTextView
import com.amebo.core.CoreUtils
import com.amebo.core.domain.SimplePost
import com.amebo.core.domain.Topic
import com.bumptech.glide.RequestManager
import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator

abstract class BasePostVH(
    private val body: RichPostTextView,
    val postAuthor: TextView,
    private val postTime: TextView,
    private val postActions: LayoutPostActionsBinding,
    val imageRecyclerView: RecyclerView,
    private val imageFrame: View,
    private val indicator: ScrollingPagerIndicator,
    private val requestManager: RequestManager,
    view: View,
    val listener: PostListAdapterListener,
    val pref: Pref
) :
    RecyclerView.ViewHolder(view) {

    private lateinit var post: SimplePost
    private var postPosition: Int = -1
    protected val theme = itemView.context.asTheme()


    private val handler = object : SpanHandler {
        override fun onImageClick(view: TextView, imagePosition: Int) =
            listener.onImageClicked(view, imagePosition, post.images, postPosition)

        override fun onImageClick(view: TextView, url: String) =
            listener.onImageClicked(view, postPosition, url)

        override fun onTopicLinkClick(view: TextView, topic: Topic) =
            listener.onTopicLinkClick(topic)

        override fun onYoutubeLinkClick(view: TextView, url: String) =
            listener.onYoutubeUrlClick(url)

        override fun onReferencedPostClick(
            view: TextView,
            postID: String,
            author: String
        ) = listener.onReferencedPostClick(body, postPosition, postID, author)

        override fun onPostLinkClick(view: TextView, postID: String) =
            listener.onPostLinkClick(view, postPosition, postID)

        override fun onUnknownLinkClick(url: String) = listener.onUnknownLinkClick(url)
    }

    private val imageAdapterListener = object :
        ImageAdapter.Listener {
        // Create only one instance of this listener per adapter.
        val imageLoadingListener = listener.imageLoadingListener

        override fun onClick(view: View, position: Int) =
            listener.onImageClicked(view, position, post.images, postPosition)

        override fun onClick(view: View, url: String) =
            listener.onImageClicked(view, postPosition, url)

        override fun onLoadCompleted(view: ImageView, imagePosition: Int) {
            imageLoadingListener.onLoadCompleted(view, postPosition, imagePosition)
        }
    }

    private val onLikeChangedListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        listener.likePost(post, isChecked)
        post.isLiked = isChecked
        if (post.isLiked) {
            post.likes++
        } else {
            post.likes--
        }
        setLikeCount()
    }

    private val onShareChangedListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        listener.sharePost(post, isChecked)
        post.isShared = isChecked
        if (post.isShared) {
            post.shares++
        } else {
            post.shares--
        }
        setShareCount()
    }

    init {
        PagerSnapHelper().attachToRecyclerView(imageRecyclerView)

        // AUTHOR
        postAuthor.setOnClickListener { listener.onUserClicked(post.author) }

        // ACTIONS
        postActions.replyPost.setOnClickListener {
            listener.replyPost(post)
        }
        postActions.moreActions.setOnClickListener {
            listener.showContextMenuOnPost(
                post,
                postPosition,
                it
            )
        }
    }

    open fun bind(
        post: SimplePost,
        postPosition: Int
    ) {
        this.post = post
        this.postPosition = postPosition

        // AUTHOR
        postAuthor.text = post.author.name
        postAuthor.setDrawableEnd(post.author.genderDrawable, useLineHeight = true)
        postTime.text = CoreUtils.howLongAgo(post.timestamp)

        // BODY
        body.setData(listener.listenerLifecycle, post, pref.useDeviceEmojis, handler)


        // IMAGES
        imageFrame.isGone = post.images.isEmpty()
        imageRecyclerView.adapter =
            ImageAdapter(
                post.images,
                requestManager,
                imageAdapterListener
            )
        indicator.attachToRecyclerView(imageRecyclerView)

        // ACTIONS
        postActions.replyPost.isEnabled = pref.isLoggedIn


        // likes
        setLikeCount()
        // apparently you cannot like your nairaland post
        postActions.chbxLike.isEnabled = pref.isLoggedIn && !pref.isCurrentAccount(post.author)
        // remove previously set one, to prevent it from firing
        postActions.chbxLike.setOnCheckedChangeListener(null)
        postActions.chbxLike.isChecked = post.isLiked
        postActions.chbxLike.setOnCheckedChangeListener(onLikeChangedListener)

        // shares
        setShareCount()
        // remove previously set one, to prevent it from firing
        postActions.chbxShare.setOnCheckedChangeListener(null)
        postActions.chbxShare.isEnabled = pref.isLoggedIn
        postActions.chbxShare.isChecked = post.isShared
        postActions.chbxShare.setOnCheckedChangeListener(onShareChangedListener)
    }

    private fun setLikeCount() {
        postActions.likeCount.isVisible = post.likes > 0
        postActions.likeCount.text = AppUtil.largeNumberFormatter(post.likes)
    }

    private fun setShareCount() {
        postActions.shareCount.isVisible = post.shares > 0
        postActions.shareCount.text = AppUtil.largeNumberFormatter(post.shares)
    }

    companion object {
        fun styleAuthorTextView(view: TextView, theme: Theme, postByOp: Boolean) {
            val color = if (postByOp) {
                theme.colorAccent
            } else {
                theme.colorOnBackground
            }
            view.setTextColor(color)
        }

        fun highlight(view: View, theme: Theme, highlight: Boolean = false) {
            view.backgroundTintList =
                ColorStateList.valueOf(if (highlight) theme.colorControlHighlight else theme.colorSurface)
        }
    }
}