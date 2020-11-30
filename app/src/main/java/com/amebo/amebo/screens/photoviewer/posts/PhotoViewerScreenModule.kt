package com.amebo.amebo.screens.photoviewer.posts

import com.amebo.amebo.screens.photoviewer.BasePhotoViewerScreen
import com.amebo.amebo.screens.photoviewer.IPhotoViewerView
import com.amebo.amebo.screens.photoviewer.ImagePagerAdapter
import dagger.Module
import dagger.Provides

@Module
class PhotoViewerScreenModule {

    @Provides
    fun providePhotoView(screen: PhotoViewerScreen): IPhotoViewerView {
        return PostPhotoViewerView(screen.binding, newAdapter(screen), screen)
    }

    companion object {
        fun newAdapter(screen: BasePhotoViewerScreen) =
            ImagePagerAdapter(screen, screen.imageLinks, screen.imageUris, screen.transitionName)
    }

}