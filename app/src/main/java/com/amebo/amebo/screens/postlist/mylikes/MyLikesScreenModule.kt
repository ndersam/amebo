package com.amebo.amebo.screens.postlist.mylikes

import androidx.fragment.app.Fragment
import com.amebo.amebo.screens.postlist.TimelinePostListScreenModuleHelper
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapterModule
import com.amebo.core.domain.PostList
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ItemAdapterModule::class, MyLikesScreenModule.ProviderModule::class])
abstract class MyLikesScreenModule {

    @Binds
    abstract fun bindTopicListAdapter(screen: MyLikesScreen): PostListAdapterListener

    @Binds
    abstract fun bindFragment(screen: MyLikesScreen): Fragment

    @Module
    class ProviderModule {

        @Provides
        fun providePostListView(
            screen: MyLikesScreen,
            contentAdapter: ItemAdapter
        ) = TimelinePostListScreenModuleHelper.providePostListView(
            screen,
            contentAdapter
        )

        @Provides
        fun providePostList(screen: MyLikesScreen): PostList {
            return screen.postList
        }
    }
}