package com.amebo.amebo.screens.postlist.adapters.posts.viewholders

import com.amebo.amebo.R
import com.amebo.amebo.common.Pref
import com.amebo.amebo.databinding.ItemSharedPostBinding
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.PostItem
import com.amebo.core.common.CoreUtils
import com.bumptech.glide.RequestManager

class SharedPostVH(
    pref: Pref,
    private val binding: ItemSharedPostBinding,
    requestManager: RequestManager,
    listener: PostListAdapterListener
) :
    BasePostVH(
        view = binding.root,
        body = binding.text,
        listener = listener,
        pref = pref,
        requestManager = requestManager,
        postTime = binding.postTime,
        postActions = binding.postActions,
        indicator = binding.indicator,
        imageRecyclerView = binding.imageRecyclerView,
        imageFrame = binding.imageFrame,
        postAuthor = binding.postAuthor
    ) {


    fun bind(
        item: PostItem.SharedPostItem,
        postPosition: Int
    ) {
        val post = item.sharedPost.post
        bind(post, postPosition)

        binding.topic.setOnClickListener { listener.onPostTopicClick(post.topic, binding.topic) }
        binding.board.setOnClickListener { listener.onBoardClicked(post.topic.mainBoard!!) }
        binding.topic.text = if (item.sharedPost.isMainPost)
            post.topic.title
        else
            "RE: ${post.topic.title}"
        binding.board.text = post.topic.mainBoard!!.name
        binding.shareInfo.text =
            binding.shareInfo.context.getString(R.string.xx_by, item.sharedPost.extra)
        binding.sharer.text = item.sharedPost.sharer.name
        binding.sharer.setOnClickListener { listener.onUserClicked(item.sharedPost.sharer) }
        binding.shareTime.text = CoreUtils.howLongAgo(item.sharedPost.shareTime)
    }
}