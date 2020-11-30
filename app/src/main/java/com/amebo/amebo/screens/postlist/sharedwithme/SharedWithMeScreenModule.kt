package com.amebo.amebo.screens.postlist.sharedwithme

import androidx.fragment.app.Fragment
import com.amebo.amebo.screens.postlist.TimelinePostListScreenModuleHelper
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapterModule
import com.amebo.core.domain.PostList
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ItemAdapterModule::class, SharedWithMeScreenModule.ProviderModule::class])
abstract class SharedWithMeScreenModule {

    @Binds
    abstract fun bindTopicListAdapter(screen: SharedWithMeScreen): PostListAdapterListener

    @Binds
    abstract fun bindFragment(screen: SharedWithMeScreen): Fragment

    @Module
    class ProviderModule {

        @Provides
        fun providePostListView(
            screen: SharedWithMeScreen,
            contentAdapter: ItemAdapter
        ) = TimelinePostListScreenModuleHelper.providePostListView(
            screen,
            contentAdapter
        )

        @Provides
        fun providePostList(screen: SharedWithMeScreen): PostList {
            return screen.postList
        }
    }
}