package com.amebo.amebo.screens.postlist.topic

import android.os.Bundle
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.FragmentRecentTopicsBinding
import com.amebo.core.domain.Topic

class RecentTopicsFragment : BaseFragment(R.layout.fragment_recent_topics),
    SimpleTopicListAdapter.Listener {
    private var recentTopicsAdapter: SimpleTopicListAdapter? = null
    private val binding by viewBinding(FragmentRecentTopicsBinding::bind)
    private val viewModel by viewModels<RecentTopicsViewModel>()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        recentTopicsAdapter =
            SimpleTopicListAdapter(showDeleteButton = true, listener = this)
        binding.recyclerView.adapter = recentTopicsAdapter

        viewModel.recentTopicsLiveData.observe(viewLifecycleOwner, {
            recentTopicsAdapter!!.setItem(it)
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recentTopicsAdapter = null
    }

    override fun onTopicClicked(topic: Topic) = router.toTopic(topic)


    override fun removeTopic(topic: Topic, position: Int) {
        viewModel.removeTopic(topic)
    }
}