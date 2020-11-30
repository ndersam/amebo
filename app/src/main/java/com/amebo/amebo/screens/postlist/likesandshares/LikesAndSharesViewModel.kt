package com.amebo.amebo.screens.postlist.likesandshares

import android.app.Application
import com.amebo.amebo.common.Pref
import com.amebo.amebo.screens.postlist.PostListScreenViewModel
import com.amebo.core.Nairaland
import com.amebo.core.domain.LikesAndShares
import javax.inject.Inject

class LikesAndSharesViewModel @Inject constructor(server: Nairaland, pref: Pref, application: Application) :
    PostListScreenViewModel<LikesAndShares>(server,pref, application)
