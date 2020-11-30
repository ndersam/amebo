package com.amebo.amebo.screens.topiclist.main

import com.amebo.amebo.screens.topiclist.BaseTopicListView
import dagger.Module
import dagger.Provides

@Module
class TopicListScreenModule {

    @Provides
    fun provideBaseTopicListView(topicListScreen: TopicListScreen): BaseTopicListView {
        return TopicListView(
            pref = topicListScreen.pref,
            binding = topicListScreen.binding,
            viewLifecycleOwner = topicListScreen.viewLifecycleOwner,
            topicList = topicListScreen.topicList,
            listener = topicListScreen
        )
    }
}