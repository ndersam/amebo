package com.amebo.amebo.screens.postlist.topic

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.FragmentTopicInfoBinding
import com.amebo.core.domain.Topic
import com.amebo.core.domain.TopicPostListDataPage
import timber.log.Timber

class TopicInfoFragment : BaseFragment(R.layout.fragment_topic_info),
    SimpleTopicListAdapter.Listener {

    companion object {
        const val KEY_TOPIC_POSTS_PAGE = "keyTopicPostsPage"
        const val KEY_TOPIC = "topic"
    }

    private val topic get() = requireArguments().getParcelable<Topic>(KEY_TOPIC)!!
    private var adapter: SimpleTopicListAdapter? = null
    private val binding by viewBinding(FragmentTopicInfoBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(KEY_TOPIC_POSTS_PAGE) { _, bundle ->
            val data = bundle.getParcelable<TopicPostListDataPage>(KEY_TOPIC_POSTS_PAGE)!!
            adapter!!.setItem(data.relatedTopics)
            Timber.d("Updated topic info ${data.relatedTopics.size}")
            binding.relatedTopicsTitle.isVisible = data.relatedTopics.isNotEmpty()
            binding.recyclerView.isVisible = data.relatedTopics.isNotEmpty()

            binding.topicTitle.text = data.topic.title
            data.topic.mainBoard?.let { board ->
                binding.board.text = board.name
                binding.board.setOnClickListener {
                    router.toBoard(board)
                }
            }

        }
    }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        adapter = SimpleTopicListAdapter(showDeleteButton = false, listener = this)
        binding.recyclerView.adapter = adapter

        binding.topicTitle.text = topic.title
        binding.board.text = topic.mainBoard?.name ?: ""
        binding.board.setOnClickListener {
            val board = topic.mainBoard
            if (board != null) {
                router.toBoard(board)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    override fun onTopicClicked(topic: Topic) = router.toTopic(topic)

}