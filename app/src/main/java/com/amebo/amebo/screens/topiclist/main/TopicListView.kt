package com.amebo.amebo.screens.topiclist.main

import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import com.amebo.amebo.R
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.setMenu
import com.amebo.amebo.common.extensions.snack
import com.amebo.amebo.databinding.TopicListScreenBinding
import com.amebo.amebo.screens.postlist.topic.TopicScreen
import com.amebo.amebo.screens.topiclist.BaseTopicListView
import com.amebo.amebo.screens.topiclist.SimpleTopicListView
import com.amebo.core.domain.*
import java.lang.ref.WeakReference

class TopicListView(
    private val pref: Pref,
    binding: TopicListScreenBinding,
    viewLifecycleOwner: LifecycleOwner,
    topicList: TopicList,
    private val listener: Listener
) : SimpleTopicListView(
    topicList = topicList,
    sort = pref.defaultSortOf(topicList),
    listener = listener,
    binding = binding
) {
    private var bindingRef = WeakReference(binding)
    private val binding: TopicListScreenBinding get() = bindingRef.get()!!

    private val context get() = binding.root.context

    private val hasRightDrawer = when (topicList) {
        is Board, is FollowedBoards -> true
        else -> false
    }

    private var drawerView: TopicListDrawerView? = null

    init {
        binding.toolbar.setNavigationOnClickListener { listener.onNavigationClicked() }
        binding.toolbar.setOnClickListener {
            binding.recyclerView.scrollToPosition(0)
            binding.bottomBar.behavior.slideDown(binding.bottomBar)
        }

        binding.toolbar.setMenu(R.menu.topic_list_menu)
        if (hasRightDrawer) {
            drawerView = TopicListDrawerView(
                topicList,
                pref,
                binding.drawer.inflate(),
                listener
            )
            binding.toolbar.menu.findItem(R.id.followBoard).isVisible = topicList is Board
        } else {
            binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END)
        }
        binding.toolbar.setOnMenuItemClickListener(::onOptionsItemSelected)
        if (topicList is FollowedTopics) {
            ItemTouchHelper(SwipeToUnFollowCallback(context, ::unFollowTopicAt))
                .attachToRecyclerView(binding.recyclerView)
        }
    }

    fun onFollowingSuccess(success: Resource.Success<Pair<Board, Boolean>>) {
        val messageResId =
            if (success.content.second) R.string.now_following else R.string.unfollowed_x
        binding.recyclerView.snack(context.getString(messageResId, success.content.first.name))
        pref.clearFollowedBoardsSyncTime()
    }

    fun onFollowingError(error: Resource.Error<Pair<Board, Boolean>>) {
        binding.recyclerView.snack(error.cause)
    }

    fun onFollowingLoading(loading: Resource.Loading<Pair<Board, Boolean>>) {
        // TODO
    }

    override fun setContent(page: BaseTopicListDataPage) {
        super.setContent(page)
        invalidateToolbarMenu(page)
        drawerView?.update(page)
    }

    private fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.info -> {
                binding.root.openDrawer(GravityCompat.END)
            }
            R.id.followBoard -> listener.toggleBoardFollowing()
        }
        return true
    }

    private fun unFollowTopicAt(position: Int) {
        // If itemAdapter is in concat adapter, we might need to increment the position if
        // the itemAdapter is not the first adapter in the concat adapter
        val topic = itemAdapter.removeTopicAt(position) ?: return
        topic.followOrUnFollowLink ?: return
        listener.unFollowTopic(topic)
    }

    private fun invalidateToolbarMenu(content: BaseTopicListDataPage) {
        val menu = binding.toolbar.menu
        menu.findItem(R.id.info).apply {
            isVisible = topicList is Board || topicList is FollowedBoards
        }
        menu.findItem(R.id.followBoard).apply {
            if (content is BoardsDataPage) {
                isVisible = topicList is Board && pref.isLoggedIn
                TopicScreen.setFollowing(this, content.isFollowing)
            }
        }
    }


    interface Listener : BaseTopicListView.Listener, TopicListDrawerView.Listener {
        fun viewMods(users: List<User>)
        fun toggleBoardFollowing()
        fun onNavigationClicked()
        fun unFollowTopic(topic: Topic)
    }
}