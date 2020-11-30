package com.amebo.amebo.screens.postlist.adapters.posts.viewholders

import com.amebo.amebo.common.Pref
import com.amebo.amebo.databinding.ItemMainPostBinding
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.PostItem
import com.bumptech.glide.RequestManager

class MainPostItemVH(
    pref: Pref,
    binding: ItemMainPostBinding,
    requestManager: RequestManager,
    listener: PostListAdapterListener
) : BasePostVH(
    body = binding.text,
    listener = listener,
    postAuthor = binding.postAuthor,
    requestManager = requestManager,
    postTime = binding.postTime,
    postActions = binding.postActions,
    indicator = binding.indicator,
    imageRecyclerView = binding.imageRecyclerView,
    imageFrame = binding.imageFrame,
    view = binding.root,
    pref = pref
) {

    fun bind(item: PostItem.MainPostItem, postPosition: Int) {
        highlight(itemView, theme, item.highlight)
        bind(item.post, postPosition)
    }
}