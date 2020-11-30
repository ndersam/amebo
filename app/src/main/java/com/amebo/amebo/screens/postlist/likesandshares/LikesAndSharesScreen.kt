package com.amebo.amebo.screens.postlist.likesandshares

import com.amebo.amebo.common.fragments.AuthenticationRequired
import com.amebo.amebo.screens.postlist.TimelinePostListScreen
import com.amebo.core.domain.LikesAndShares

class LikesAndSharesScreen : TimelinePostListScreen<LikesAndShares>(), AuthenticationRequired {
    override val viewModel by viewModels<LikesAndSharesViewModel>()
    override val postList = LikesAndShares
}