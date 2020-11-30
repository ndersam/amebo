package com.amebo.amebo.screens.photoviewer.posts

import androidx.core.view.isVisible
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.databinding.FragmentPhotoViewerScreenBinding
import com.amebo.amebo.screens.photoviewer.BasePhotoViewerScreen


class PhotoViewerScreen : BasePhotoViewerScreen(R.layout.fragment_photo_viewer_screen) {

    val binding: FragmentPhotoViewerScreenBinding by viewBinding(FragmentPhotoViewerScreenBinding::bind)

    override fun toggleToggleToolbar() {
        binding.toolbar.isVisible = !binding.toolbar.isVisible
    }
}
