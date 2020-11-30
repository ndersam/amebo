package com.amebo.amebo.screens.postlist.topic

import androidx.fragment.app.Fragment
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapterModule
import com.amebo.core.domain.PostList
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ItemAdapterModule::class, TopicScreenModule.ProviderModule::class])
abstract class TopicScreenModule {

    @Binds
    abstract fun bindTopicListAdapter(topicScreen: TopicScreen): PostListAdapterListener

    @Binds
    abstract fun bindFragment(topicScreen: TopicScreen): Fragment

    @Module
    class ProviderModule {

        @Provides
        fun providePostListView(
            topicScreen: TopicScreen,
            contentAdapter: ItemAdapter
        ): TopicPostListView {
            return TopicPostListView(
                topicScreen,
                contentAdapter,
                topicScreen.binding,
                topicScreen.postList,
                topicScreen
            )
        }

        @Provides
        fun providePostList(topicScreen: TopicScreen): PostList {
            return topicScreen.postList
        }
    }
}