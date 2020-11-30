package com.amebo.amebo.screens.postlist.topic

import androidx.fragment.app.Fragment
import com.amebo.amebo.common.widgets.AppBarStateChangeListener
import com.amebo.amebo.databinding.TopicScreenBinding
import com.amebo.amebo.screens.postlist.PostListMeta
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.amebo.screens.postlist.components.IPostListView
import com.amebo.amebo.screens.postlist.components.PostListView
import com.amebo.core.domain.PostList
import com.amebo.core.domain.PostListDataPage
import com.amebo.core.domain.Topic
import com.google.android.material.appbar.AppBarLayout

class TopicPostListView(
    fragment: Fragment,
    contentAdapter: ItemAdapter,
    private val binding: TopicScreenBinding,
    postList: PostList,
    listener: IPostListView.Listener
) : PostListView(
    fragment,
    contentAdapter,
    binding.swipeRefreshLayout,
    binding.recyclerView,
    binding.toolbar,
    binding.bottomBar.btnNextPage,
    binding.bottomBar.btnPrevPage,
    binding.bottomBar.btnRefreshPage,
    binding.bottomBar.btnMore,
    postList, listener
) {

    private var topic = postList as Topic

    private val appBarListener = object : AppBarStateChangeListener() {

        override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
            binding.swipeRefreshLayout.isEnabled = state == State.EXPANDED
            if (state == State.COLLAPSED) {
                binding.toolbar.title = topic.title
            } else {
                binding.toolbar.title = " "
            }
        }
    }


    init {
        binding.appBar.addOnOffsetChangedListener(appBarListener)
    }

    override fun setContent(dataPage: PostListDataPage) {
        super.setContent(dataPage)
        binding.bottomBar.btnReply.isEnabled = true
    }


    override fun setTitle(title: String) {
        // Don't set toolbar title
    }

    override fun setPostListMeta(meta: PostListMeta) {
        // Don't set toolbar subtitle
    }

    override fun scrollToPosition(position: Int) {
        super.scrollToPosition(position)
        val bottomBar = binding.bottomBar.bottomBar
        bottomBar.behavior.slideDown(bottomBar)
    }
}