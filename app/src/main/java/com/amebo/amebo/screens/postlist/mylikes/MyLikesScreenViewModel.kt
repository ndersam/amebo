package com.amebo.amebo.screens.postlist.mylikes

import android.app.Application
import com.amebo.amebo.common.Pref
import com.amebo.amebo.screens.postlist.PostListScreenViewModel
import com.amebo.core.Nairaland
import com.amebo.core.domain.MyLikes
import javax.inject.Inject

class MyLikesScreenViewModel @Inject constructor(
    server: Nairaland,
    pref: Pref,
    application: Application
) :
    PostListScreenViewModel<MyLikes>(server, pref, application)
