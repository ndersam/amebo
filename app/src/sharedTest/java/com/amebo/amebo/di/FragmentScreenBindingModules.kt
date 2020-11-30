package com.amebo.amebo.di

import com.amebo.amebo.screens.newpost.modifypost.ModifyPostView
import com.amebo.amebo.screens.newpost.newpost.NewPostView
import com.amebo.amebo.screens.newpost.newtopic.NewTopicView
import com.amebo.amebo.screens.photoviewer.IPhotoViewerView
import com.amebo.amebo.screens.postlist.components.PostListView
import com.amebo.amebo.screens.postlist.topic.TopicPostListView
import com.amebo.amebo.screens.search.SearchResultsView
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides

@Module
class TestNewPostScreenModule {
    companion object {
        var newPostView: NewPostView = mock()
            private set

        fun reset() {
            newPostView = mock()
        }
    }

    @Provides
    fun provideNewPostView(): NewPostView {
        reset()
        return newPostView
    }
}

@Module
class TestModifyPostScreenModule {
    companion object {
        var modifyPostView: ModifyPostView = mock()
            private set

        private fun reset() {
            modifyPostView = mock()
        }
    }

    @Provides
    fun provideModifyPostView(): ModifyPostView {
        reset()
        return modifyPostView
    }
}

@Module
class TestNewTopicScreenModule {
    companion object {
        var newTopicView: NewTopicView = mock()
            private set

        fun reset() {
            newTopicView = mock()
        }
    }

    @Provides
    fun provideNewTopicView(): NewTopicView = newTopicView
}

@Module
class PostListViewModule {
    companion object {
        var postListView: PostListView = mock()
            private set

        fun reset() {
            postListView = mock()
        }
    }

    @Provides
    fun provideView(): PostListView = postListView
}

@Module
class TopicPostListViewModule {
    companion object {
        var postListView: TopicPostListView = mock()
            private set

        fun reset() {
            postListView = mock()
        }
    }

    @Provides
    fun provideView(): TopicPostListView = postListView
}



@Module
class SearchResultsViewModule {
    companion object {
        var postListView: SearchResultsView = mock()
            private set

        fun reset() {
            postListView = mock()
        }
    }

    @Provides
    fun provideView(): SearchResultsView = postListView
}

@Module
class PhotoViewModule {
    companion object {
        var view: IPhotoViewerView = mock()
            private set

        fun reset() {
            view = mock()
        }
    }

    @Provides
    fun provideView(): IPhotoViewerView = view
}