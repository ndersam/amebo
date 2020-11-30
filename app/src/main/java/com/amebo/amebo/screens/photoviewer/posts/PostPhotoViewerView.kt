package com.amebo.amebo.screens.photoviewer.posts

import com.amebo.amebo.databinding.FragmentPhotoViewerScreenBinding
import com.amebo.amebo.screens.photoviewer.BasePhotoViewerView
import com.amebo.amebo.screens.photoviewer.IPhotoViewerView
import com.amebo.amebo.screens.photoviewer.ImagePagerAdapter

class PostPhotoViewerView(
    binding: FragmentPhotoViewerScreenBinding,
    adapter: ImagePagerAdapter,
    listener: IPhotoViewerView.Listener
) : BasePhotoViewerView(
    binding.toolbar,
    binding.viewPager,
    adapter, listener
)