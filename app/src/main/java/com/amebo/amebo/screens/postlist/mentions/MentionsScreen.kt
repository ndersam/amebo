package com.amebo.amebo.screens.postlist.mentions

import com.amebo.amebo.common.fragments.AuthenticationRequired
import com.amebo.amebo.screens.postlist.TimelinePostListScreen
import com.amebo.core.domain.Mentions

class MentionsScreen : TimelinePostListScreen<Mentions>(), AuthenticationRequired {
    override val viewModel by viewModels<MentionsViewModel>()
    override val postList: Mentions get() = Mentions(pref.user!!)
}