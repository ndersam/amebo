package com.amebo.amebo.screens.postlist.recentposts

import androidx.fragment.app.Fragment
import com.amebo.amebo.screens.postlist.TimelinePostListScreenModuleHelper
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapterModule
import com.amebo.core.domain.PostList
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ItemAdapterModule::class, RecentPostsScreenModule.ProviderModule::class])
abstract class RecentPostsScreenModule {

    @Binds
    abstract fun bindTopicListAdapter(screen: RecentPostsScreen): PostListAdapterListener

    @Binds
    abstract fun bindFragment(screen: RecentPostsScreen): Fragment

    @Module
    class ProviderModule {

        @Provides
        fun providePostListView(
            screen: RecentPostsScreen,
            contentAdapter: ItemAdapter
        ) = TimelinePostListScreenModuleHelper.providePostListView(
            screen,
            contentAdapter
        )

        @Provides
        fun providePostList(screen: RecentPostsScreen): PostList {
            return screen.postList
        }
    }
}