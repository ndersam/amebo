package com.amebo.amebo.screens.imagepicker

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import com.amebo.core.domain.Attachment
import kotlinx.parcelize.Parcelize

sealed class ImageItem : Parcelable {
    /**
     * [ImageItem] already uploaded to a post
     */
    @Parcelize
    class Existing(val attachment: Attachment, val url: String) : ImageItem() {
        val name: String get() = attachment.name
    }

    /**
     * [ImageItem] that has been newly selected from gallery
     */
    @Parcelize
    class New(val downscaledBitmap: Bitmap, val path: String, val uri: Uri) : ImageItem() {
        val name get() = path.substringAfterLast('/')
    }
}


class PostImagesUpdate(
    val allNew: List<ImageItem.New>,
    val existingRemoved: List<ImageItem.Existing>
)