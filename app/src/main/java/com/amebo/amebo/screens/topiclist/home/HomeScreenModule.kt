package com.amebo.amebo.screens.topiclist.home

import com.amebo.amebo.screens.topiclist.BaseTopicListView
import com.amebo.amebo.screens.topiclist.main.TopicListView
import dagger.Module
import dagger.Provides

@Module
class HomeScreenModule {

    @Provides
    fun provideBaseTopicListView(topicListScreen: HomeScreen): BaseTopicListView {
        return TopicListView(
            pref = topicListScreen.pref,
            binding = topicListScreen.binding,
            viewLifecycleOwner = topicListScreen.viewLifecycleOwner,
            topicList = topicListScreen.topicList,
            listener = topicListScreen
        )
    }
}