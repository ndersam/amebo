package com.amebo.amebo.screens.topiclist.main

import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.fragment.app.setFragmentResultListener
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BackPressable
import com.amebo.amebo.databinding.TopicListScreenBinding
import com.amebo.amebo.screens.topiclist.BaseTopicListScreen
import com.amebo.core.domain.Board
import com.amebo.core.domain.BoardsDataPage
import com.amebo.core.domain.TopicListDataPage
import com.amebo.core.domain.User

open class TopicListScreen : BaseTopicListScreen(R.layout.topic_list_screen),
    TopicListView.Listener, BackPressable {
    val binding: TopicListScreenBinding by viewBinding(TopicListScreenBinding::bind)
    private val topicListView get() = baseTopicListView as TopicListView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(FragKeys.RESULT_TOPIC_LIST_META) { _, bundle ->
            val page = bundle.getInt(FragKeys.BUNDLE_SELECTED_PAGE)
            viewModel.loadPage(page)
        }
        setFragmentResultListener(FragKeys.RESULT_ACTION_FOLLOW_BOARD) { _, _ ->
            toggleBoardFollowing()
        }
        setFragmentResultListener(FragKeys.RESULT_ACTION_NEW_TOPIC) { _, _ ->
            newTopic()
        }
        setFragmentResultListener(FragKeys.RESULT_TOPIC_LIST) { _, bundle ->
            val postList = bundle.getParcelable<TopicListDataPage>(FragKeys.BUNDLE_TOPIC_LIST)!!
            viewModel.setData(postList)
        }
    }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        super.onViewCreated(savedInstanceState)

        viewModel.followingBoardEvent.observe(
            viewLifecycleOwner,
            EventObserver(::onFollowedBoardEvent)
        )
    }

    private fun onFollowedBoardEvent(content: Resource<Pair<Board, Boolean>>) {
        when (content) {
            is Resource.Success -> topicListView.onFollowingSuccess(content)
            is Resource.Error -> topicListView.onFollowingError(content)
            is Resource.Loading -> topicListView.onFollowingLoading(content)
        }
    }


    override fun viewMods(users: List<User>) =
        router.toUserList(users, getString(R.string.x_board_moderators, (topicList as Board).name))


    override fun mailSuperMods() {
        router.toMailSuperMods()
    }

    override fun mailBoardMods() {
        router.toMailBoardMods(
            (viewModel.dataPage as BoardsDataPage).boardId ?: return,
            topicList as Board
        )
    }

    override fun toggleBoardFollowing() = viewModel.toggleBoardFollowing()


    override fun onNavigationClicked() {
        router.back()
    }

    override fun toUserList(users: List<User>, title: String) = router.toUserList(users, title)


    override fun unFollowBoard(board: Board, position: Int) {
        viewModel.unFollowBoard(board)
    }

    override fun handledBackPress(): Boolean {
        if (binding.root.isDrawerOpen(GravityCompat.END)) {
            binding.root.closeDrawer(GravityCompat.END)
            return true
        }
        return false
    }
}
