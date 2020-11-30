package com.amebo.amebo.screens.photoviewer.user

import com.amebo.amebo.screens.photoviewer.IPhotoViewerView
import com.amebo.amebo.screens.photoviewer.posts.PhotoViewerScreenModule
import dagger.Module
import dagger.Provides

@Module
class UserPhotoViewerScreenModule {

    @Provides
    fun providePhotoView(screen: UserPhotoViewerScreen): IPhotoViewerView {
        return UserPostPhotoViewerView(
            screen.binding,
            PhotoViewerScreenModule.newAdapter(screen),
            screen.pref,
            screen,
            screen.user
        )
    }

}