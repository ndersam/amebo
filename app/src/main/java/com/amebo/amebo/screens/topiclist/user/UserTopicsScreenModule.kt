package com.amebo.amebo.screens.topiclist.user

import com.amebo.amebo.screens.topiclist.BaseTopicListView
import dagger.Module
import dagger.Provides

@Module
class UserTopicsScreenModule {

    @Provides
    fun provideBaseTopicListView(topicListScreen: UserTopicsScreen): BaseTopicListView {
        return UserTopicsView(
            topicList = topicListScreen.topicList,
            listener = topicListScreen,
            binding = topicListScreen.binding
        )
    }
}