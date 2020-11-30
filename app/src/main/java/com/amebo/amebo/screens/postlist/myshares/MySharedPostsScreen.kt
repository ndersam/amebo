package com.amebo.amebo.screens.postlist.myshares

import com.amebo.amebo.common.fragments.AuthenticationRequired
import com.amebo.amebo.screens.postlist.TimelinePostListScreen
import com.amebo.core.domain.MySharedPosts

class MySharedPostsScreen : TimelinePostListScreen<MySharedPosts>(), AuthenticationRequired {
    override val viewModel by viewModels<MySharedPostsViewModel>()
    override val postList = MySharedPosts
}