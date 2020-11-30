package com.amebo.amebo.screens.search

import android.app.Application
import com.amebo.amebo.common.Pref
import com.amebo.amebo.screens.postlist.PostListScreenViewModel
import com.amebo.core.Nairaland
import com.amebo.core.domain.SearchQuery
import javax.inject.Inject

class SearchResultsViewModel @Inject constructor(server: Nairaland, pref: Pref, application: Application) :
    PostListScreenViewModel<SearchQuery>(server, pref, application)