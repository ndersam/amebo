package com.amebo.amebo.screens.search

import androidx.fragment.app.Fragment
import com.amebo.amebo.screens.postlist.adapters.PostListAdapterListener
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapterModule
import com.amebo.core.domain.PostList
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ItemAdapterModule::class, SearchResultsScreenModule.ProviderModule::class])
abstract class SearchResultsScreenModule {

    @Binds
    abstract fun bindTopicListAdapter(screen: SearchResultScreen): PostListAdapterListener

    @Binds
    abstract fun bindFragment(screen: SearchResultScreen): Fragment

    @Module
    class ProviderModule {

        @Provides
        fun providePostListView(
            screen: SearchResultScreen,
            contentAdapter: ItemAdapter
        ): SearchResultsView {
            return SearchResultsView(
                screen,
                contentAdapter, screen.binding,
                screen.postList,
                screen
            )
        }

        @Provides
        fun providePostList(screen: SearchResultScreen): PostList {
            return screen.postList
        }
    }
}