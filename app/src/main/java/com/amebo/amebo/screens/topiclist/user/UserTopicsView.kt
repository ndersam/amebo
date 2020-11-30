package com.amebo.amebo.screens.topiclist.user

import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.amebo.amebo.R
import com.amebo.amebo.databinding.TopicListScreenBinding
import com.amebo.amebo.screens.topiclist.BaseTopicListView
import com.amebo.amebo.screens.topiclist.SimpleTopicListView
import com.amebo.core.domain.TopicList

class UserTopicsView(
    topicList: TopicList,
    listener: Listener,
    binding: TopicListScreenBinding,
    viewLifecycleOwner: LifecycleOwner
) : SimpleTopicListView(
    viewLifecycleOwner = viewLifecycleOwner,
    topicList = topicList,
    listener = listener,
    binding = binding,
    sort = null
) {

    init {
        binding.btnMore.isVisible = false
        binding.btnNewTopic.isVisible = false
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp)
        binding.toolbar.setNavigationOnClickListener { listener.onNavigationClicked() }
    }

    interface Listener : BaseTopicListView.Listener {
        fun onNavigationClicked()
    }
}