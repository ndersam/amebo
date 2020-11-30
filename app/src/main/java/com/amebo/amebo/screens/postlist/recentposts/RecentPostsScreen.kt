package com.amebo.amebo.screens.postlist.recentposts

import com.amebo.amebo.screens.postlist.TimelinePostListScreen
import com.amebo.core.domain.RecentPosts

class RecentPostsScreen : TimelinePostListScreen<RecentPosts>() {
    override val viewModel by viewModels<RecentPostsViewModel>()
    override val postList = RecentPosts
}
