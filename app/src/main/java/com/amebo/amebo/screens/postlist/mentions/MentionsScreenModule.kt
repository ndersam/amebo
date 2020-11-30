package com.amebo.amebo.screens.postlist.mentions

import androidx.fragment.app.Fragment
import com.amebo.amebo.screens.postlist.TimelinePostListScreenModuleHelper
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapterModule
import com.amebo.core.domain.PostList
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ItemAdapterModule::class, MentionsScreenModule.ProviderModule::class])
abstract class MentionsScreenModule {

    @Binds
    abstract fun bindTopicListAdapter(screen: MentionsScreen): PostListAdapterListener

    @Binds
    abstract fun bindFragment(screen: MentionsScreen): Fragment

    @Module
    class ProviderModule {

        @Provides
        fun providePostListView(
            screen: MentionsScreen,
            contentAdapter: ItemAdapter
        ) = TimelinePostListScreenModuleHelper.providePostListView(
            screen,
            contentAdapter
        )

        @Provides
        fun providePostList(screen: MentionsScreen): PostList {
            return screen.postList
        }
    }
}