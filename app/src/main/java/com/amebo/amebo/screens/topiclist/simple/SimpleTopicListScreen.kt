package com.amebo.amebo.screens.topiclist.simple

import android.os.Bundle
import androidx.core.os.bundleOf
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.FragmentSimpleTopicsScreenBinding
import com.amebo.amebo.screens.topiclist.adapters.TopicListAdapterListener
import com.amebo.core.domain.Board
import com.amebo.core.domain.Topic
import com.amebo.core.domain.User

class SimpleTopicListScreen : BaseFragment(R.layout.fragment_simple_topics_screen),
        TopicListAdapterListener {

    val binding: FragmentSimpleTopicsScreenBinding by viewBinding(FragmentSimpleTopicsScreenBinding::bind)
    val topics get() = requireArguments().getParcelableArrayList<Topic>(TOPICS)!!
    val title get() = requireArguments().getString(TITLE)!!

    lateinit var adapter: SimpleItemAdapter
    val viewModel by viewModels<SimpleTopicListScreenViewModel>()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        adapter = SimpleItemAdapter(topics, this)
        binding.recyclerView.adapter = adapter
        binding.toolbar.title = title
        binding.toolbar.setNavigationOnClickListener { router.back() }

        viewModel.viewedTopicsLoadedEvent.observe(viewLifecycleOwner, EventObserver {
            adapter.notifyDataSetChanged()
        })
        viewModel.initialize()
    }

    override fun onTopicClicked(topic: Topic) = router.toTopic(topic)

    override fun onAuthorClicked(user: User) = router.toUser(user)

    override fun onBoardClicked(board: Board) = router.toBoard(board)

    override fun hasViewedTopic(topic: Topic) = viewModel.hasViewedTopic(topic)

    override fun hasNextPage() = false // Not used

    override fun hasPrevPage(): Boolean = false // Not Used

    override fun loadFirstPage() = Unit

    override fun loadLastPage() = Unit

    override fun loadPrevPage() = Unit

    override fun loadNextPage() = Unit // Not used


    companion object {
        private const val TOPICS = "topics"
        private const val TITLE = "title"

        fun newBundle(topics: List<Topic>, title: String) =
            bundleOf(TITLE to title, TOPICS to ArrayList(topics))
    }


}