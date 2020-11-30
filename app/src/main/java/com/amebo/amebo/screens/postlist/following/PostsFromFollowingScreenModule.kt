package com.amebo.amebo.screens.postlist.following

import androidx.fragment.app.Fragment
import com.amebo.amebo.screens.postlist.TimelinePostListScreenModuleHelper
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapterModule
import com.amebo.core.domain.PostList
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ItemAdapterModule::class, PostsFromFollowingScreenModule.ProviderModule::class])
abstract class PostsFromFollowingScreenModule {

    @Binds
    abstract fun bindTopicListAdapter(screen: PostsFromFollowingsScreen): PostListAdapterListener

    @Binds
    abstract fun bindFragment(screen: PostsFromFollowingsScreen): Fragment

    @Module
    class ProviderModule {

        @Provides
        fun providePostListView(
            screen: PostsFromFollowingsScreen,
            contentAdapter: ItemAdapter
        ) = TimelinePostListScreenModuleHelper.providePostListView(
            screen,
            contentAdapter
        )

        @Provides
        fun providePostList(screen: PostsFromFollowingsScreen): PostList {
            return screen.postList
        }
    }
}