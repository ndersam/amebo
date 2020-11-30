package com.amebo.amebo.screens.postlist.adapters.posts.viewholders

import com.amebo.amebo.common.Pref
import com.amebo.amebo.databinding.ItemSimplePostBinding
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.core.domain.SimplePost
import com.bumptech.glide.RequestManager

class PostItemVH(
    pref: Pref,
    binding: ItemSimplePostBinding,
    requestManager: RequestManager,
    listener: PostListAdapterListener
) :
    BasePostVH(
        pref = pref,
        view = binding.root,
        imageFrame = binding.imageFrame,
        imageRecyclerView = binding.imageRecyclerView,
        indicator = binding.indicator,
        postActions = binding.postActions,
        postTime = binding.postTime,
        requestManager = requestManager,
        postAuthor = binding.postAuthor,
        listener = listener,
        body = binding.text
    ) {


    fun bind(
        post: SimplePost,
        postByOp: Boolean,
        postPosition: Int,
        highlight: Boolean
    ) {
        highlight(itemView, theme, highlight)
        styleAuthorTextView(
            postAuthor,
            theme,
            postByOp
        )
        bind(post, postPosition)
    }

}