package com.amebo.amebo.screens.postlist.userposts

import androidx.fragment.app.Fragment
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapterModule
import com.amebo.amebo.screens.postlist.components.PostListView
import com.amebo.core.domain.PostList
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ItemAdapterModule::class, UsersPostsScreenModule.ProviderModule::class])
abstract class UsersPostsScreenModule {

    @Binds
    abstract fun bindTopicListAdapter(screen: UserPostsScreen): PostListAdapterListener

    @Binds
    abstract fun bindFragment(screen: UserPostsScreen): Fragment

    @Module
    class ProviderModule {

        @Provides
        fun providePostListView(
            screen: UserPostsScreen,
            contentAdapter: ItemAdapter
        ): PostListView = UserPostListView(
            screen,
            contentAdapter,
            screen.postList,
            screen
        )

        @Provides
        fun providePostList(screen: UserPostsScreen): PostList {
            return screen.postList
        }
    }
}