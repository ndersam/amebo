package com.amebo.amebo.screens.postlist.following

import com.amebo.amebo.common.fragments.AuthenticationRequired
import com.amebo.amebo.screens.postlist.TimelinePostListScreen
import com.amebo.core.domain.PostsByPeopleYouAreFollowing

class PostsFromFollowingsScreen : TimelinePostListScreen<PostsByPeopleYouAreFollowing>(), AuthenticationRequired {
    override val viewModel by viewModels<FollowingViewModel>()
    override val postList = PostsByPeopleYouAreFollowing
}
