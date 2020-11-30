package com.amebo.amebo.screens.imagepicker

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.*
import com.amebo.amebo.common.extensions.getBitmap
import com.amebo.amebo.common.extensions.getPath
import com.amebo.amebo.common.Event
import com.amebo.core.Nairaland
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class ImagePickerSharedViewModel @Inject constructor(private val nairaland: Nairaland) :
    ViewModel() {
    private val liveData = MutableLiveData<Event<ViewState>>()
    private val images: MutableList<ImageItem> = mutableListOf()
    private val newImagesSelected get() = images.filterIsInstance<ImageItem.New>()
    private var existingItemsRemoved = mutableListOf<ImageItem.Existing>()

    /**
     * @see [addImageFromUri]
     * @see [removeImage]
     */
    private val _imagesUpdatedEvent = MutableLiveData<Event<PostImagesUpdate>>()
    val imagesUpdatedEvent: LiveData<Event<PostImagesUpdate>> = _imagesUpdatedEvent


    fun observe(
        owner: LifecycleOwner,
        observer: Observer<Event<ViewState>>
    ) {
        liveData.value = Event(ViewState.ImagesAvailable(images))
        liveData.observe(owner, observer)
    }

    private fun notifyUpdate() {
        _imagesUpdatedEvent.value = Event(
            PostImagesUpdate(
                allNew = newImagesSelected,
                existingRemoved = existingItemsRemoved
            )
        )
    }

    fun removeImage(item: ImageItem) {
        images.remove(item)
        if (item is ImageItem.Existing) {
            existingItemsRemoved.add(item)
        }
        notifyUpdate()
    }

    fun setImages(images: List<ImageItem>) {
        this.images.clear()
        this.existingItemsRemoved.clear()
        this.images.addAll(images)
    }

    fun addImages(activity: Activity, uris: List<Uri>) {
        viewModelScope.launch {
            val imagesToAdd = mutableListOf<ImageItem>()
            uris.forEach { uri ->
                if (images.size == 4) {
                    return@forEach
                }
                when (addImageFromUri(activity, uri)) {
                    ERROR_BIG_FILE -> {
                        liveData.value = Event(ViewState.Error.TooLarge)
                    }
                    ERROR_FILENAME_CANNOT_BE_DETERMINED -> {
                        liveData.value = Event(ViewState.Error.Unknown)
                    }
                    OKAY -> {
                        imagesToAdd.add(images.last()) // last added in addImageFromUri
                    }
                    else -> throw IllegalStateException()
                }
            }
            if (imagesToAdd.isNotEmpty() && liveData.hasObservers()) {
                notifyUpdate()
                liveData.value = Event(ViewState.ImagesAvailable(imagesToAdd))
            }
        }
    }


    private suspend fun addImageFromUri(activity: Activity, uri: Uri): Int =
        withContext(Dispatchers.IO) {
            val (bitmap, bitmapByteCount) = uri.getBitmap(activity)
            when {
                bitmapByteCount > IMAGE_SIZE_LIMIT -> ERROR_BIG_FILE
                else -> {
                    try {
                        val filename = uri.getPath(activity)
                        images.add(ImageItem.New(bitmap, filename, uri))
                        OKAY
                    } catch (e: IllegalStateException) {
                        ERROR_FILENAME_CANNOT_BE_DETERMINED
                    }
                }
            }
        }


    companion object {
        private const val IMAGE_SIZE_LIMIT = 1024 * 1024 * 4
        private const val ERROR_BIG_FILE = 9999
        private const val ERROR_FILENAME_CANNOT_BE_DETERMINED = 11
        private const val OKAY = 0
    }
}
