package com.amebo.amebo.screens.postlist.mylikes

import com.amebo.amebo.common.fragments.AuthenticationRequired
import com.amebo.amebo.screens.postlist.TimelinePostListScreen
import com.amebo.core.domain.MyLikes

class MyLikesScreen : TimelinePostListScreen<MyLikes>(), AuthenticationRequired {
    override val viewModel by viewModels<MyLikesScreenViewModel>()
    override val postList = MyLikes
}