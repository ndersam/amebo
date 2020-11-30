package com.amebo.core.domain

import android.graphics.Bitmap

sealed class DisplayPhoto
class DisplayPhotoBitmap(val bitmap: Bitmap): DisplayPhoto()
class DisplayPhotoUrl(val url: String): DisplayPhoto()
object NoDisplayPhoto: DisplayPhoto()