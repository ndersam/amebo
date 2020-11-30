package com.amebo.amebo.screens.photoviewer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.R
import com.amebo.amebo.common.AppUtil
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.core.Nairaland
import com.amebo.core.domain.ResultWrapper
import com.amebo.core.domain.User
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import javax.inject.Inject

class PhotoSharedViewModel @Inject constructor(private val nairaland: Nairaland) : ViewModel() {
    var currentImageDrawable: Event<Drawable>? = null

    private val _likePhotoEvent = MutableLiveData<Event<Resource<LikeResult>>>()
    val likePhotoEvent: LiveData<Event<Resource<LikeResult>>> = _likePhotoEvent

    fun likePhoto(like: Boolean, user: User) {
        viewModelScope.launch {
            _likePhotoEvent.value = Event(Resource.Loading(null))
            _likePhotoEvent.value = Event(
                when(val result = nairaland.sources.submissions.likeProfilePhoto(like, user)){
                    is ResultWrapper.Failure -> Resource.Error(result.data, null)
                    is ResultWrapper.Success -> Resource.Success(LikeResult(like, result.data))
                }
            )
        }
    }

    fun saveImage(view: View, url: String) = viewModelScope.launch {
        val context = view.context
        when (val path = AppUtil.saveImage(context, url)) {
            null -> {
                snack(view, R.string.download_failed)
            }
            else -> {
                AppUtil.addToGallery(context, path)
                snack(view, R.string.saved_to_gallery)
            }
        }
    }

    fun snack(view: View, @StringRes msg: Int) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show()
    }

    fun shareImage(view: View) {
        val drawable = currentImageDrawable?.getContentIfNotHandled() ?: return
        val result = AppUtil.shareImage(view.context, drawable)
        if (!result) {
            snack(view, R.string.share_failed)
        }
    }

    fun copyToClipBoard(view: View, url: String) {
        val clipboard =
            view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip =
            ClipData.newPlainText(view.context.getString(R.string.image_url), url)
        clipboard.setPrimaryClip(clip)
        snack(view, R.string.link_copied)
    }
}