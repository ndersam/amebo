package com.amebo.amebo.screens.postlist.userposts

import android.app.Application
import com.amebo.amebo.common.Pref
import com.amebo.amebo.screens.postlist.PostListScreenViewModel
import com.amebo.core.Nairaland
import com.amebo.core.domain.UserPosts
import javax.inject.Inject

class UserPostsViewModel @Inject constructor(server: Nairaland, pref: Pref, application: Application) :
    PostListScreenViewModel<UserPosts>(server, pref, application)
