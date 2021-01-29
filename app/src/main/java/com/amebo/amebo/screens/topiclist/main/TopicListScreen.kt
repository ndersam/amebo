package com.amebo.amebo.screens.topiclist.main

import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.asTheme
import com.amebo.amebo.common.extensions.dpToPx
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BackPressable
import com.amebo.amebo.databinding.TopicListScreenBinding
import com.amebo.amebo.screens.topiclist.BaseTopicListScreen
import com.amebo.core.domain.*
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        viewModel.unFollowTopicEvent.observe(
            viewLifecycleOwner,
            EventObserver(::onUnFollowTopicEvent)
        )
    }

    private fun onFollowedBoardEvent(content: Resource<Pair<Board, Boolean>>) {
        when (content) {
            is Resource.Success -> topicListView.onFollowingSuccess(content)
            is Resource.Error -> topicListView.onFollowingError(content)
            is Resource.Loading -> topicListView.onFollowingLoading(content)
        }
    }

    override fun onEventContentChanged(content: Resource<BaseTopicListDataPage>) {
        super.onEventContentChanged(content)
        if (content is Resource.Success && pref.showUnFollowTopicHint && content.content.data.isNotEmpty() && topicList is FollowedTopics) {
            pref.showUnFollowTopicHint = false
            showHint()
        }
    }

    private fun onUnFollowTopicEvent(content: Resource<Topic>) {
        // TODO: Ignore for now
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

    override fun unFollowTopic(topic: Topic) = viewModel.unFollowTopic(topic)

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

    private fun showHint() {
        lifecycleScope.launch {
            delay(300)
            val manager = binding.recyclerView.layoutManager as LinearLayoutManager

            val idx = (manager.findFirstCompletelyVisibleItemPosition() +
                    manager.findLastCompletelyVisibleItemPosition()) / 2
            val view =
                binding.recyclerView.findViewHolderForLayoutPosition(idx)?.itemView
                    ?: return@launch
            val rect = Rect().apply {
                bottom = view.bottom + view.height
                top = view.bottom
                right = view.right
                left = right - requireContext().dpToPx(100)
            }

            val theme = requireContext().asTheme()
            TapTargetView.showFor(
                requireActivity(),
                TapTarget.forBounds(
                    rect,
                    "Slide topic right-to-left to unfollow"
                ).outerCircleColorInt(theme.colorAccent)
                    .outerCircleAlpha(0.85f)
                    .targetCircleColor(android.R.color.white)
                    .titleTextSize(20)
                    .titleTextColor(android.R.color.white)
                    .textColor(R.color.white)
                    .textTypeface(Typeface.SANS_SERIF)
                    .dimColor(android.R.color.black)
                    .drawShadow(true)
                    .cancelable(true)
                    .tintTarget(true)
                    .transparentTarget(false)
                    .icon(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_arrow_back_24dp
                        )
                    )
                    .targetRadius(32),
                object : TapTargetView.Listener() {}
            )
        }
    }
}
