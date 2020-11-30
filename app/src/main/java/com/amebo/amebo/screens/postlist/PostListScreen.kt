package com.amebo.amebo.screens.postlist

import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.databinding.PostListScreenBinding
import com.amebo.amebo.screens.postlist.components.IPostListView
import com.amebo.amebo.screens.postlist.components.PostListView
import com.amebo.core.domain.PostList
import javax.inject.Inject
import javax.inject.Provider

/**
 * Convenient [BasePostListScreen] subclass
 */
abstract class PostListScreen<T : PostList> : BasePostListScreen<T>(R.layout.post_list_screen) {
    val binding: PostListScreenBinding by viewBinding(PostListScreenBinding::bind)
    override var postListView: IPostListView? = null

    @Inject
    lateinit var viewProvider: Provider<PostListView>
    override fun initializeViews() {
        postListView = viewProvider.get()
    }
}