package com.amebo.amebo.screens.photoviewer.user

import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.databinding.FragmentUserPhotoViewerScreenBinding
import com.amebo.amebo.screens.photoviewer.BasePhotoViewerScreen
import com.amebo.amebo.screens.photoviewer.LikeResult
import com.amebo.core.domain.User


class UserPhotoViewerScreen : BasePhotoViewerScreen(R.layout.fragment_user_photo_viewer_screen),
    UserPostPhotoViewerView.Listener {

    val user: User get() = requireArguments().getParcelable(USER)!!
    val binding by viewBinding(FragmentUserPhotoViewerScreenBinding::bind)

    override fun onViewCreated(savedInstanceState: Bundle?) {
        super.onViewCreated(savedInstanceState)

        viewModel.likePhotoEvent.observe(
            viewLifecycleOwner,
            EventObserver(::onLikePhotoEventContent)
        )
    }

    override fun toggleToggleToolbar() {
        binding.toolbar.isVisible = !binding.toolbar.isVisible
        binding.bottomBar.isVisible = !binding.bottomBar.isVisible
    }

    override fun likePhoto(like: Boolean) = viewModel.likePhoto(like, user)

    private fun onLikePhotoEventContent(resource: Resource<LikeResult>) {
        val view = photoView as UserPostPhotoViewerView
        when (resource) {
            is Resource.Success -> {
                user.data = resource.content.data
                setFragmentResult(
                    FragKeys.RESULT_UPDATED_USER,
                    bundleOf(FragKeys.BUNDLE_UPDATED_USER to user.data)
                )
                view.onDataSuccess(resource)
            }
            is Resource.Loading -> view.onDataLoading(resource)
            is Resource.Error -> view.onDataError(resource)
        }
    }

    companion object {
        private const val USER = "USER"

        fun newBundle(user: User, link: String, transitionName: String) =
            newBundle(listOf(link), transitionName, 0).apply {
                putParcelable(USER, user)
            }

        fun newBundle(user: User, uri: Uri, transitionName: String) =
            newBundleForUris(listOf(uri), transitionName, 0).apply {
                putParcelable(USER, user)
            }
    }
}
