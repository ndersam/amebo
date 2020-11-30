package com.amebo.amebo.screens.postlist

import com.amebo.amebo.screens.postlist.adapters.posts.ItemAdapter
import com.amebo.amebo.screens.postlist.components.PostListView
import com.amebo.amebo.screens.postlist.components.TimelinePostListView

object TimelinePostListScreenModuleHelper {

    fun providePostListView(
        timelinePostListScreen: TimelinePostListScreen<*>,
        contentAdapter: ItemAdapter
    ): PostListView {
        return TimelinePostListView(
            timelinePostListScreen,
            contentAdapter,
            timelinePostListScreen.postList,
            timelinePostListScreen
        )
    }

}