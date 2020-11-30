package com.amebo.amebo.screens.user

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.UserScreenBinding
import com.amebo.core.domain.Board
import com.amebo.core.domain.Topic
import com.amebo.core.domain.User


open class UserScreen : BaseFragment(R.layout.user_screen), UserScreenView.Listener {
    var user: User
        get() = requireArguments().getParcelable(USER)!!
        set(value) {
            requireArguments().putParcelable(USER, value)
        }

    protected val binding: UserScreenBinding by viewBinding(UserScreenBinding::bind)
    private val viewModel by viewModels<UserScreenViewModel>()
    private lateinit var userView: UserScreenView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(FragKeys.RESULT_UPDATED_USER) { _, bundle ->
            user.data = bundle.getParcelable(FragKeys.BUNDLE_UPDATED_USER)
            userView.onDataLoadSuccess(
                Resource.Success(
                    user.data ?: return@setFragmentResultListener
                )
            )
        }
    }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        userView = UserScreenView(pref, binding, user, this, lifecycleScope)
        viewModel.initialize(user)
        viewModel.dataEvent.observe(viewLifecycleOwner, EventObserver(::onLoadUserDataEventContent))
        viewModel.followUserEvent.observe(
            viewLifecycleOwner,
            EventObserver(::onFollowUserEventContent)
        )
        viewModel.loadConditionally()
    }

    private fun onLoadUserDataEventContent(content: Resource<User.Data>) {
        when (content) {
            is Resource.Error -> userView.onDataLoadError(content)
            is Resource.Success -> {
                user.data = content.content
                userView.onDataLoadSuccess(content)
            }
            is Resource.Loading -> userView.onDataLoadProgress(content)
        }
    }

    private fun onFollowUserEventContent(content: Resource<User.Data>) {
        when (content) {
            is Resource.Error -> userView.onFollowUserError(content)
            is Resource.Success -> userView.onFollowUserSuccess(content)
            is Resource.Loading -> userView.onFollowUserProgress(content)
        }
    }


    override fun onDisplayPhotoClicked(url: String) {
        router.toPhotoViewer(
            user,
            url,
            binding.displayPhoto,
            binding.displayPhoto.transitionName
        )
    }

    override fun load() = viewModel.load()

    override fun visitFollowersListener(followers: List<User>) {
        val title = getString(R.string.users_followed_by_x, user.name)
        router.toUserList(followers, title)
    }

    override fun onTwitterClicked(text: String) = viewModel.viewTwitter(requireContext(), text)

    override fun onFollowClicked(follow: Boolean) = viewModel.follow(follow)

    override fun sendMail() = router.toMailUser(user)


    override fun onBoardClicked(board: Board) = router.toBoard(board)

    override fun onLatestTopicsClicked(topics: List<Topic>) =
        router.toTopicList(topics, getString(R.string.latest_topics_by, user.name))

    override fun onEditClicked() = router.toEditProfile()

    override fun onViewPostsClicked() = router.toUserPosts(user)


    override fun onViewTopicsClicked() = router.toUserTopics(user)

    override var lastDisplayPhotoLoadSuccessful: Boolean
        get() = requireArguments().getBoolean("lastDisplayPhotoLoadSuccessful", false)
        set(value) {
            requireArguments().putBoolean("lastDisplayPhotoLoadSuccessful", value)
        }

    override fun goBack() {
        router.back()
    }

    override fun showSearch() = router.toGotoUserProfile()


    companion object {
        const val USER = "user"
        fun newBundle(user: User) = bundleOf(USER to user)
    }
}
