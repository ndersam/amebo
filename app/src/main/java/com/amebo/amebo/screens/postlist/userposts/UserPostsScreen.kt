package com.amebo.amebo.screens.postlist.userposts

import androidx.core.os.bundleOf
import com.amebo.amebo.screens.postlist.PostListScreen
import com.amebo.amebo.screens.postlist.PostListScreenViewModel
import com.amebo.core.domain.User
import com.amebo.core.domain.UserPosts

class UserPostsScreen : PostListScreen<UserPosts>() {

    private val user get() = requireArguments().getParcelable<User>(USER)!!
    override val viewModel: PostListScreenViewModel<UserPosts> by viewModels<UserPostsViewModel>()

    override val postList: UserPosts by lazy { UserPosts(user) }


    companion object {
        private const val USER = "USER"
        fun newBundle(user: User) = bundleOf(USER to user)
    }
}
