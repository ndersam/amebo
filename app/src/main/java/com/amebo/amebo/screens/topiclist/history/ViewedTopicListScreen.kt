package com.amebo.amebo.screens.topiclist.history

import android.os.Bundle
import androidx.core.view.isVisible
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.FragmentViewedTopicListScreenBinding
import com.amebo.core.domain.Topic

class ViewedTopicListScreen : BaseFragment(R.layout.fragment_viewed_topic_list_screen),
    ItemAdapter.Listener {
    private var itemAdapter: ItemAdapter? = null
    private val binding by viewBinding(FragmentViewedTopicListScreenBinding::bind)
    private val viewModel by viewModels<ViewedTopicsViewModel>()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        itemAdapter = ItemAdapter(this)
        binding.recyclerView.adapter = itemAdapter
        binding.toolbar.setNavigationOnClickListener { router.back() }
        binding.toolbar.setOnClickListener { binding.recyclerView.scrollToPosition(0) }
        viewModel.dataEvent.observe(viewLifecycleOwner, {
            itemAdapter!!.setItem(it)
            alterVisibility()
        })

        viewModel.load()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        itemAdapter = null
    }

    override fun onTopicClicked(topic: Topic) = router.toTopic(topic)


    override fun removeTopic(topic: Topic, position: Int) {
        itemAdapter!!.removeItemAt(position)
        viewModel.removeTopic(topic)
        alterVisibility()
    }

    private fun alterVisibility() {
        if (itemAdapter!!.itemCount == 0) {
            binding.recyclerView.isVisible = false
            binding.ouchView.isVisible = true
            binding.ouchView.buttonIsVisible = false
            binding.ouchView.empty()
        } else {
            binding.recyclerView.isVisible = true
            binding.ouchView.isVisible = false
        }
    }
}