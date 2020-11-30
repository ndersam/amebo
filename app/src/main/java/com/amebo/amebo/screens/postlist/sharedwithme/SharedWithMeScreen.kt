package com.amebo.amebo.screens.postlist.sharedwithme

import com.amebo.amebo.common.fragments.AuthenticationRequired
import com.amebo.amebo.screens.postlist.TimelinePostListScreen
import com.amebo.core.domain.SharedPosts

class SharedWithMeScreen : TimelinePostListScreen<SharedPosts>(), AuthenticationRequired {
    override val viewModel by viewModels<SharedWithMeViewModel>()
    override val postList = SharedPosts
}