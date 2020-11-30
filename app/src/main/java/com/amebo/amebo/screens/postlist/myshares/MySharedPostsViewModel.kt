package com.amebo.amebo.screens.postlist.myshares

import android.app.Application
import com.amebo.amebo.common.Pref
import com.amebo.amebo.screens.postlist.PostListScreenViewModel
import com.amebo.core.Nairaland
import com.amebo.core.domain.MySharedPosts
import javax.inject.Inject

class MySharedPostsViewModel @Inject constructor(
    server: Nairaland,
    pref: Pref,
    application: Application
) :
    PostListScreenViewModel<MySharedPosts>(server, pref, application)
