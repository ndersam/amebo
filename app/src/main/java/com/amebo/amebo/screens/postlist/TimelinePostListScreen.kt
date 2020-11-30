package com.amebo.amebo.screens.postlist

import android.os.Bundle
import androidx.core.os.bundleOf
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.databinding.PostListScreenBinding
import com.amebo.amebo.screens.postlist.components.IPostListView
import com.amebo.amebo.screens.postlist.components.PostListView
import com.amebo.core.domain.Timeline
import javax.inject.Inject
import javax.inject.Provider


abstract class TimelinePostListScreen<T : Timeline> :
    BasePostListScreen<T>(R.layout.post_list_screen) {

    val binding: PostListScreenBinding by viewBinding(PostListScreenBinding::bind)
    override var postListView: IPostListView? = null

    val isRootScreen get() = requireArguments().getBoolean(IS_ROOT_SCREEN, false)

    @Inject
    lateinit var postListViewProvider: Provider<PostListView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // We need bundle for layout manager state
        if (arguments == null) {
            arguments = Bundle()
        }
    }

    override fun initializeViews() {
        postListView = postListViewProvider.get()
    }

    companion object {
        private const val IS_ROOT_SCREEN = "IS_ROOT_SCREEN"

        fun TimelinePostListScreen<*>.rootScreen() = apply {
            arguments = bundleOf(IS_ROOT_SCREEN to true)
        }
    }
}
