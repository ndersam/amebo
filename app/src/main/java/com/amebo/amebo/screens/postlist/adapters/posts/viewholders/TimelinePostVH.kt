package com.amebo.amebo.screens.postlist.adapters.posts.viewholders

import com.amebo.amebo.common.Pref
import com.amebo.amebo.databinding.ItemTimelinePostBinding
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.PostItem
import com.bumptech.glide.RequestManager

class TimelinePostVH(
    pref: Pref,
    private val binding: ItemTimelinePostBinding,
    requestManager: RequestManager,
    listener: PostListAdapterListener
) :
    BasePostVH(
        postAuthor = binding.postAuthor,
        postActions = binding.postActions,
        imageFrame = binding.imageFrame,
        imageRecyclerView = binding.imageRecyclerView,
        indicator = binding.indicator,
        postTime = binding.postTime,
        requestManager = requestManager,
        listener = listener,
        body = binding.text,
        view = binding.root,
        pref = pref
    ) {


    fun bind(
        item: PostItem.TimelinePostItem,
        postPosition: Int
    ) {
        val post = item.timelinePost.post
        bind(post, postPosition)

        binding.topic.setOnClickListener { listener.onPostTopicClick(post.topic, binding.topic) }
        binding.board.setOnClickListener { listener.onBoardClicked(post.topic.mainBoard!!) }
        binding.topic.text = if (item.timelinePost.isMainPost)
            post.topic.title
        else
            "RE: ${post.topic.title}"
        binding.board.text = post.topic.mainBoard!!.name
    }
}
