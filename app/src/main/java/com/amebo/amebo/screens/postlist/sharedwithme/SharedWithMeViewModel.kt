package com.amebo.amebo.screens.postlist.sharedwithme

import android.app.Application
import com.amebo.amebo.common.Pref
import com.amebo.amebo.screens.postlist.PostListScreenViewModel
import com.amebo.core.Nairaland
import com.amebo.core.domain.SharedPosts
import javax.inject.Inject

class SharedWithMeViewModel @Inject constructor(server: Nairaland, pref: Pref, application: Application) :
    PostListScreenViewModel<SharedPosts>(server, pref, application)