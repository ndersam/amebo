package com.amebo.amebo.screens.postlist.components

import androidx.core.view.isVisible
import com.amebo.amebo.R
import com.amebo.amebo.common.drawerLayout.DrawerLayoutToolbarMediator
import com.amebo.amebo.common.extensions.getPostListTitle
import com.amebo.amebo.screens.postlist.PostListMeta
import com.amebo.amebo.screens.postlist.TimelinePostListScreen
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.core.domain.PostList

class TimelinePostListView(
    fragment: TimelinePostListScreen<*>,
    contentAdapter: ItemAdapter,
    postList: PostList,
    listener: IPostListView.Listener
) : PostListView(fragment, fragment.binding, contentAdapter, postList, listener) {

    private val binding = fragment.binding

    init {
        DrawerLayoutToolbarMediator(fragment, binding.toolbar)
        if (fragment.isRootScreen.not()) {
            binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp)
        }

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