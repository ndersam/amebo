package com.amebo.amebo.screens.accounts.edit

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.getBitmap
import com.amebo.amebo.common.extensions.getBitmapOriginal
import com.amebo.amebo.common.extensions.getPath
import com.amebo.amebo.common.extensions.toResource
import com.amebo.core.Nairaland
import com.amebo.core.domain.*
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EditAccountScreenViewModel @Inject constructor(
    private val nairaland: Nairaland,
    private val pref: Pref,
    application: Application
) : AndroidViewModel(application) {

    private val _displayPhotoEvent = MutableLiveData<Event<Resource<DisplayPhoto>>>()
    val displayPhotoEvent: LiveData<Event<Resource<DisplayPhoto>>> = _displayPhotoEvent
    private var existingDisplayPhoto: DisplayPhoto? = null
    private var selectedDisplayPhoto: DisplayPhotoBitmap? = null

    private val _editProfileFormEvent = MutableLiveData<Event<Resource<EditProfileFormData>>>()
    val editProfileFormEvent: LiveData<Event<Resource<EditProfileFormData>>> = _editProfileFormEvent
    private var editProfileForm: EditProfileForm? = null
    var editProfileFormData: EditProfileFormData? = null

    private val _editProfileSubmissionEvent = MutableLiveData<Event<Resource<User.Data>>>()
    val editProfileSubmissionEvent: LiveData<Event<Resource<User.Data>>> =
        _editProfileSubmissionEvent

    private val _imageRemovedEvent = MutableLiveData<Event<Unit>>()
    val imageRemovedEvent: LiveData<Event<Unit>> = _imageRemovedEvent


    private val user: User? get() = pref.user

    val existingImageUrl
        get() = when (val photo = existingDisplayPhoto) {
            is DisplayPhotoUrl -> photo.url
            else -> null
        }
    val selectedImageUri
        get() = when (val uri = editProfileFormData?.selectedPhoto) {
            is Uri -> uri
            else -> null
        }


    fun loadDisplayPhoto() {
        // if has selected photo, publish event and return
        when (val photo = selectedDisplayPhoto) {
            is DisplayPhoto -> {
                _displayPhotoEvent.value = Event(Resource.Success(photo))
                return
            }
        }

        // if existing image marked for removal, publish event
        if (editProfileFormData?.removeThisImage == true) {
            _imageRemovedEvent.value = Event(Unit)
            return
        }

        // if has existing photo, publish event and return
        when (val photo = existingDisplayPhoto) {
            is DisplayPhoto -> {
                _displayPhotoEvent.value = Event(Resource.Success(photo))
                return
            }
            null -> {}
        }

        val user = user ?: return
        viewModelScope.launch {
            _displayPhotoEvent.value = Event(Resource.Loading(existingDisplayPhoto))

            val resource = when (val result = nairaland.sources.accounts.displayPhoto(user)) {
                is Ok -> {
                    existingDisplayPhoto = result.value
                    Resource.Success(existingDisplayPhoto!!)
                }
                is Err -> {
                    Resource.Error(cause = result.error, content = existingDisplayPhoto)
                }
            }
            _displayPhotoEvent.value = Event(resource)
        }
    }

    fun loadForm() {
        if (editProfileFormData == null) {
            doLoadForm()
        } else {
            _editProfileFormEvent.value = Event(Resource.Success(editProfileFormData!!))
        }
    }

    fun updateProfile() {
        val form = editProfileForm ?: return
        val data = editProfileFormData ?: return
        form.apply {
            photo = when (val uri = data.selectedPhoto) {
                null -> null
                else -> uri.getPath(getApplication()).substringAfterLast('/') to
                        uri.getBitmapOriginal(getApplication()).first
            }
            location = data.location
            signature = data.signature
            personalText = data.personalText
            gender = data.gender
            twitter = data.twitter
            birthDate = data.birthDate
            yim = data.yim
            removeThisImage = data.removeThisImage
        }
        viewModelScope.launch {
            _editProfileSubmissionEvent.value = Event(Resource.Loading(null))
            val result = nairaland.sources.submissions.editProfile(form)
            _editProfileSubmissionEvent.value = Event(result.toResource(null))
        }
    }

    fun removeImage() {
        editProfileFormData?.removeThisImage = true
        _imageRemovedEvent.value = Event(Unit)
    }

    private fun doLoadForm() {
        viewModelScope.launch {
            _editProfileFormEvent.value = Event(Resource.Loading(editProfileFormData))
            val resource = when (val result = nairaland.sources.forms.editProfile()) {
                is Ok -> {
                    editProfileForm = result.value
                    editProfileForm?.let {
                        editProfileFormData =
                            EditProfileFormData(
                                birthDate = it.birthDate,
                                personalText = it.personalText,
                                signature = it.signature,
                                location = it.location,
                                yim = it.yim,
                                twitter = it.twitter,
                                gender = it.gender,
                                selectedPhoto = null,
                                removeThisImage = it.removeThisImage,
                                earliestYear = it.earliestYear
                            )
                    }
                    Resource.Success(editProfileFormData!!)
                }
                is Err -> {
                    Resource.Error(result.error, editProfileFormData)
                }
            }
            _editProfileFormEvent.value = Event(resource)
        }
    }


    fun setImage(uri: Uri) {
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                uri.getBitmap(getApplication()).first
            }
            editProfileFormData?.selectedPhoto = uri
            selectedDisplayPhoto = DisplayPhotoBitmap(bitmap)
            _displayPhotoEvent.value = Event(Resource.Success(selectedDisplayPhoto!!))
        }
    }

    fun revertToOldImage() {
        val data = editProfileFormData ?: return
        // if has selected photo, remove
        if (data.selectedPhoto != null) {
            data.selectedPhoto = null
            selectedDisplayPhoto = null
        } else if (data.removeThisImage) {
            data.removeThisImage = false
        }
        val content = Resource.Success(existingDisplayPhoto ?: return)
        _displayPhotoEvent.value = Event(content)
    }

}

