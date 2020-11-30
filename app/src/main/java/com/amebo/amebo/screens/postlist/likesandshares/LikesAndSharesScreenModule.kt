package com.amebo.amebo.screens.postlist.likesandshares

import androidx.fragment.app.Fragment
import com.amebo.amebo.screens.postlist.TimelinePostListScreenModuleHelper
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapterModule
import com.amebo.core.domain.PostList
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ItemAdapterModule::class, LikesAndSharesScreenModule.ProviderModule::class])
abstract class LikesAndSharesScreenModule {

    @Binds
    abstract fun bindTopicListAdapter(screen: LikesAndSharesScreen): PostListAdapterListener

    @Binds
    abstract fun bindFragment(screen: LikesAndSharesScreen): Fragment

    @Module
    class ProviderModule {

        @Provides
        fun providePostListView(
            screen: LikesAndSharesScreen,
            contentAdapter: ItemAdapter
        ) = TimelinePostListScreenModuleHelper.providePostListView(
            screen,
            contentAdapter
        )

        @Provides
        fun providePostList(screen: LikesAndSharesScreen): PostList {
            return screen.postList
        }
    }
}