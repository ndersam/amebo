package com.amebo.amebo.screens.postlist.following

import android.app.Application
import com.amebo.amebo.common.Pref
import com.amebo.amebo.screens.postlist.PostListScreenViewModel
import com.amebo.core.Nairaland
import com.amebo.core.domain.PostsByPeopleYouAreFollowing
import javax.inject.Inject

class FollowingViewModel @Inject constructor(
    server: Nairaland,
    pref: Pref,
    application: Application
) : PostListScreenViewModel<PostsByPeopleYouAreFollowing>(server, pref, application)