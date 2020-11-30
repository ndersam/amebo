package com.amebo.amebo.screens.photoviewer

import android.view.View

interface IPhotoViewerView {
    val currentImageView: View?
    interface  Listener {
        fun copyToClipBoard()
        fun saveImage()
        fun shareImage()
        fun goBack()
        var currentPosition: Int
    }
}