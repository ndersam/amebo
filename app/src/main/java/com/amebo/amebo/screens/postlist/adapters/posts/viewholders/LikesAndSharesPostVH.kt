package com.amebo.amebo.screens.postlist.adapters.posts.viewholders

import androidx.core.view.isVisible
import com.amebo.amebo.R
import com.amebo.amebo.common.Pref
import com.amebo.amebo.databinding.ItemSharedPostBinding
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.PostItem
import com.amebo.core.CoreUtils
import com.amebo.core.domain.LikedOrSharedPost
import com.bumptech.glide.RequestManager

class LikesAndSharesPostVH(
    pref: Pref,
    private val binding: ItemSharedPostBinding,
    requestManager: RequestManager,
    listener: PostListAdapterListener
) :
    BasePostVH(
        pref = pref,
        view = binding.root,
        body = binding.text,
        listener = listener,
        requestManager = requestManager,
        postTime = binding.postTime,
        indicator = binding.indicator,
        imageRecyclerView = binding.imageRecyclerView,
        imageFrame = binding.imageFrame,
        postActions = binding.postActions,
        postAuthor = binding.postAuthor
    ) {

    init {
        binding.shareBullet.isVisible = false
        binding.sharer.isVisible = false
    }

    fun bind(
        item: PostItem.LikesOrSharedPostItem,
        postPosition: Int
    ) {
        val post = item.likedOrSharedPost.post
        bind(post, postPosition)

        binding.topic.setOnClickListener { listener.onPostTopicClick(post.topic, binding.topic) }
        binding.board.setOnClickListener { listener.onBoardClicked(post.topic.mainBoard!!) }
        binding.topic.text = if (item.likedOrSharedPost.isMainPost)
            post.topic.title
        else
            "RE: ${post.topic.title}"
        binding.board.text = post.topic.mainBoard!!.name
        val context = itemView.context //.getString(R.string.liked_at)
        binding.shareInfo.text = when (val kind = item.likedOrSharedPost.kind) {
            is LikedOrSharedPost.Kind.Liked -> {
                binding.sharer.isVisible = false
                binding.shareBullet.isVisible = false
                context.getString(R.string.liked)
            }
            is LikedOrSharedPost.Kind.Shared -> {
                binding.sharer.isVisible = true
                binding.shareBullet.isVisible = true
                val sharer = if (kind.isYou) pref.user!! else kind.sharer!!
                binding.sharer.text = sharer.name
                binding.sharer.setOnClickListener {
                    listener.onUserClicked(sharer)
                }
                context.getString(R.string.shared_by)
            }
        }

        binding.shareTime.text = CoreUtils.howLongAgo(item.likedOrSharedPost.timestamp) + " ago"
    }
}