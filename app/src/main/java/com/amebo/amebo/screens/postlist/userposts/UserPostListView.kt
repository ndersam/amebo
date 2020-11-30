package com.amebo.amebo.screens.postlist.userposts

import androidx.core.view.isVisible
import com.amebo.amebo.common.extensions.getPostListTitle
import com.amebo.amebo.screens.postlist.PostListMeta
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.amebo.screens.postlist.components.IPostListView
import com.amebo.amebo.screens.postlist.components.PostListView
import com.amebo.core.domain.PostList

class UserPostListView(
    fragment: UserPostsScreen,
    contentAdapter: ItemAdapter,
    postList: PostList,
    listener: IPostListView.Listener
) : PostListView(fragment, fragment.binding, contentAdapter, postList, listener) {

    private val binding = fragment.binding

    init {
        binding.title.text = postList.getPostListTitle(fragment.requireContext())
        binding.txtPageInfo.setOnClickListener { listener.onPageMetaClicked() }
    }

    override fun setPostListMeta(meta: PostListMeta) {
        super.setPostListMeta(meta)
        binding.txtPageInfo.isVisible = true
        binding.txtPageInfo.text = meta.toString(toolbar.context)
    }

    override fun scrollToPosition(position: Int) {
        super.scrollToPosition(position)
        val bottomBar = binding.bottomBar.bottomBar
        bottomBar.behavior.slideDown(bottomBar)
    }
}