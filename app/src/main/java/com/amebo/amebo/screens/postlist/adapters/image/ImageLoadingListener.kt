package com.amebo.amebo.screens.postlist.adapters.image

import android.widget.ImageView


interface ImageLoadingListener {
    fun onLoadCompleted(view: ImageView, postPosition: Int, imagePosition: Int)
}