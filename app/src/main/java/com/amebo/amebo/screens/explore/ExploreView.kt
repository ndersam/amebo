package com.amebo.amebo.screens.explore

import android.view.View
import androidx.core.view.isVisible
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.disableCopyPaste
import com.amebo.amebo.databinding.ExploreScreenBinding
import com.amebo.amebo.screens.explore.adapters.TopicListAdapter
import com.amebo.core.domain.Board
import java.lang.ref.WeakReference

class ExploreView(
    private val pref: Pref,
    binding: ExploreScreenBinding,
    private val listener: Listener,
    private val topicListAdapter: TopicListAdapter = TopicListAdapter(listener, showHistory = true)
) {
    private val bindingRef = WeakReference(binding)
    private val binding get() = bindingRef.get()!!


    init {
        binding.rvBoards.adapter = topicListAdapter
        binding.btnSync.isVisible = pref.isLoggedIn
        binding.searchBox.disableCopyPaste()
        binding.searchBox.setOnClickListener { listener.showSearch(binding.searchBox) }
        binding.btnSync.setOnClickListener {
            listener.fetchFollowedBoards()
        }
    }


    fun setExploreData(exploreData: ExploreData) {
        topicListAdapter.setData(exploreData.topicListData)
        if (exploreData.topicListData.followed.isEmpty() || pref.isFollowedBoardsSyncDue()) {
            listener.fetchFollowedBoards()
        } else {
            binding.btnSync.isVisible = true && pref.isLoggedIn
        }
    }

    fun onFetchedFollowedBoardsSuccess(success: Resource.Success<List<Board>>) {
        topicListAdapter.setFollowedBoards(success.content)
        binding.toolbarProgress.isVisible = false
        binding.btnSync.isVisible = true && pref.isLoggedIn
    }

    fun onFetchedFollowedBoardsError(error: Resource.Error<List<Board>>) {
        binding.toolbarProgress.isVisible = false
        binding.btnSync.isVisible = true && pref.isLoggedIn
        topicListAdapter.setFollowedBoards(error.content ?: return)
    }

    fun onFetchedFollowedBoardsLoading(loading: Resource.Loading<List<Board>>) {
        binding.toolbarProgress.isVisible = true
        binding.btnSync.isVisible = false
        topicListAdapter.setFollowedBoards(loading.content ?: return)
    }

    interface Listener : TopicListAdapter.Listener {
        fun showSearch(view: View)
        fun fetchFollowedBoards()
    }
}