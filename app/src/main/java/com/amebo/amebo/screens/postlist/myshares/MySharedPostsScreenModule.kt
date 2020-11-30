package com.amebo.amebo.screens.postlist.myshares

import androidx.fragment.app.Fragment
import com.amebo.amebo.screens.postlist.TimelinePostListScreenModuleHelper
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapterModule
import com.amebo.core.domain.PostList
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ItemAdapterModule::class, MySharedPostsScreenModule.ProviderModule::class])
abstract class MySharedPostsScreenModule {

    @Binds
    abstract fun bindTopicListAdapter(screen: MySharedPostsScreen): PostListAdapterListener

    @Binds
    abstract fun bindFragment(screen: MySharedPostsScreen): Fragment

    @Module
    class ProviderModule {

        @Provides
        fun providePostListView(
            screen: MySharedPostsScreen,
            contentAdapter: ItemAdapter
        ) = TimelinePostListScreenModuleHelper.providePostListView(
            screen,
            contentAdapter
        )

        @Provides
        fun providePostList(screen: MySharedPostsScreen): PostList {
            return screen.postList
        }
    }
}