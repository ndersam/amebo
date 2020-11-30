package com.amebo.amebo.screens.topiclist.user

import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.databinding.TopicListScreenBinding
import com.amebo.amebo.screens.topiclist.BaseTopicListScreen

class UserTopicsScreen : BaseTopicListScreen(R.layout.topic_list_screen), UserTopicsView.Listener {
    val binding: TopicListScreenBinding by viewBinding(TopicListScreenBinding::bind)


    override fun onNavigationClicked() {
        router.back()
    }
}